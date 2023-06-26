package com.heima.article.test;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.ArticleApplication;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {

    @Resource
    private ApArticleContentMapper apArticleContentMapper;

    @Resource
    private Configuration configuration;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private ApArticleMapper apArticleMapper;

    @Test
    public void createStaticUrlTest() throws IOException, TemplateException {
        // 获取文章内容
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, "1383827787629252610L"));
        if (apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())) {
            // 通过freemarker生成html
            StringWriter out = new StringWriter();
            Template template = configuration.getTemplate("article.ftl");    // 模板
            HashMap<String, Object> params = new HashMap<>();   // 数据
            params.put("content", JSONArray.parseArray(apArticleContent.getContent()));
            template.process(params, out);
            ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes());
            // 上传到minIO
            String url = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", is);
            // 修改ap_article表，保存article_url字段
            ApArticle apArticle = new ApArticle();
            apArticle.setId(apArticleContent.getArticleId());
            apArticle.setStaticUrl(url);
            apArticleMapper.updateById(apArticle);
        }
    }
}
