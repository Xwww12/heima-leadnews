package com.heima.common.constants;

public class ArticleConstants {
    // 查询更多
    public static final Short LOADTYPE_LOAD_MORE = 1;

    // 查询最新
    public static final Short LOADTYPE_LOAD_NEW = 2;

    // 查询所有
    public static final String DEFAULT_TAG = "__all__";

    // kafka中文章的topic
    public static final String ARTICLE_ES_SYNC_TOPIC = "article.es.sync.topic";

    public static final Integer HOT_ARTICLE_LIKE_WEIGHT = 3;
    public static final Integer HOT_ARTICLE_COMMENT_WEIGHT = 5;
    public static final Integer HOT_ARTICLE_COLLECTION_WEIGHT = 8;

    public static final String HOT_ARTICLE_FIRST_PAGE = "hot_article_first_page_";
}