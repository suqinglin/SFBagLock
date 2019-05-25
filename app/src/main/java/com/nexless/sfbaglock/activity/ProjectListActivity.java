package com.nexless.sfbaglock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.R;
import com.nexless.sfbaglock.adapter.PAdapter;
import com.nexless.sfbaglock.adapter.PViewHolder;
import com.nexless.sfbaglock.bean.ProjectInfo;
import com.nexless.sfbaglock.bean.ProjectsResponse;
import com.nexless.sfbaglock.bean.TResponse;
import com.nexless.sfbaglock.http.RxHelper;
import com.nexless.sfbaglock.http.ServiceFactory;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * @date: 2019/5/23
 * @author: su qinglin
 * @description:
 */
public class ProjectListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = ProjectListActivity.class.getSimpleName();
    private ListView mListView;
    private TextView mTvUserId;
    private TextView mTvUserKey;
    private List<ProjectsResponse.ProjectBean> mProjectList = new ArrayList<>();
    private PAdapter<ProjectsResponse.ProjectBean> mAdapter;
    private long mExitTime;
    private String mUserId;
    private String mUserKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        mTvUserId = findViewById(R.id.tv_project_list_user_id);
        mTvUserKey = findViewById(R.id.tv_project_list_user_key);
        mListView = findViewById(R.id.lv_project_list);
        mAdapter = new PAdapter<ProjectsResponse.ProjectBean>(this, mProjectList, R.layout.item_project) {
            @Override
            public void convert(PViewHolder helper, ProjectsResponse.ProjectBean item, int
                    position) {
                TextView tvProject = helper.getView(R.id.tv_project_item_project_name);
                tvProject.setText(item.getProjectNo() + "(" + item.getStart() + "~" + item.getEnd() + ")");
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        getProjectList();
    }

    private void getProjectList() {
        mDialogHelper.showProgressDialog();
        Observable<TResponse<ProjectsResponse>> observable = ServiceFactory.getInstance().getApiService().getProjects();
        RxHelper.getInstance().sendRequest(TAG, observable, projectsResponseTResponse -> {
            mDialogHelper.dismissProgressDialog();
            if (projectsResponseTResponse.isSuccess()) {
                ProjectsResponse result = projectsResponseTResponse.data;
                mUserId = result.getUserId();
                mUserKey = result.getUserKey();
                mTvUserKey.setText("User Key:" + mUserKey);
                mTvUserId.setText("User Id:" + mUserId);
                mProjectList.clear();
                mProjectList.addAll(result.getProjects());
                mAdapter.notifyDataSetChanged();
                saveProjects(mProjectList);
            } else {
                showToast(projectsResponseTResponse.message);
            }
        }, throwable -> {
            mDialogHelper.dismissProgressDialog();
            showToast(RxHelper.getInstance().getErrorInfo(throwable));
        });
    }

    /**
     * 将Project保存到本地数据库
     * @param projects
     */
    private void saveProjects(List<ProjectsResponse.ProjectBean> projects) {
        for (ProjectsResponse.ProjectBean projectBean: projects) {
            ProjectInfo project = new ProjectInfo();
            project.setUserId(mUserId);
            project.setUserKey(mUserKey);
            project.setProjectName(projectBean.getProjectNo());
            project.setSnEnd(Long.parseLong(projectBean.getEnd()));
            project.setSnStart(Long.parseLong(projectBean.getStart()));
            project.setProjectNo(projectBean.getProjectNo());
            project.saveOrUpdate("projectNo = ?", projectBean.getProjectNo());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ProjectsResponse.ProjectBean projectBean = (ProjectsResponse.ProjectBean) parent.getItemAtPosition(position);
        ProjectInfo project = new ProjectInfo();
        project.setUserId(mUserId);
        project.setUserKey(mUserKey);
        project.setProjectName(projectBean.getProjectNo());
        project.setSnEnd(Long.parseLong(projectBean.getEnd()));
        project.setSnStart(Long.parseLong(projectBean.getStart()));
        project.setProjectNo(projectBean.getProjectNo());
        Intent intent = new Intent(this, ProductActivity.class);
        intent.putExtra(AppConstant.EXTRA_PROJECT, project);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                mExitTime = System.currentTimeMillis();
                showToast("再按一次退出系统");
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
