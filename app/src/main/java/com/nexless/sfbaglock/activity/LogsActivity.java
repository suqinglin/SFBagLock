package com.nexless.sfbaglock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.nexless.ccommble.codec.DecoderException;
import com.nexless.ccommble.codec.binary.Hex;
import com.nexless.ccommble.conn.BluetoothListener;
import com.nexless.ccommble.conn.ConnectionHelper;
import com.nexless.ccommble.data.BaglockUtils;
import com.nexless.ccommble.data.Encrypt;
import com.nexless.ccommble.data.model.LogResult;
import com.nexless.ccommble.util.BleStatusUtil;
import com.nexless.ccommble.util.CommLog;
import com.nexless.ccommble.util.CommUtil;
import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.R;
import com.nexless.sfbaglock.adapter.PAdapter;
import com.nexless.sfbaglock.adapter.PViewHolder;
import com.nexless.sfbaglock.bean.LogInfo;
import com.nexless.sfbaglock.bean.ProductInfo;
import com.nexless.sfbaglock.bean.ProjectInfo;
import com.nexless.sfbaglock.util.DateUtil;
import com.nexless.sfbaglock.view.AppTitleBar;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @date: 2019/5/22
 * @author: su qinglin
 * @description: 日志页面
 */
public class LogsActivity extends BaseActivity implements View.OnClickListener {

    private ProjectInfo mProject;
    private ProductInfo mDevice;
    private PAdapter<LogInfo> mAdapter;
    private List<LogInfo> mLogList = new ArrayList<>();
    private DecimalFormat mDfBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        mProject = getIntent().getParcelableExtra(AppConstant.EXTRA_PROJECT);
        mDevice = getIntent().getParcelableExtra(AppConstant.EXTRA_DEVICE);
        AppTitleBar titleBar = findViewById(R.id.titlebar);
        titleBar.setRightListener(this);
        TextView tvProject = findViewById(R.id.tv_logs_project);
        TextView tvSn = findViewById(R.id.tv_logs_sn);
        findViewById(R.id.btn_logs_get).setOnClickListener(this);
        ListView listView = findViewById(R.id.lv_logs_list);
        mAdapter = new PAdapter<LogInfo>(this, mLogList, R.layout.item_log) {
            @Override
            public void convert(PViewHolder helper, LogInfo item, int position) {
                TextView tvTime = helper.getView(R.id.tv_log_item_time);
                TextView tvMsg = helper.getView(R.id.tv_log_item_msg);
                TextView tvBattery = helper.getView(R.id.tv_log_item_battery);
                tvTime.setText(DateUtil.parseLongToString(item.getTimeStamp() * 1000, DateUtil.FORMAT_YY_MM_DD_HH_MM));
                tvMsg.setText(item.getContent());
                tvBattery.setText(item.getUserId());
            }
        };
        listView.setAdapter(mAdapter);

        tvProject.setText("Project:" + mProject.getProjectNo());
        tvSn.setText("S/N:" + mDevice.getSN());
        mDfBattery = new DecimalFormat("#.0");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.apptitlebar_btn_right:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.btn_logs_get:
                if (mDevice == null || mProject == null) {
                    return;
                }
                try {
                    getLogs();
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 获取日志
     */
    private void getLogs() throws DecoderException {
        mDialogHelper.showProgressDialog();
        byte[] sendData = Encrypt.readLogEncrypt(
                Long.valueOf(mDevice.getSN()),
                System.currentTimeMillis() / 1000,
                mDevice.getMac().replace(":", ""),
                mProject.getUserKey());
        CommLog.logE("sendData:" + Hex.encodeHexString(sendData).toUpperCase());
        ConnectionHelper.getInstance().bleCommunication(
                mDevice.getSN(),
                mDevice.getMac(),
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
                                LogResult logResult = BaglockUtils.parseLogResult(result);
                                CommLog.logE("logResult:" + logResult.toString());
                                if (BleStatusUtil.RST_SUCC.equals(logResult.getResult())) {
                                    LogInfo logInfo = new LogInfo();
                                    logInfo.setMac(mDevice.getMac());
                                    logInfo.setContent("开锁成功");
                                    logInfo.setTimeStamp(logResult.getTimeStamp());
//                                    logInfo.setBattery(mDfBattery.format(logResult.ge()/1000f));
                                    logInfo.setUserId(new String(Hex.decodeHex(logResult.getUserId()), StandardCharsets.US_ASCII));
                                    logInfo.save();
                                    mLogList.add(0, logInfo);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    showToast(BleStatusUtil.getResultMsg(logResult.getResult()));
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
