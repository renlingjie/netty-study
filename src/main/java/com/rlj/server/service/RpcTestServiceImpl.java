package com.rlj.server.service;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-26
 */
public class RpcTestServiceImpl implements RpcTestService{

    @Override
    public String sayHello(String name) {
        int i = 1/0;
        return "你好" + name;
    }
}
