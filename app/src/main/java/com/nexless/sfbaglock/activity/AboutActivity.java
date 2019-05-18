package com.nexless.sfbaglock.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.nexless.ccommble.util.CommUtil;
import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.R;

/**
 * @date: 2019/5/9
 * @author: su qinglin
 * @description:
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView tvVersion = findViewById(R.id.tv_about_version);
        TextView tvDate = findViewById(R.id.tv_about_date);
        tvVersion.setText("Ver. " + CommUtil.getVersionName(this));
        tvDate.setText(AppConstant.CURRENT_DATE);
    }
}
