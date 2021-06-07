package com.orhanobut.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.DiskTxtLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
  private int requestPermissionCode = 100;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestPermissionCode);
    } else {
      testLogger();
    }
  }

  private void testLogger() {
    Logger.addLogAdapter(new DiskTxtLogAdapter());
    Log.d("Tag", "I'm a log which you don't see easily, hehe");
    Log.d("json content", "{ \"key\": 3, \n \"value\": something}");
    Log.d("error", "There is a crash somewhere or any warning");
    Logger.d("message");
    Logger.w("no thread info and only 1 method");
    Logger.w("test write log to disk with txt format");
    Logger.i("no thread info and method info");
    Logger.t("tag").e("Custom tag for only one use");
    Logger.json("{ \"key\": 3, \"value\": something}");
    Logger.d(Arrays.asList("foo", "bar"));
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    map.put("key1", "value2");
    Logger.d(map);
    Logger.w("my log message with my tag");
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    for (int i = 0; i < permissions.length; i++) {
      if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i]) && PackageManager.PERMISSION_GRANTED == grantResults[i]) {
        testLogger();
      }
    }
  }
}
