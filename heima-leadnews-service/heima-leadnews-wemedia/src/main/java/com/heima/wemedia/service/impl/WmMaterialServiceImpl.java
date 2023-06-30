package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        // 校验参数
        if (multipartFile == null || multipartFile.getSize() == 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        // 上传到minIO
        String fileName = UUID.randomUUID().toString().replace("-", "");    // 随机的图片名称
        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf(".")); // 后缀
        String fileId = null;
        try {
            fileId = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
            log.info("上传图片到MinIO中，fileId:{}",fileId);
        } catch (IOException e) {
            log.error("WmMaterialServiceImpl-上传文件失败");
            e.printStackTrace();
        }
        // 保存图片路径到数据库
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getApUserId());
        wmMaterial.setUrl(fileId);
        wmMaterial.setIsCollection((short)0);   // 是否收藏
        wmMaterial.setType((short)0);   // 0 图片
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);
        // 返回
        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult findList(WmMaterialDto dto) {
        // 校验参数
        dto.checkParam();
        Integer current = dto.getPage();
        Integer size = dto.getSize();
        Short isCollection = dto.getIsCollection();
        Integer userId = WmThreadLocalUtil.getUser().getApUserId();
        // 构造查询条件
        LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
        if (isCollection != null && isCollection == 1)
            wrapper.eq(WmMaterial::getIsCollection, 1); // 是否收藏
        wrapper.eq(WmMaterial::getUserId, userId);          // 对应用户
        wrapper.orderByDesc(WmMaterial::getCreatedTime);    // 按照创建时间倒排
        // 分页查找
        Page<WmMaterial> page = new Page<>(current, size);
        page(page, wrapper);
        // 返回
        ResponseResult responseResult = new PageResponseResult(current, size, (int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    @Override
    public ResponseResult deletePicture(Integer id) {
        // 参数校验
        if (id == null || id < 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 查找对应图片是否存在
        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);

        // 判断是否有文章在用此图片
        Integer count = wmNewsMaterialMapper.selectCount(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getMaterialId, id));
        if (count > 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文件删除失败");

        // 删除，返回结果
        boolean result = removeById(id);
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult cancelCollect(Integer id) {
        // 参数校验
        if (id == null || id < 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 查找对应图片是否存在
        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);

        // 取消收藏
        wmMaterial.setIsCollection((short) 0);
        boolean result = updateById(wmMaterial);
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult collect(Integer id) {
        // 参数校验
        if (id == null || id < 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 查找对应图片是否存在
        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);

        // 收藏
        wmMaterial.setIsCollection((short) 1);
        boolean result = updateById(wmMaterial);
        return ResponseResult.okResult(result);
    }
}
