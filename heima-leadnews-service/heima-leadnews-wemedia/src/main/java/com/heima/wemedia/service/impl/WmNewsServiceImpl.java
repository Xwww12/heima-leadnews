package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl  extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Resource
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Resource
    private WmMaterialMapper wmMaterialMapper;

    @Resource
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        dto.checkParam();
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        // 准备查询条件
        Short status = dto.getStatus();
        Date beginPubDate = dto.getBeginPubDate();
        Date endPubDate = dto.getEndPubDate();
        String keyword = dto.getKeyword();
        Integer channelId = dto.getChannelId();
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        if (channelId != null)
            wrapper.eq(WmNews::getChannelId, channelId);                        // 频道
        if (status != null)
            wrapper.eq(WmNews::getStatus, status);                              // 文章状态
        if (beginPubDate != null && endPubDate != null)
            wrapper.between(WmNews::getPublishTime, beginPubDate, endPubDate);  // 时间范围
        if (StringUtils.isNotBlank(keyword))
            wrapper.like(WmNews::getTitle, keyword);                            // 标题模糊查询
        wrapper.eq(WmNews::getUserId, user.getApUserId());                      // 对应用户
        wrapper.orderByDesc(WmNews::getCreatedTime);                            // 创建时间倒排

        // 返回
        Page<WmNews> page = new Page<>();
        page(page, wrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 发布文章
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        // 校验参数
        if (dto == null || dto.getContent() == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 保存or修改文章
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {        // 封面图片
            String imageStr = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(imageStr);
        }
        saveOrUpdateNews(wmNews);

        // 当前为草稿，直接返回，不需要保存文章和素材关系
        if (dto.getStatus().equals(WmNews.Status.NORMAL.getCode()))
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        // 不是草稿，保存文章内容图片与素材的关系
        List<String> materials = ectractUrlInfo(dto.getContent());  // 获取到文章内容中的图片信息
        saveRelativeInfoForContent(materials, wmNews.getId());      // 保存文章和图片的对应关系
        saveRelativeInfoForCover(dto,wmNews,materials);             // 判断是否是自动选择封面图片数量

        // 审核
        try {
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult getDetail(Integer id) {
        // 校验参数
        if (id == null || id < 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 判断文章是否存在
        WmNews wmNews = getById(id);
        if (wmNews == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);

        // 返回
        return ResponseResult.okResult(wmNews);
    }

    @Override
    public ResponseResult deleteNews(Integer id) {
        // 校验参数
        if (id == null || id < 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章ID不可缺少");

        // 判断文章是否存在
        WmNews wmNews = getById(id);
        if (wmNews == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");

        // 判断文章是否已发布，发布的不能删除
        if (wmNews.getStatus().equals(WemediaConstants.WM_HAS_BEEN_PUBLISHED))
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章已发布，不能删除");

        // 删除，返回结果
        boolean result = removeById(id);
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult downOrUpNews(WmNewsDto dto) {
        // 校验参数
        Integer id = dto.getId();
        Short enable = dto.getEnable();
        if (id == null || id < 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章ID不可缺少");

        // 判断文章是否存在
        WmNews wmNews = getById(id);
        if (wmNews == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");

        // 判断文章是否已发布，发布的不能删除
        if (wmNews.getStatus().equals(WemediaConstants.WM_HAS_BEEN_PUBLISHED))
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "当前文章不是发布状态，不能上架下架");

        // 修改状态，返回结果
        wmNews.setEnable(enable);
        boolean result = updateById(wmNews);
        return ResponseResult.okResult(result);
    }

    // 自动选择封面图片数量
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials) {
        // 封面图片
        List<String> images = dto.getImages();
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            if (materials.size() >= 3) {                                            // 展示3张
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else if (materials.size() >= 1) {                                     // 展示1张
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else {
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);                // 无图
            }
            if (images != null && !images.isEmpty())
                wmNews.setImages(StringUtils.join(images, ","));
            updateById(wmNews);
        }
        if(images != null && images.size() > 0){
            saveRelativeInfo(images,wmNews.getId(),WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    // 保存文章和素材的对应关系
    private void saveRelativeInfoForContent(List<String> materials, Integer newsId) {
        saveRelativeInfo(materials,newsId,WemediaConstants.WM_CONTENT_REFERENCE);
    }

    private void saveRelativeInfo(List<String> materials, Integer newsId, short type) {
        if (materials != null && !materials.isEmpty()) {
            // 获取素材id
            List<WmMaterial> dbWmMaterials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, materials));
            if (dbWmMaterials == null || dbWmMaterials.size() == 0)     // 未找到对应素材信息
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            if (materials.size() != dbWmMaterials.size())               // 找到的素材数量不一致
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            // 保存对应关系
            List<Integer> ids = dbWmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
            wmNewsMaterialMapper.saveRelations(ids, newsId, type);
        }
    }

    // 解析出文章中的图片信息
    private List<String> ectractUrlInfo(String content) {
        ArrayList<String> res = new ArrayList<>();
        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if (map.get("type").equals("image")) {
                String imgUrl = (String) map.get("value");
                res.add(imgUrl);
            }
        }
        return res;
    }

    // 保存or更新文章
    private void saveOrUpdateNews(WmNews wmNews) {
        //补全属性
        wmNews.setUserId(WmThreadLocalUtil.getUser().getApUserId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short) 1);             //默认上架
        if (wmNews.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO))
            wmNews.setType(null);
        if (wmNews.getId() == null) {
            // 保存到数据库
            save(wmNews);
        } else {
            // 删除原文章和素材关系、更新文章信息
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
            updateById(wmNews);
        }
    }
}
