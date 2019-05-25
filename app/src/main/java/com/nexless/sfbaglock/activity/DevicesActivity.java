package com.nexless.sfbaglock.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nexless.ccommble.codec.DecoderException;
import com.nexless.ccommble.codec.binary.Hex;
import com.nexless.ccommble.conn.BluetoothListener;
import com.nexless.ccommble.conn.ConnectionHelper;
import com.nexless.ccommble.data.BaglockUtils;
import com.nexless.ccommble.data.Encrypt;
import com.nexless.ccommble.data.model.LockResult;
import com.nexless.ccommble.util.BleStatusUtil;
import com.nexless.ccommble.util.CommLog;
import com.nexless.ccommble.util.CommUtil;
import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.R;
import com.nexless.sfbaglock.adapter.DeviceAdapter;
import com.nexless.sfbaglock.bean.ProductInfo;
import com.nexless.sfbaglock.bean.ProjectInfo;
import com.nexless.sfbaglock.view.AppTitleBar;

import org.jetbrains.annotations.Nullable;
import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @date: 2019/5/22
 * @author: su qinglin
 * @description: 设备页面
 */
public class DevicesActivity extends BaseActivity implements View.OnClickListener, AdapterView
        .OnItemClickListener {

    private ProjectInfo mProject;
    private List<ProductInfo> mDeviceList;
    private ProductInfo mSelectDevice;
    private int mSelectIndex = -1;
    private DeviceAdapter mAdapter;
    private TextView mTvProject;
    private ListView mListView;
    private TextView mTvSn;
    private TextView mTvMac;
    private TextView mTvCnt;
    private TextView mTvVolt;
    private long mCnt;
    private DecimalFormat mDfBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        mProject = getIntent().getParcelableExtra(AppConstant.EXTRA_PROJECT);

        AppTitleBar titleBar = findViewById(R.id.titlebar);
        titleBar.setRightListener(this);

        mTvProject = findViewById(R.id.tv_devices_project);
        mListView = findViewById(R.id.lv_devices_list);
        mTvSn = findViewById(R.id.tv_devices_sn);
        mTvMac = findViewById(R.id.tv_devices_mac);
        mTvCnt = findViewById(R.id.tv_devices_cnt);
        mTvVolt = findViewById(R.id.tv_devices_volt);
        findViewById(R.id.btn_devices_delete).setOnClickListener(this);
        findViewById(R.id.btn_devices_test).setOnClickListener(this);
        mTvProject.setText("Project:" + mProject.getProjectName());
        mDeviceList = LitePal
                .where("projectNo = ?", mProject.getProjectNo())
                .find(ProductInfo.class);
        mAdapter = new DeviceAdapter(this, mDeviceList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mDfBattery = new DecimalFormat("#.0");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.setSelectItem(position);
        mAdapter.notifyDataSetChanged();
        mSelectIndex = position;
        mSelectDevice = (ProductInfo) parent.getItemAtPosition(position);
        mCnt = mSelectDevice.getCNT();
        mTvSn.setText("SN:" + mSelectDevice.getSN());
        mTvMac.setText("MAC:" + mSelectDevice.getMac());
        mTvCnt.setText("CNT:" + mSelectDevice.getCNT());
        mTvVolt.setText("Volt:");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.apptitlebar_btn_right:
                if (mSelectDevice == null) {
                    showToast("请选择设备！");
                    return;
                }
                Intent intent = new Intent(this, LogsActivity.class);
                intent.putExtra(AppConstant.EXTRA_PROJECT, mProject);
                intent.putExtra(AppConstant.EXTRA_DEVICE, mSelectDevice);
                startActivity(intent);
                break;
            case R.id.btn_devices_delete:
                if (mSelectDevice == null) {
                    showToast("请选择设备！");
                    return;
                }
                new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("您确定要删除当前设备")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", (dialog, which) -> {
                            LitePal.deleteAll(ProductInfo.class, "mac = ?", mSelectDevice.getMac());
                            reset();
                        })
                        .show();
                break;
            case R.id.btn_devices_test:
                if (mSelectDevice == null) {
                    showToast("请选择设备！");
                    return;
                }
                try {
                    mCnt++;
                    test();
                } catch (DecoderException e) {
                    e.printStackTrace();
                    mDialogHelper.dismissProgressDialog();
                }
                break;
        }
    }

    /**
     * 重置
     */
    private void reset() {
        mSelectDevice = null;
        mSelectIndex = -1;
        mDeviceList.clear();
        mDeviceList.addAll(LitePal
                .where("projectNo = ?", mProject.getProjectNo())
                .find(ProductInfo.class));
        mAdapter.notifyDataSetChanged();
        mTvSn.setText("SN:");
        mTvMac.setText("MAC:");
        mTvCnt.setText("CNT:");
        mTvVolt.setText("Volt:");
    }

    /**
     * 测试
     * @throws DecoderException
     */
    private void test() throws DecoderException {

        mDialogHelper.showProgressDialog();
        byte[] sendData = Encrypt.openEncrypt(
                Long.valueOf(mSelectDevice.getSN()),
                mCnt,
                System.currentTimeMillis() / 1000,
                mSelectDevice.getMac().replace(":", ""),
                mProject.getUserId(),
                mProject.getUserKey());
        CommLog.logE("sendData:" + Hex.encodeHexString(sendData).toUpperCase());
        ConnectionHelper.getInstance().bleCommunication(
                mSelectDevice.getSN(),
                mSelectDevice.getMac(),
                null,
                sendData,
                false,
                new BluetoothListener() {

                    @Override
                    public void onDataChange(@Nullable byte[] data) {
                        mDialogHelper.dismissProgressDialog();
                        String result = Hex.encodeHexString(data).toUpperCase();
                        CommLog.logE("receiveData:" + result);
                        String resData = result.substring(2, 36);
                        try {
                            if (CommUtil.checkCrc(resData, result.substring(36))) {
                                LockResult lockResult = BaglockUtils.parseLockResult(result);
                                CommLog.logE("lockResult:" + lockResult.toString());
                                if (BleStatusUtil.RST_SUCC.equals(lockResult.getResult())) {
                                    ProductInfo product = LitePal.where("mac = ?", mSelectDevice.getMac()).findFirst(ProductInfo.class);
                                    if (product != null) {
                                        product.setCNT(mCnt);
                                        product.setTimeStamp(System.currentTimeMillis() / 1000);
                                        product.update(product.getId());
                                    }
                                    mDeviceList.get(mSelectIndex).setCNT(mCnt);
                                    mTvCnt.setText("CNT:" + String.valueOf(mCnt));
                                    mTvVolt.setText("Volt:" + mDfBattery.format(lockResult.getBattery() / 1000f) + "V");
                                } else {
                                    showToast(BleStatusUtil.getResultMsg(lockResult.getResult()));
                                }
                            } else {
                                showToast("crc校验失败");
                            }
                        } catch (DecoderException e) {
                            e.printStackTrace();
                            showToast("程序异常");
                        }
                    }

                    @Override
                    public void onConnStatusFail(int status) {
                        mDialogHelper.dismissProgressDialog();
                        showToast(BleStatusUtil.getConnectStatusMsg(status));
                    }

                    @Override
                    public void onConnStatusSucc(int status) {

                    }
                }, 0);
    }
}
