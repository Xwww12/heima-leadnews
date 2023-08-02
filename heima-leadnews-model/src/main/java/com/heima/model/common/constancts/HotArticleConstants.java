package com.heima.model.common.constancts;


public class HotArticleConstants {
    // 热门文章用户行为的kafka topic
    public static final String HOT_ARTICLE_SCORE_TOPIC="hot.article.score.topic";

    // 流式处理完用户行为后发送到的topic
    public static final String HOT_ARTICLE_INCR_HANDLE_TOPIC="hot.article.incr.handle.topic";
   
}