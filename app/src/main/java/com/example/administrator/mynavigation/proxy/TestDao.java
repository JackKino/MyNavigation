package com.example.administrator.mynavigation.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class TestDao {
    public  void main(String[] args) {
        //创建我们的代理类
         Dao dao=new DaoImp();
        InvocationHandler invocationHandler=new DynamicProxy(dao);
        Class[] interfaces = { Dao.class };
        Dao d = (Dao) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                interfaces, invocationHandler);
        System.out.println(d.show2());


    }
}
