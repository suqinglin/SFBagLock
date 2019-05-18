package com.nexless.sfbaglock.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.nexless.sfbaglock.bean.ProjectInfo;

import java.util.List;

/**
 * @date: 2019/5/5
 * @author: su qinglin
 * @description:
 */
public class ProjectAdapter extends BaseAdapter {

    private Context context;
    private List<ProjectInfo> data;

    public ProjectAdapter(Context context, List<ProjectInfo> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ProjectInfo getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }


}
