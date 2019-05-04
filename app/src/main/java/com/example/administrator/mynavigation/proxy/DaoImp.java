package com.example.administrator.mynavigation.proxy;

public class DaoImp implements Dao {
    //代理类
    @Override
    public void show() {
        System.out.println("我是show()");
    }

    @Override
    public Object show2() {
        System.out.println("我是show2()");
        return "111";
    }
}
