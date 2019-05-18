package com.nexless.sfbaglock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;

import com.calm.comm.lib.qr.zxing.activity.CaptureActivity;
import com.nexless.ccommble.codec.DecoderException;
import com.nexless.ccommble.codec.binary.Hex;
import com.nexless.ccommble.conn.BluetoothListener;
import com.nexless.ccommble.conn.ConnectionHelper;
import com.nexless.ccommble.util.BleStatusUtil;
import com.nexless.ccommble.util.CommLog;
import com.nexless.ccommble.util.CommUtil;
import com.nexless.sfbaglock.R;

import org.jetbrains.annotations.Nullable;

/**
 * @date: 2019/5/7
 * @author: su qinglin
 * @description:
 */
public class BluetoothTestActivity extends BaseActivity implements View.OnClickListener {

    private static final int ACTION_REQUEST_PERMISSIONS = 0x0001;
    private static final int REQ_QR_CODE = 0x0002;
    private EditText mEdtQrResult;
    private EditText mEdtSendResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);

        findViewById(R.id.btn_bluetooth_test_scan).setOnClickListener(this);
        findViewById(R.id.btn_bluetooth_test_send).setOnClickListener(this);
        mEdtQrResult = findViewById(R.id.edt_bluetooth_test_qr_result);
        mEdtSendResult = findViewById(R.id.edt_bluetooth_test_send_result);

        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_bluetooth_test_scan:
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent, REQ_QR_CODE);
                break;
        }
    }

    private void sendData(String devName, String mac, String data) throws DecoderException {
        mDialogHelper.showProgressDialog();
        byte[] sendData =  Hex.decodeHex(data);
        CommLog.logE("sendData:" + Hex.encodeHexString(sendData).toUpperCase());
        ConnectionHelper.getInstance().bleCommunication(
                "11111",
                mac,
                null,
                sendData,
                true,
                new BluetoothListener() {

                    @Override
                    public void onDataChange(@Nullable byte[] data) {
                        mDialogHelper.dismissProgressDialog();
                        String result = Hex.encodeHexString(data).toUpperCase();
                        mEdtSendResult.setText(result);
                    }

                    @Override
                    public void onConnStatusFail(int status) {
                        mDialogHelper.dismissProgressDialog();
                        showToast(BleStatusUtil.getConnectStatusMsg(status));
                    }

                    @Override
                    public void onConnStatusSucc(int status) {

                    }
                }, 5000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {
            String result = data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
            mEdtQrResult.setText(result);
            String[] resultArr = result.split("#");
            try {
                sendData(resultArr[0], CommUtil.splitMac(resultArr[0].substring(4)), resultArr[1]);
            } catch (DecoderException e) {
                e.printStackTrace();
            }
        }
    }
}
