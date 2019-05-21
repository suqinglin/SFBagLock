package com.nexless.sfbaglock.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.calm.comm.lib.qr.zxing.activity.CaptureActivity;
import com.nexless.ccommble.codec.DecoderException;
import com.nexless.ccommble.codec.binary.Hex;
import com.nexless.ccommble.conn.BluetoothListener;
import com.nexless.ccommble.conn.ConnectionConstants;
import com.nexless.ccommble.conn.ConnectionHelper;
import com.nexless.ccommble.data.BagLockAESUtils;
import com.nexless.ccommble.data.BaglockUtils;
import com.nexless.ccommble.data.Encrypt;
import com.nexless.ccommble.data.model.LockResult;
import com.nexless.ccommble.util.BleStatusUtil;
import com.nexless.ccommble.util.CommHandler;
import com.nexless.ccommble.util.CommLog;
import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.R;
import com.nexless.sfbaglock.adapter.PAdapter;
import com.nexless.sfbaglock.adapter.PViewHolder;
import com.nexless.sfbaglock.bean.DeviceBean;
import com.nexless.sfbaglock.bean.LogInfo;
import com.nexless.sfbaglock.bean.ProductInfo;
import com.nexless.sfbaglock.bean.ProjectInfo;
import com.nexless.sfbaglock.bean.SetupRecordBean;
import com.nexless.sfbaglock.util.CsvHelper;
import com.nexless.sfbaglock.util.DateUtil;
import com.nexless.sfbaglock.view.AppTitleBar;

import org.jetbrains.annotations.Nullable;
import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @date: 2019/5/6
 * @author: su qinglin
 * @description: 生产
 */
public class ProductActivity extends BaseActivity implements View.OnClickListener, CommHandler.MessageHandler {

    private static final String TAG = "BluetoothConnectionCallback";
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final int REQ_QR_CODE = 0x0001;
    private static final int REQ_SEARCH_DEVICE = 0x0002;
    private static final int MSG_UPDATE_LOG = 0x0003;
    private ProjectInfo mProject;
    private EditText mEdtSn;
    private EditText mEdtMac;
    private EditText mEdtCnt;
    private TextView mTvMsg;
    private long mSn;
    private long mCnt;
    private String mMac;
    private String mDevName;
    private CommHandler mHandle = new CommHandler(this);
    private String mLogContent = "";
    private DecimalFormat mDfBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        mProject = getIntent().getParcelableExtra(AppConstant.EXTRA_PROJECT);

        AppTitleBar titleBar = findViewById(R.id.titlebar);
        EditText edtProjectName = findViewById(R.id.edt_product_project_name);
        EditText edtUserId = findViewById(R.id.edt_product_user_id);
        mEdtCnt = findViewById(R.id.edt_product_cnt);
        mEdtMac = findViewById(R.id.edt_product_mac);
        mEdtSn = findViewById(R.id.edt_product_sn);
        mTvMsg = findViewById(R.id.tv_product_msg);
        findViewById(R.id.btn_product_scan).setOnClickListener(this);
        findViewById(R.id.btn_product_test).setOnClickListener(this);
        findViewById(R.id.btn_product_save).setOnClickListener(this);
        findViewById(R.id.btn_product_load).setOnClickListener(this);
        mTvMsg.setMovementMethod(new ScrollingMovementMethod());
        titleBar.setRightListener(this);
        edtProjectName.setText(mProject.getProjectName());
        edtUserId.setText(mProject.getUserId());
        mDfBattery = new DecimalFormat("#.0");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_product_scan:
                if (!checkPermissions(NEEDED_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
                } else {
                    Intent intent = new Intent(this, CaptureActivity.class);
                    startActivityForResult(intent, REQ_QR_CODE);
                }
                break;
            case R.id.btn_product_test:
                try {
                    mCnt++;
                    test();
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_product_save:
                save();
                break;
            case R.id.btn_product_load:
                List<ProductInfo> productList = LitePal
                        .where("projectId = ?", String.valueOf(mProject.getId()))
                        .find(ProductInfo.class);
                if (productList != null && productList.size() > 0) {
                    showProductList(productList);
                } else {
                    showToast("暂无可选产品，请先添加！");
                }
                break;
            case R.id.apptitlebar_btn_right:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
    }

    private void showProductList(List<ProductInfo> productList) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product");
        builder.setSingleChoiceItems(new PAdapter<ProductInfo>(this, productList, R.layout.item_project) {
            @Override
            public void convert(PViewHolder helper, ProductInfo item, int position) {
                TextView tvProjectName = helper.getView(R.id.tv_project_item_project_name);
                tvProjectName.setText(item.getMac() + "(" + item.getSN() + ")");
            }
        }, 0, (dialog, which) -> {
            dialog.cancel();
            ProductInfo product = productList.get(which);
            mSn = product.getSN();
            mCnt = product.getCNT();
            mMac = product.getMac();
            mDevName = String.valueOf(mSn);
            mEdtSn.setText(String.valueOf(mSn));
            mEdtMac.setText(mMac);
            mEdtCnt.setText(String.valueOf(mCnt));
        });
        builder.show();
    }

    private void setUserKey() {
        mDialogHelper.showProgressDialog();
        byte[] sendData = Encrypt.setUserKeyEncrypt(mProject.getUserKey());
        CommLog.logE(TAG, "setUserKey sendData:" + Hex.encodeHexString(sendData).toUpperCase());
        ConnectionHelper.getInstance().bleCommunication(
                mDevName,
                mMac,
                null,
                sendData,
                true,
                new BluetoothListener() {

                    @Override
                    public void onDataChange(@Nullable byte[] data) {
                        String result = Hex.encodeHexString(data).toUpperCase();
                        CommLog.logE(TAG, "receiveData:" + result);
                        String resData = result.substring(2, 36);
                        try {
                            if (checkCrc(resData, result.substring(36))) {
                                LockResult lockResult = BaglockUtils.parseLockResult(result);
                                CommLog.logE("lockResult:" + lockResult.toString());
                                if (BleStatusUtil.RST_SUCC.equals(lockResult.getResult())) {
                                    setSnCnt();
                                } else {
                                    CommLog.logE(TAG, "setUserKey->onDataChange:Status = " + lockResult.getResult());
                                    mDialogHelper.dismissProgressDialog();
                                    addOptionLog(BleStatusUtil.getResultMsg(lockResult.getResult()));
                                }
                            } else {
                                CommLog.logE(TAG, "setUserKey->onDataChange:crc校验失败");
                                mDialogHelper.dismissProgressDialog();
                                addOptionLog("crc校验失败");
                            }
                        } catch (DecoderException e) {
                            CommLog.logE(TAG, "setUserKey->onDataChange->DecoderException:" + e.getMessage());
                            e.printStackTrace();
                            mDialogHelper.dismissProgressDialog();
                            addOptionLog("程序异常");
                        }
                    }

                    @Override
                    public void onConnStatusFail(int status) {
                        CommLog.logE(TAG, "setUserKey->onConnStatusFail:Status = " + status);
                        mDialogHelper.dismissProgressDialog();
                        showToast(BleStatusUtil.getConnectStatusMsg(status));
                    }

                    @Override
                    public void onConnStatusSucc(int status) {

                    }
                }, 3000);
    }

    private void setSnCnt() throws DecoderException {
        byte[] sendData = Encrypt.setSnCntEncrypt(mSn, mCnt, mMac.replace(":", ""));
        CommLog.logE(TAG, "setSnCnt sendData:" + Hex.encodeHexString(sendData).toUpperCase());
        ConnectionHelper.getInstance().bleCommunication(
                mDevName,
                mMac,
                null,
                sendData,
                true,
                new BluetoothListener() {

                    @Override
                    public void onDataChange(@Nullable byte[] data) {
                        mDialogHelper.dismissProgressDialog();
                        String result = Hex.encodeHexString(data).toUpperCase();
                        CommLog.logE("receiveData:" + result);
                        String resData = result.substring(2, 36);
                        try {
                            if (checkCrc(resData, result.substring(36))) {
                                LockResult lockResult = BaglockUtils.parseLockResult(result);
                                CommLog.logE("lockResult:" + lockResult.toString());
                                if (BleStatusUtil.RST_SUCC.equals(lockResult.getResult())) {
                                    addOptionLog("匹配成功，SN:" + mEdtSn.getText() + "，电压:" + mDfBattery.format(lockResult.getBattery()/1000f) + "V");
                                    ProductInfo product = new ProductInfo();
                                    product.setCNT(mCnt);
                                    product.setMac(mMac);
                                    product.setSN(mSn);
                                    product.setProjectId(mProject.getId());
                                    product.setTimeStamp(System.currentTimeMillis() / 1000);
                                    product.saveOrUpdate("mac = ?", mMac);
                                } else {
                                    addOptionLog(BleStatusUtil.getResultMsg(lockResult.getResult()));
                                }
                            } else {
                                addOptionLog("crc校验失败");
                            }
                        } catch (DecoderException e) {
                            e.printStackTrace();
                            addOptionLog("程序异常");
                        }
                    }

                    @Override
                    public void onConnStatusFail(int status) {
                        mDialogHelper.dismissProgressDialog();
//                        if (status == ConnectionConstants.STATUS_DATA_READ_TIMEOUT) {
//                            ConnectionHelper.getInstance().disConnDevice(mMac);
//                        }
                        showToast(BleStatusUtil.getConnectStatusMsg(status));
                    }

                    @Override
                    public void onConnStatusSucc(int status) {

                    }
                }, 100);
    }

    private void test() throws DecoderException {

        if (!checkData()) {
            return;
        }
        mDialogHelper.showProgressDialog();
        byte[] sendData = Encrypt.openEncrypt(
                mSn,
                mCnt,
                System.currentTimeMillis() / 1000,
                mMac.replace(":", ""),
                mProject.getUserId(),
                mProject.getUserKey());
        CommLog.logE("sendData:" + Hex.encodeHexString(sendData).toUpperCase());
        ConnectionHelper.getInstance().bleCommunication(
                mDevName,
                mMac,
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
                            if (checkCrc(resData, result.substring(36))) {
                                LockResult lockResult = BaglockUtils.parseLockResult(result);
                                CommLog.logE("lockResult:" + lockResult.toString());
                                if (BleStatusUtil.RST_SUCC.equals(lockResult.getResult())) {
                                    ProductInfo product = LitePal.where("mac = ?", mMac).findFirst(ProductInfo.class);
                                    if (product != null) {
                                        product.setCNT(mCnt);
                                        product.setTimeStamp(System.currentTimeMillis() / 1000);
                                        product.update(product.getId());
                                    } else {
                                        CommLog.logE("Open success, product in database is null");
                                        product = new ProductInfo();
                                        product.setProjectId(mProject.getId());
                                        product.setMac(mMac);
                                        product.setSN(mSn);
                                        product.setCNT(mCnt);
                                        product.setTimeStamp(System.currentTimeMillis() / 1000);
                                        product.save();
                                    }
                                    CommLog.logE("-----------------------------> SN = " + mSn + "/" + product.getSN());
                                    mEdtCnt.setText(String.valueOf(mCnt));
                                    addOptionLog("开锁成功，SN:" + mEdtSn.getText() + "，电压:" + mDfBattery.format(lockResult.getBattery() / 1000f) + "V");
                                } else {
                                    addOptionLog(BleStatusUtil.getResultMsg(lockResult.getResult()));
                                }
                            } else {
                                addOptionLog("crc校验失败");
                            }
                        } catch (DecoderException e) {
                            e.printStackTrace();
                            addOptionLog("程序异常");
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

    /**
     * 检查数据是否完整
     */
    private boolean checkData() {
        if (mSn == 0) {
            showToast("请先扫描SN二维码");
            return false;
        } else if (mMac == null || mCnt == 0) {
            showToast("请先扫码并选择设备");
            return false;
        } else {
            return true;
        }
    }

    private void save() {
        List<ProductInfo> productList = LitePal.findAll(ProductInfo.class);
        List<SetupRecordBean> setupRecordList = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            ProductInfo product = productList.get(i);
            ProjectInfo project = LitePal.find(ProjectInfo.class, product.getProjectId());
            SetupRecordBean setupRecord = new SetupRecordBean();
            setupRecord.setCnt(product.getCNT());
            setupRecord.setSn(product.getSN());
            setupRecord.setMac(product.getMac());
            if (project != null) {
                setupRecord.setProject(project.getProjectName());
                setupRecord.setUserKey(project.getUserKey());
                setupRecord.setUserId(project.getUserId());
                setupRecord.setTimeStamp(product.getTimeStamp());
            }
            setupRecordList.add(setupRecord);
        }

        boolean save = CsvHelper.getInstance().saveSetupRecords(setupRecordList);
        if (save) {
            showToast("保存成功");
        } else {
            showToast("保存失败");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫码
        if (resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {
            String result = data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
            CommLog.logE("qr_result:" + result);
            if (!result.contains("SN:")) {
                showToast("二维码格式错误");
                return;
            }
            try {
                mSn = Long.valueOf(result.split("SN:")[1]);
                if (mSn >= mProject.getSnStart() && mSn <= mProject.getSnEnd()) {
                    mEdtSn.setText(result.split("SN:")[1]);
                    startActivityForResult(new Intent(this, SearchDeviceActivity.class), REQ_SEARCH_DEVICE);
                } else {
                    showToast("SN未在指定范围内");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                showToast("二维码格式错误");
            }
        }
        if (requestCode == REQ_SEARCH_DEVICE && data != null) {
            DeviceBean device = data.getParcelableExtra(AppConstant.EXTRA_DEVICE);
            mMac = device.device.getAddress();
            mDevName = device.device.getName();
            mCnt = getRandomCnt();
            mEdtMac.setText(mMac);
            mEdtCnt.setText(String.valueOf(mCnt));
            setUserKey();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent, REQ_QR_CODE);
            } else {
                showToast("权限被拒绝");
            }
        }
    }

    private boolean checkCrc(String data, String crc) throws DecoderException {
        int dataCrc = BagLockAESUtils.crc16(0, Hex.decodeHex(data));
        int crcInt = Integer.parseInt(crc, 16);
        return dataCrc == crcInt;
    }

    /**
     * 随机生成2^32以内的正整数
     * @return
     */
    private long getRandomCnt() {
        long n = 1L<<31; // 2^31
        Random rng = new Random();
        long bits, val;
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % n;
        } while (bits-val+(n-1) < 0L);
        return val;
//        return 1000;
    }

    /**
     * 操作日志
     */
    private void addOptionLog(String msg) {
        LogInfo logInfo = new LogInfo(msg, System.currentTimeMillis(), mSn, mMac, 1);
        logInfo.save();
        Message message = Message.obtain();
        message.what = MSG_UPDATE_LOG;
        message.obj = logInfo;
        mHandle.sendMessage(message);
    }

    @Override
    protected void showToast(String msg) {
        super.showToast(msg);
        if (!TextUtils.isEmpty(msg) && mSn != 0 && !TextUtils.isEmpty(mMac)) {
            LogInfo logInfo = new LogInfo(msg, System.currentTimeMillis(), mSn, mMac, 2);
            logInfo.save();
            Message message = Message.obtain();
            message.what = MSG_UPDATE_LOG;
            message.obj = logInfo;
            mHandle.sendMessage(message);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_LOG:
                LogInfo logInfo = (LogInfo) msg.obj;
                mLogContent = DateUtil.parseLongToString(logInfo.getTimeStamp(), DateUtil.FORMAT_HH_MM_SS) + "  " + logInfo.getContent() + "\n" + mLogContent;
                mTvMsg.setText(mLogContent);
        }
    }
}
