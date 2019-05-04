package com.example.administrator.mynavigation;

import android.app.Application;

import com.inuker.bluetooth.library.BluetoothContext;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        BluetoothContext.set(this);
    }

    private static MyApplication instance;

    public static Application getInstance() {
        return instance;
    }
}
