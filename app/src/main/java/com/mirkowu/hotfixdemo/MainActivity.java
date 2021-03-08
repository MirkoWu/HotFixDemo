package com.mirkowu.hotfixdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.mirkowu.hotfixdemo.hotfix.SdkDexClassLoader;

import java.io.File;
import java.security.Permissions;
import java.security.acl.Permission;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void OnClick(View view) {
        int i = 1 / 0;
        Toast.makeText(this, "这个是有bug的版本" + i, Toast.LENGTH_SHORT).show();//dx --dex --output=D:\test\classes2.dex D:\test
//        Toast.makeText(this, "这个是修复的版本" + i, Toast.LENGTH_SHORT).show();
    }

    public void OnClickFixed(View view) {
        String[] READ = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(READ, 1);
            }
        }
        SdkDexClassLoader.loadFixedDex(this, new File(Environment.getExternalStorageDirectory(), "PlanLife"));
//        SdkDexClassLoader.loadFixedDex(this,  getExternalFilesDir(null));
    }
}