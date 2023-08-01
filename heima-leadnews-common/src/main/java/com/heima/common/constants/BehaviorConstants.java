package com.heima.common.constants;

/**
 * 常量类
 */
public class BehaviorConstants {

    // 文章点赞的所有用户redis前缀  behavior:like:文章id
    public static final String CACHE_BEHAVIOR_LIKE = "behavior:like:";

    // 文章点赞数redis前缀  behavior:like:count:文章id
    public static final String CACHE_BEHAVIOR_LIKE_COUNT = "behavior:like:count:";

    // 文章阅读数redis前缀  behavior:read:文章id
    public static final String CACHE_BEHAVIOR_READ = "article:viewCount";

    // 文章不喜欢的所有用户redis前缀  behavior:read:文章id
    public static final String CACHE_BEHAVIOR_UNLIKE = "behavior:unlike:";

    // 订阅
    public static final String CACHE_BEHAVIOR_FOLLOW = "behavior:follow:";

    // 收藏
    public static final String CACHE_BEHAVIOR_COLLECTION = "behavior:collection:";
}
