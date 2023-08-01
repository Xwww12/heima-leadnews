package com.heima.article.test;

import com.heima.article.ArticleApplication;
import com.heima.article.service.HotArticleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class TestHotArticleService {

    @Resource
    private HotArticleService hotArticleService;

    @Test
    public void test() {
        hotArticleService.computeHotArticle();
    }
}
