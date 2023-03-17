package com.rlj.server.service;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-18
 */
public interface UserService {
    // 登录。成功返回true，失败返回false
    boolean login(String username,String password);
}
