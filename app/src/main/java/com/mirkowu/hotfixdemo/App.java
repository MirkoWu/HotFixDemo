package com.mirkowu.hotfixdemo;

import android.app.Application;
import android.os.Environment;

import com.mirkowu.hotfixdemo.hotfix.SdkDexClassLoader;

import java.io.File;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SdkDexClassLoader.loadFixedDex(this, new File(Environment.getExternalStorageDirectory(), "PlanLife"));
    }
}
