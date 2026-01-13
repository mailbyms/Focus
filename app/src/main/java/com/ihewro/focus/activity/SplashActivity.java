package com.ihewro.focus.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.blankj.ALog;
import com.ihewro.focus.R;
import com.ihewro.focus.task.AppStartTask;
import com.ihewro.focus.task.listener.TaskListener;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.setTheme(R.style.AppTheme);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

        setContentView(R.layout.activity_welcome);

        Intent intent = getIntent();

        if (!isTaskRoot()) {
            finish();
            return;
        }

        ImageView imageview= findViewById(R.id.imageView);
        AnimatedVectorDrawable animatedVectorDrawable =  ((AnimatedVectorDrawable) imageview.getDrawable());
        animatedVectorDrawable.start();

        //跳转到 {@link MainActivity}
        new AppStartTask(new TaskListener() {
            @Override
            public void onFinish(String jsonString) {
                finish();
                MainActivity.activityStart(SplashActivity.this);
            }
        }).execute();
    }
}
