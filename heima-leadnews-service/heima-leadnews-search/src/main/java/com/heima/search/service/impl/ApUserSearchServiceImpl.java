package com.heima.search.service.impl;

import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
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

            query = Query.query(Criteria.where("userId").is(userId));
            query.with(Sort.by(Sort.Direction.DESC, "createTime"));
            List<ApUserSearch> apUserSearchList = mongoTemplate.find(query, ApUserSearch.class);
            if (apUserSearchList == null || apUserSearchList.size() < 10)
                mongoTemplate.save(apUserSearch);
            else {
                ApUserSearch last = apUserSearchList.get(apUserSearchList.size() - 1);
                mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(last.getId())), apUserSearch);
            }
        }
    }
}
