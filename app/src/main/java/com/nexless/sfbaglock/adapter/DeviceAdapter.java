package com.nexless.sfbaglock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexless.sfbaglock.R;
import com.nexless.sfbaglock.bean.ProductInfo;
import java.util.List;

/**
 * @date: 2019/5/5
 * @author: su qinglin
 * @description:
 */
public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<ProductInfo> data;
    private int selectItem = -1;

    public DeviceAdapter(Context context, List<ProductInfo> data) {
        this.context = context;
        this.data = data;
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ProductInfo getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null && data.size() > 0) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_device, null);
            viewHolder.rlContainer = convertView.findViewById(R.id.rl_device_item_container);
            viewHolder.tvIndex = convertView.findViewById(R.id.tv_device_item_index);
            viewHolder.tvSn = convertView.findViewById(R.id.tv_device_item_sn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvIndex.setText(String.valueOf(position));
        viewHolder.tvSn.setText(getItem(position).getSN());
        if (selectItem == position) {
            viewHolder.rlContainer.setBackgroundColor(0x80CCCCCC);
        } else {
            viewHolder.rlContainer.setBackgroundColor(0x00FFFFFF);
        }
        return convertView;
    }


    private class ViewHolder {
        private RelativeLayout rlContainer;
        private TextView tvIndex;
        private TextView tvSn;
    }

}
