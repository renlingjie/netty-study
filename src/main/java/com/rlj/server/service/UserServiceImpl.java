package com.rlj.server.service;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-18
 */
public class UserServiceImpl implements UserService{
    @Override
    public boolean login(String username, String password) {
        if (username != null && !username.trim().equals("")
                && password != null && !password.trim().equals("")){
            return true;
        } else {
            return false;
        }
    }
}
