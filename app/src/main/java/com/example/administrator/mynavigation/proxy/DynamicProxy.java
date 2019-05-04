package com.example.administrator.mynavigation.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DynamicProxy implements InvocationHandler {

    Dao dao = null;
    public DynamicProxy(Dao dao) {
        super();
        this.dao = dao;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("代理开始");
        Object result = method.invoke(dao);
        System.out.println("代理结束");
        return null;
    }
}
