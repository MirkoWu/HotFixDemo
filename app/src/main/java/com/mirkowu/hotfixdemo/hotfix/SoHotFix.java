package com.mirkowu.hotfixdemo.hotfix;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.Field;

public class SoHotFix {
    private static String[] primaryCpuAbis;//首选CPU类型

    public static String[] getPrimaryCpuAbis(Context context) {
        try {
            //sdk>=21下， 直接反射拿到ApplicationInfo对象的primaryCpuAbi即可
            //sdk<21下， 由于此时不支持64位， 所以直接把Build.CPU_ABI, Build.CPU_ABI2作为primaryCpuAbi即可 。
//            Context context;
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//sdk>=21
                    Field field = ApplicationInfo.class.getDeclaredField("primaryCpuAbi");
                    field.setAccessible(true);

                    String cpuAbi = (String) field.get(appInfo);
                    primaryCpuAbis = new String[]{cpuAbi};

                } else {//sdk<21
                    primaryCpuAbis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
                }
            }
        } catch (IllegalAccessException | PackageManager.NameNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        return primaryCpuAbis;
    }

    public static void load(String name){
        System.load(name);
    }
}
