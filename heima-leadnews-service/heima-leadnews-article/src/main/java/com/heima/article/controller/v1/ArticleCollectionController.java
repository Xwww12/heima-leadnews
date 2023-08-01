package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.model.behavior.dto.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/")
public class ArticleCollectionController {

    @Resource
    private ApArticleService apArticleService;

    @PostMapping("/collection_behavior")
    public ResponseResult collection(@RequestBody CollectionBehaviorDto dto) {
        return apArticleService.collection(dto);
    }
}
