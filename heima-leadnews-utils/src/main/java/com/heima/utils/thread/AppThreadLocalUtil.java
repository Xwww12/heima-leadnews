package com.heima.utils.thread;

import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.pojos.WmUser;

public class AppThreadLocalUtil {
    private static final ThreadLocal<ApUser> App_USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 存放登录用户
     * @param apUser
     */
    public static void setUser(ApUser apUser) {
        App_USER_THREAD_LOCAL.set(apUser);
    }

    /**
     * 获取登录用户
     * @return
     */
    public static ApUser getUser() {
        return App_USER_THREAD_LOCAL.get();
    }

    /**
     * 清除用户
     */
    public static void clear() {
        App_USER_THREAD_LOCAL.remove();
    }
}
