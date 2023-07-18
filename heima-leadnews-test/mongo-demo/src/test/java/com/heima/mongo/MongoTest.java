package com.heima.mongo;

import com.heima.mongo.pojos.ApAssociateWords;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@SpringBootTest(classes = MongoApplication.class)
@RunWith(SpringRunner.class)
public class MongoTest {
    @Resource
    private MongoTemplate mongoTemplate;

    @Test
    public void saveTest() {
        ApAssociateWords apAssociateWords = new ApAssociateWords();
        apAssociateWords.setAssociateWords("test");
        apAssociateWords.setCreatedTime(new Date());
        mongoTemplate.save(apAssociateWords);
    }

    @Test
    public void findByIdTest(){
        ApAssociateWords apAssociateWords = mongoTemplate.findById("64b65971d91a9f14ddf8da6a", ApAssociateWords.class);
        System.out.println(apAssociateWords);
    }

    @Test
    public void findByWordsTest() {
        Query query = Query.query(Criteria.where("associateWords").is("test"))
                .with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApAssociateWords> apAssociateWordsList = mongoTemplate.find(query, ApAssociateWords.class);
        System.out.println(apAssociateWordsList);
    }

    @Test
    public void testDel() {
        Query query = Query.query(Criteria.where("associateWords").is("test"));
        mongoTemplate.remove(query, ApAssociateWords.class);
    }
}
