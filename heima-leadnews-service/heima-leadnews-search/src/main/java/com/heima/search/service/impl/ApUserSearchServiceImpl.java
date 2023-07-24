package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.interceptor.AppTokenInterceptor;
import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ApUserSearchServiceImpl implements ApUserSearchService {
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    @Async
    public void insert(String keyword, Integer userId) {
        // 查找搜索词是否在历史中
        Query query = Query.query(Criteria.
                where("userId").is(userId).
                and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        if (apUserSearch != null) {
            // 如果搜索词在历史中 更新时间
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
        } else {
            // 不在，则只保存最新的10条记录
            apUserSearch = new ApUserSearch();
            apUserSearch.setUserId(userId);
            apUserSearch.setKeyword(keyword);
            apUserSearch.setCreatedTime(new Date());

            query = Query.query(Criteria.where("userId").is(userId))
                    .with(Sort.by(Sort.Direction.DESC, "createdTime"));
            List<ApUserSearch> apUserSearchList = mongoTemplate.find(query, ApUserSearch.class);
            if (apUserSearchList == null || apUserSearchList.size() < 10)
                mongoTemplate.save(apUserSearch);
            else {
                // 替换最旧的一条记录
                ApUserSearch last = apUserSearchList.get(apUserSearchList.size() - 1);
                mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(last.getId())), apUserSearch);
            }
        }
    }

    @Override
    public ResponseResult findUserSearch() {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null)
            return new ResponseResult().errorResult(AppHttpCodeEnum.NEED_LOGIN);

        Query query = Query.query(Criteria.where("userId").is(user.getId()))
                .with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApUserSearch> apUserSearches = mongoTemplate.find(query, ApUserSearch.class);
        return new ResponseResult().ok(apUserSearches);
    }

    @Override
    public ResponseResult delUserSearch(HistorySearchDto dto) {
        if (dto.getId() == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null)
            return new ResponseResult().errorResult(AppHttpCodeEnum.NEED_LOGIN);

        //3.删除
        mongoTemplate.remove(Query.query(Criteria.where("userId").is(user.getId())
                                .and("id").is(dto.getId())), ApUserSearch.class);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
