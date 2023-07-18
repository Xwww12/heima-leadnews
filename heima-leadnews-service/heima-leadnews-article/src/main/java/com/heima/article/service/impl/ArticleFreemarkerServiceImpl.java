package com.heima.article.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

@Service
@Slf4j
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {

    @Resource
    private ApArticleContentMapper apArticleContentMapper;

    @Resource
    private Configuration configuration;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private ApArticleMapper apArticleMapper;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Async
    @Override
    public void buildArticleToMinIO(ApArticle apArticle, String content) {
        // 获取文章内容
        if (content != null && StringUtils.isNotBlank(content)) {
            // 通过freemarker生成html
            StringWriter out = new StringWriter();
            Template template = null;    // 模板
            try {
                template = configuration.getTemplate("article.ftl");
                HashMap<String, Object> params = new HashMap<>();   // 数据
                params.put("content", JSONArray.parseArray(content));
                template.process(params, out);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes());
            // 上传到minIO
            String url = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", is);
            // 修改ap_article表，保存article_url字段
            apArticle.setStaticUrl(url);
            apArticleMapper.updateById(apArticle);

            // 通过Kafka发送消息，创建es文档
            createArticleEsIndex(apArticle, content, url);
        }
    }

    // 发送消息给搜索微服务，创建文章的文档
    private void createArticleEsIndex(ApArticle apArticle, String content, String url) {
        SearchArticleVo vo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, vo);
        vo.setContent(content);
        vo.setStaticUrl(url);

        // 发送消息
        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC, JSONArray.toJSONString(vo));
    }
}
