package com.nexless.sfbaglock.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nexless.ccommble.scan.NexlessBluetoothScanner;
import com.nexless.ccommble.util.CommHandler;
import com.nexless.ccommble.util.CommLog;
import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.R;
import com.nexless.sfbaglock.adapter.PAdapter;
import com.nexless.sfbaglock.adapter.PViewHolder;
import com.nexless.sfbaglock.bean.DeviceBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @date: 2019/5/6
 * @author: su qinglin
 * @description: 蓝牙搜索设备
 */
public class SearchDeviceActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "SearchDeviceActivity";
    private static final int MSG_UPDATE_LIST = 0x0001;
    private List<DeviceBean> mScanDeviceList = new ArrayList<>();
    private PAdapter<DeviceBean> mAdapter;
    private ListView mLvScanDevice;
    private NexlessBluetoothScanner mBluetoothScanner;
    private Map<String, BluetoothDevice> mFoundDevicesMap = new HashMap<>(); // 搜索到的设备

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);
        mLvScanDevice = findViewById(R.id.comm_listview_list);
        mAdapter = new PAdapter<DeviceBean>(this, mScanDeviceList, R.layout.item_project) {
            @Override
            public void convert(PViewHolder helper, DeviceBean item, int position) {

                TextView tvDeviceName = helper.getView(R.id.tv_project_item_project_name);
                tvDeviceName.setText(item.bluetoothName);
            }
        };
        mLvScanDevice.setOnItemClickListener(this);
        mLvScanDevice.setAdapter(mAdapter);
        mBluetoothScanner = NexlessBluetoothScanner.getIntance(this);
        mBluetoothScanner.startScan(mScannerCallback);
        findViewById(R.id.btn_search_device_refresh).setOnClickListener(
                v -> mBluetoothScanner.startScan(mScannerCallback));
    }

    private NexlessBluetoothScanner.NexlessScannerCallBack mScannerCallback = new NexlessBluetoothScanner.NexlessScannerCallBack() {
        @Override
        public void onScannerResultCallBack(BluetoothDevice device, int rssi) {
            if (device != null) {
                addDevice(device, rssi);
            }
        }

        @Override
        public void onScanFinished() {
            if (mBluetoothScanner != null && mBluetoothScanner.isScanning()) {
                mBluetoothScanner.stopScan();
            }
        }

        @Override
        public void onScanStarted() {
        }

        @Override
        public void onScanFailed(NexlessBluetoothScanner.ScanFailType type) {
            showToast("蓝牙不可用");
        }
    };

    private void addDevice(BluetoothDevice device, int rssi) {
        if (!mFoundDevicesMap.containsKey(device.getAddress()) && !TextUtils.isEmpty(device.getName()) && device.getName().contains("SFU_")) {
            mFoundDevicesMap.put(device.getAddress(), device);
//            DeviceDB deviceDB = LitePal.where("devMac = ?", device.getAddress()).findFirst(DeviceDB.class);
//            mScanDeviceList.add(new DeviceBean(device, rssi, deviceDB != null));
//            Collections.sort(mScanDeviceList);
            DeviceBean deviceBean = new DeviceBean(device, rssi, false);
            Message message = Message.obtain();
            message.obj = deviceBean;
            message.what = MSG_UPDATE_LIST;
            mCommHandler.sendMessage(message);
        }
    }

    private CommHandler mCommHandler = new CommHandler(new CommHandler.MessageHandler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_LIST:
                    DeviceBean deviceBean = (DeviceBean) msg.obj;
                    mScanDeviceList.add(deviceBean);
                    Collections.sort(mScanDeviceList);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    });

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceBean device = (DeviceBean) parent.getItemAtPosition(position);
        if (device != null) {
            Intent intent = getIntent();
            intent.putExtra(AppConstant.EXTRA_DEVICE, device);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothScanner != null) {
            mBluetoothScanner.stopScan();
        }
    }

}
