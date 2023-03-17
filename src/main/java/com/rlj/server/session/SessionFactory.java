package com.rlj.server.session;

public abstract class SessionFactory {
    // 单例模式，大家都用这个Session，不能每次用的时候都创建，那样Session保存的信息就会被清空
    private static Session session = new SessionImpl();

    public static Session getSession() {
        return session;
    }
}
