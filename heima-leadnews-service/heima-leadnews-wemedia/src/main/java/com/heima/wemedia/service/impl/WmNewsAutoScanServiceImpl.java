package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.TextModeration;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Resource
    private WmNewsMapper wmNewsMapper;

    // 阿里云文本审核
    @Resource
    private TextModeration textModeration;

    // feign远程调用
    @Resource
    private IArticleClient iArticleClient;

    @Resource
    private WmChannelMapper wmChannelMapper;

    @Resource
    private WmUserMapper wmUserMapper;

    @Resource
    private WmSensitiveMapper wmSensitiveMapper;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private Tess4jClient tess4jClient;

    @Async
    @Override
    public void autoScanWmNews(Integer id) throws Exception {
        // 校验参数
        if (id == null || id < 0)
            throw new RuntimeException("参数错误");
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null)
            throw new RuntimeException("文章不存在");
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            // 提取文章中的图片和文本
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);
            // 敏感词过滤
            boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
            if (!isSensitive)
                return;
            // 审核图片
            boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"), wmNews);
            if (!isImageScan)
                return;
            // 审核文本
            boolean isTextScan = handleTextScan((String) textAndImages.get("content"), wmNews);
            if (!isTextScan)
                return;
            // 保存文章信息到App端
            ResponseResult responseResult = saveAppArticle(wmNews);
            if (!responseResult.getCode().equals(200))
                throw new RuntimeException("保存文章到App端失败");
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews, (short) 9, "审核成功");
        }
    }

    // 敏感词过滤
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        // 获取敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitives = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        // 创建字典树
        SensitiveWordUtil.initMap(sensitives);

        // 匹配敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);

        if (map.size() > 0) {
            updateWmNews(wmNews, (short) 2, "文章中存在违规内容");
            return false;
        }
        return true;
    }

    // 保存文章信息到App端
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews, dto);
        // 一些不能直接拷贝的值要手动设置
        dto.setLayout(wmNews.getType());
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null)
            dto.setChannelName(wmChannel.getName());
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null)
            dto.setAuthorName(wmUser.getName());
        if (wmNews.getArticleId() != null)
            dto.setId(wmNews.getArticleId());
        dto.setCreatedTime(new Date());

        ResponseResult responseResult = iArticleClient.saveArticle(dto);
        return responseResult;
    }

    // 提取文章中的图片和文本
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        // 文本
        StringBuilder text = new StringBuilder();
        // 图片
        List<String> images = new ArrayList<>();
        // 提取内容中的文本和图片
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")){
                    text.append(map.get("value"));
                } else if (map.get("type").equals("image")){
                    images.add((String) map.get("value"));
                }
            }
        }
        // 提取封面图片
        if (StringUtils.isNotBlank(wmNews.getImages())) {
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        // 返回
        HashMap<String, Object> res = new HashMap<>();
        res.put("content", text.toString());
        res.put("images", images);
        return res  ;
    }

    // 审核文本
    HashSet<String> labels = new HashSet<>();
    {
        labels.add("ad");
        labels.add("political_content");
        labels.add("profanity");
        labels.add("contraband");
        labels.add("sexual_content");
        labels.add("violence");
        labels.add("ad_compliance");
        labels.add("compliance_fin");
        labels.add("negative_content");
        labels.add("nonsense");
        labels.add("cyberbullying");
        labels.add("C_customized");
    }
    private boolean handleTextScan(String content, WmNews wmNews) throws Exception {
        content = content + "-" + wmNews.getTitle();
        if (content.equals("-"))
            return true;

        // 如果有违规内容，label不为空
        Map map = textModeration.greenTextScan(content);
        if (map != null) {
            String label = (String) map.get("label");
            if (labels.contains(label))
                updateWmNews(wmNews, (short) 2, "当前文章存在违规内容");
            else
                updateWmNews(wmNews, (short) 3, "当前文章存在不确定内容");
            return false;
        }
        return true;
    }



    // 审核图片
    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        if (images == null || images.size() == 0)
            return true;
        images = images.stream().distinct().collect(Collectors.toList());
        ArrayList<byte[]> imageList = new ArrayList<>();
        for (String image : images) {
            byte[] bytes = fileStorageService.downLoadFile(image);

            // 识别图片中是否有敏感词
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                BufferedImage bufferImage = ImageIO.read(bais);
                String result = tess4jClient.doOCR(bufferImage);
                boolean b = handleTextScan(result, wmNews);
                if (!b)
                    return false;
            } catch (Exception e) {
                e.printStackTrace();
            }

            imageList.add(bytes);
        }

        // 图片的url必须得公网能访问，先不审核了
        return true;
    }

    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }
}
