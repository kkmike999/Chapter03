package com.dodola.alloctrack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100;

    @BindView(R.id.dump_log)
    private Button dumpLogBtn;

    private AllocTracker tracker = new AllocTracker();
    private File         externalReportPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            initExternalReportPath();
        }
        tracker.initForArt(BuildConfig.VERSION_CODE, 5000);//从 start 开始触发到5000的数据就 dump 到文件中
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initExternalReportPath();
    }

    @OnClick(R.id.dump_log)
    public void onDumpLog(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                tracker.dumpAllocationDataInLog();
            }
        }).start();
    }

    @OnClick(R.id.btn_start)
    public void onStart(View view) {
        tracker.startAllocationTracker();
        dumpLogBtn.setEnabled(true);
    }

    @OnClick(R.id.btn_stop)
    public void onStop(View view) {
        tracker.stopAllocationTracker();
        dumpLogBtn.setEnabled(false);
    }

    @OnClick(R.id.gen_obj)
    public void onGenObj(View view) {
        for (int i = 0; i < 1000; i++) {
            Message msg = new Message();
            msg.what = i;
        }
    }

    private void initExternalReportPath() {
        externalReportPath = new File(Environment.getExternalStorageDirectory(), "crashDump");
        if (!externalReportPath.exists()) {
            externalReportPath.mkdirs();
        }
        tracker.setSaveDataDirectory(externalReportPath.getAbsolutePath());
    }
}
