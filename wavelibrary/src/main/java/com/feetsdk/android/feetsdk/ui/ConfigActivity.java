package com.feetsdk.android.feetsdk.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.feetsdk.android.FeetSdk;
import com.feetsdk.android.R;
import com.feetsdk.android.common.utils.AppManager;
import com.zhy.autolayout.AutoLayoutActivity;

public class ConfigActivity extends AutoLayoutActivity implements View.OnClickListener {

    private View cancel;
    private View thirty;
    private View sixty;
    private View oneHundred;
    private View twoHundred;
    private View sixtyIcon;
    private View thirtyIcon;
    private View oneHundredIcon;
    private View twoHundredIcon;
    private View changeSinger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_config);
        AppManager.getAppManager().addActivity(this);
        initView();
        initListener();
    }

    private void initListener() {
        cancel.setOnClickListener(this);
        thirty.setOnClickListener(this);
        sixty.setOnClickListener(this);
        oneHundred.setOnClickListener(this);
        twoHundred.setOnClickListener(this);
        changeSinger.setOnClickListener(this);
    }

    private void initView() {
        cancel = findViewById(R.id.cancel);
        thirty = findViewById(R.id.thirty_minute);
        sixty = findViewById(R.id.sixth_minute);
        oneHundred = findViewById(R.id.one_hundred_minute);
        twoHundred = findViewById(R.id.two_hundred_minute);
        changeSinger = findViewById(R.id.change_singer);
        thirtyIcon = findViewById(R.id.thirty_icon);
        sixtyIcon = findViewById(R.id.sixty_icon);
        oneHundredIcon = findViewById(R.id.one_hundred_icon);
        twoHundredIcon = findViewById(R.id.two_hundred_icon);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.cancel){
            finish();
        }
        if(id == R.id.thirty_minute){
            resetMinute(30);
        }
        if(id == R.id.sixth_minute){
            resetMinute(60);
        }
        if(id == R.id.one_hundred_minute){
            resetMinute(120);
        }
        if(id == R.id.two_hundred_minute){
            resetMinute(240);
        }
        if(id == R.id.change_singer){
            startActivity(new Intent(this,ChooseSingerActivity.class));
        }
    }

    private void resetMinute(int type){
        switch (type) {
            case 30:
                thirtyIcon.setVisibility(View.VISIBLE);
                sixtyIcon.setVisibility(View.INVISIBLE);
                oneHundredIcon.setVisibility(View.INVISIBLE);
                twoHundredIcon.setVisibility(View.INVISIBLE);
                FeetSdk.getInstance().setMusicLibrarySize(this,10);
                break;
            case 60:
                thirtyIcon.setVisibility(View.INVISIBLE);
                sixtyIcon.setVisibility(View.VISIBLE);
                oneHundredIcon.setVisibility(View.INVISIBLE);
                twoHundredIcon.setVisibility(View.INVISIBLE);
                FeetSdk.getInstance().setMusicLibrarySize(this,20);
                break;
            case 120:
                thirtyIcon.setVisibility(View.INVISIBLE);
                sixtyIcon.setVisibility(View.INVISIBLE);
                oneHundredIcon.setVisibility(View.VISIBLE);
                twoHundredIcon.setVisibility(View.INVISIBLE);
                FeetSdk.getInstance().setMusicLibrarySize(this,30);
                break;
            case 240:
                thirtyIcon.setVisibility(View.INVISIBLE);
                sixtyIcon.setVisibility(View.INVISIBLE);
                oneHundredIcon.setVisibility(View.INVISIBLE);
                twoHundredIcon.setVisibility(View.VISIBLE);
                FeetSdk.getInstance().setMusicLibrarySize(this,40);
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        Intent intent1 = new Intent(FWProxy.UPDATE_DB);
        sendBroadcast(intent1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishAllActivity();
    }
}
