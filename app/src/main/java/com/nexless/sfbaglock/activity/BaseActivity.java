package com.nexless.sfbaglock.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nexless.sfbaglock.AppConstant;
import com.nexless.sfbaglock.bean.ProductInfo;
import com.nexless.sfbaglock.bean.ProjectInfo;
import com.nexless.sfbaglock.bean.SetupRecordBean;
import com.nexless.sfbaglock.bean.TResponse;
import com.nexless.sfbaglock.bean.UploadCsvResponse;
import com.nexless.sfbaglock.http.RxHelper;
import com.nexless.sfbaglock.http.ServiceFactory;
import com.nexless.sfbaglock.util.CsvHelper;
import com.nexless.sfbaglock.view.DialogHelper;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @date: 2019/5/5
 * @author: su qinglin
 * @description:
 */
public class BaseActivity extends Activity {

    protected DialogHelper mDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDialogHelper = new DialogHelper(this);
    }

    /**
     * 所需的所有权限信息
     */
    public static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(getApplicationContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    public void save(String TAG) {
        List<ProductInfo> productList = LitePal.findAll(ProductInfo.class);
        if (productList == null || productList.isEmpty()) {
            showToast("暂无可上传设备，请先扫码设置");
            return;
        }
        List<SetupRecordBean> setupRecordList = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            ProductInfo product = productList.get(i);
            if (product.getProjectNo() == null) {
                break;
            }
            ProjectInfo project = LitePal.where("projectNo = ?", product.getProjectNo()).findFirst(ProjectInfo.class);
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
//            showToast("保存成功");
            uploadCsv(TAG, new File(System.getenv("EXTERNAL_STORAGE") + "/nexless/" + AppConstant.CSV_FILE_NAME));
        } else {
            showToast("保存失败");
        }
    }

    /**
     * 上传csv文件到云服务器
     * @param file
     */
    private void uploadCsv(String TAG, File file) {
        if (!file.exists()) {
            return;
        }
        mDialogHelper.showProgressDialog();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        String name = file.getName();
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", name, requestBody);
        Observable<TResponse<UploadCsvResponse>> observable = ServiceFactory.getInstance().getApiService().uploadCsv(filePart);

        RxHelper.getInstance().sendRequest(TAG, observable, uploadCsvResponseTResponse -> {
            mDialogHelper.dismissProgressDialog();
            if (uploadCsvResponseTResponse.isSuccess()) {
                showToast("上传成功");
            } else {
                showToast(uploadCsvResponseTResponse.message);
            }
        }, throwable -> {
            mDialogHelper.dismissProgressDialog();
            showToast(RxHelper.getInstance().getErrorInfo(throwable));
        });
    }

}
