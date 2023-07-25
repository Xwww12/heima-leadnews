package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmSensitiveQueryDto;
import com.heima.model.wemedia.dtos.WmSensitiveUpdateDto;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {
    /**
     * 保存敏感词
     * @param wmSensitive
     * @return
     */
    ResponseResult saveSensitive(WmSensitive wmSensitive);

    /**
     * 查询敏感词
     * @param dto
     * @return
     */
    ResponseResult sensitiveList(WmSensitiveQueryDto dto);

    /**
     * 更新敏感词
     * @param dto
     * @return
     */
    ResponseResult sensitiveUpdate(WmSensitiveUpdateDto dto);

    /**
     * 删除敏感词
     * @param id
     * @return
     */
    ResponseResult sensitiveDel(Integer id);
}
