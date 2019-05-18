package com.nexless.sfbaglock.util;

import com.nexless.sfbaglock.bean.SetupRecordBean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @date: 2019/5/7
 * @author: su qinglin
 * @description:
 */
public class CsvHelper {

    private static CsvHelper instance;
    private CsvHelper() {}
    public static CsvHelper getInstance() {
        if (instance == null) {
            instance = new CsvHelper();
        }
        return instance;
    }

    public boolean saveSetupRecords(List<SetupRecordBean> list) {
        try {
            File file = FileHelper.resetFile("sf_setup_record.csv");
//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()  + File.separator + "sf_setup_record" + ".csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            // 添加头部名称
            bw.write("project" + "," + "userKey" + "," + "userId" + "," + "SN" + "," + "MAC" + "," + "CNT" + "," + "timeStamp");
            bw.newLine();
            for (int i = 0; i < list.size(); i++) {
                bw.write(
                        list.get(i).getProject()
                                + "," + list.get(i).getUserKey()
                                + "," + list.get(i).getUserId()
                                + "," + list.get(i).getSn()
                                + "," + list.get(i).getMac()
                                + "," + list.get(i).getCnt()
                                + "," + list.get(i).getTimeStamp());
                bw.newLine();
            }
            bw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
