package com.heima.wemedia.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserAuthDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsListDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 查询文章
     * @param dto
     * @return
     */
    public ResponseResult findAll(WmNewsPageReqDto dto);

    /**
     *  发布文章或保存草稿
     * @param dto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto dto);

    /**
     * 查看文章详情
     * @param id
     * @return
     */
    ResponseResult getDetail(Integer id);

    /**
     * 删除文章
     * @param id
     * @return
     */
    ResponseResult deleteNews(Integer id);

    /**
     * 上架/下架文章
     * @param dto
     * @return
     */
    ResponseResult downOrUpNews(WmNewsDto dto);

    /**
     * 文章列表
     * @param dto
     * @return
     */
    ResponseResult listVo(WmNewsListDto dto);

    /**
     * 文章审核通过
     * @param dto
     * @return
     */
    ResponseResult authPass(UserAuthDto dto);

    /**
     * 文章审核失败
     * @param dto
     * @return
     */
    ResponseResult authFail(UserAuthDto dto);
}