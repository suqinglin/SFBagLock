package com.nexless.sfbaglock.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.R;
import com.nexless.sfbaglock.adapter.PAdapter;
import com.nexless.sfbaglock.adapter.PViewHolder;
import com.nexless.sfbaglock.bean.ProjectInfo;
import com.nexless.sfbaglock.view.AppTitleBar;

import org.litepal.LitePal;

import java.util.List;

public class ProjectActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEdtProjectName;
    private EditText mEdtUserKey;
    private EditText mEdtWeChart;
    private EditText mEdtSnStart;
    private EditText mEdtSnEnd;
    private EditText mEdtUserId;
    private Button btnClear;
    private Button btnLoad;
    private Button btnSave;
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        AppTitleBar titleBar = findViewById(R.id.titlebar);
        mEdtProjectName = findViewById(R.id.edt_project_name);
        mEdtUserKey = findViewById(R.id.edt_project_user_key);
        mEdtWeChart = findViewById(R.id.edt_project_we_chart);
        mEdtSnStart = findViewById(R.id.edt_project_sn_start);
        mEdtUserId = findViewById(R.id.edt_project_user_id);
        mEdtSnEnd = findViewById(R.id.edt_project_sn_end);
        btnClear = findViewById(R.id.btn_project_clear);
        btnLoad = findViewById(R.id.btn_project_load);
        btnSave = findViewById(R.id.btn_project_save);

        btnClear.setOnClickListener(this);
        btnLoad.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        titleBar.setRightListener(this);
//        try {
//            byte[] appKeyBuf = BaglockUtils.getAppKey(mac, sn, userKey);
//            Log.d("ProjectActivity", "appKey:" + Hex.encodeHexString(appKeyBuf).toUpperCase());
//        } catch (DecoderException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_project_clear:
                clear();
                break;
            case R.id.btn_project_load:
                List<ProjectInfo> projectList = LitePal.findAll(ProjectInfo.class);
                if (projectList != null && projectList.size() > 0) {
                    showProjectList(projectList);
                } else {
                    showToast("暂无可选项目，请先添加！");
                }
                break;
            case R.id.apptitlebar_btn_right:
            case R.id.btn_project_save:
                String projectName = mEdtProjectName.getEditableText().toString();
                String weChart = mEdtWeChart.getEditableText().toString();
                String userKey = mEdtUserKey.getEditableText().toString();
                String userId = mEdtUserId.getEditableText().toString();
                String snStart = mEdtSnStart.getEditableText().toString();
                String snEnd = mEdtSnEnd.getEditableText().toString();
                if (TextUtils.isEmpty(projectName)) {
                    showToast("请输入Project");
                    return;
                }
                if (TextUtils.isEmpty(userId)) {
                    showToast("请输入UserId");
                    return;
                }
                if (TextUtils.isEmpty(userKey)) {
                    showToast("请输入UserKey");
                    return;
                }
                if (TextUtils.isEmpty(weChart)) {
                    showToast("请输入WeChart");
                    return;
                }
                if (TextUtils.isEmpty(snStart)) {
                    showToast("请输入SnStart");
                    return;
                }
                if (TextUtils.isEmpty(snEnd)) {
                    showToast("请输入SnEnd");
                    return;
                }
                ProjectInfo project = new ProjectInfo();
                project.setProjectName(projectName);
                project.setUserKey(userKey);
                project.setWeChart(weChart);
                project.setUserId(userId);
                project.setSnStart(Long.valueOf(snStart));
                project.setSnEnd(Long.valueOf(snEnd));
                project.saveOrUpdate("userKey = ?", userKey);
                project = LitePal
                        .where("userKey = ?", userKey)
                        .findFirst(ProjectInfo.class);
                Intent intent = new Intent(this, ProductActivity.class);
                intent.putExtra(AppConstant.EXTRA_PROJECT, project);
                startActivity(intent);
                break;
        }
    }

    private void showProjectList(final List<ProjectInfo> projectList) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Project");
        builder.setSingleChoiceItems(new PAdapter<ProjectInfo>(this, projectList, R.layout.item_project) {
            @Override
            public void convert(PViewHolder helper, ProjectInfo item, int position) {
                TextView tvProjectName = helper.getView(R.id.tv_project_item_project_name);
                tvProjectName.setText(item.getProjectName() + "(" + item.getUserKey() + ")");
            }
        }, 0, (dialog, which) -> {
            dialog.cancel();
            ProjectInfo project = projectList.get(which);
            mEdtProjectName.setText(project.getProjectName());
            mEdtUserKey.setText(project.getUserKey());
            mEdtWeChart.setText(project.getWeChart());
            mEdtSnStart.setText(project.getSnStart() + "");
            mEdtSnEnd.setText(project.getSnEnd() + "");
            mEdtUserId.setText(project.getUserId());
            Intent intent = new Intent(ProjectActivity.this, ProductActivity.class);
            intent.putExtra(AppConstant.EXTRA_PROJECT, project);
            startActivity(intent);
        });
        builder.show();
    }

    private void clear() {
        mEdtProjectName.setText("");
        mEdtUserKey.setText("");
        mEdtWeChart.setText("");
        mEdtSnStart.setText("");
        mEdtSnEnd.setText("");
        mEdtUserId.setText("");
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("程序即将退出，请确认数据已保存！")
                .setPositiveButton("确定", (dialog, which) -> System.exit(0))
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                mExitTime = System.currentTimeMillis();
                showToast("再按一次推出系统");
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
