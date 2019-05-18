package com.nexless.sfbaglock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.nexless.ccommble.util.CommUtil;
import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.R;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @date: 2019/5/9
 * @author: su qinglin
 * @description:
 */
public class SplashActivity extends BaseActivity {

    private Disposable timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView tvVersion = findViewById(R.id.tv_splash_version);
        TextView tvDate = findViewById(R.id.tv_splash_date);
        tvVersion.setText("Ver. " + CommUtil.getVersionName(this));
        tvDate.setText(AppConstant.CURRENT_DATE);
        timer = Observable.timer(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    startActivity(new Intent(SplashActivity.this, ProjectActivity.class));
                    SplashActivity.this.finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null && !timer.isDisposed()) {
            timer.dispose();
        }
    }
}
