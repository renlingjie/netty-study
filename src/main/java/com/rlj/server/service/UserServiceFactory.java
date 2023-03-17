package com.rlj.server.service;

public abstract class UserServiceFactory {

    private static UserService userService = new UserServiceImpl();

    public static UserService getUserService() {
        return userService;
    }
}
