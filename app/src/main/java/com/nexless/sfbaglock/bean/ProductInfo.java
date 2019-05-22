package com.nexless.sfbaglock.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.LitePalSupport;

/**
 * @date: 2019/5/5
 * @author: su qinglin
 * @description: 产品表
 */
public class ProductInfo extends LitePalSupport implements Parcelable {

    private int id;
    private int projectId;
    private String SN;
    private long CNT;
    private String mac;
    private String qr;
    private long timeStamp;

    public ProductInfo() {
    }

    protected ProductInfo(Parcel in) {
        id = in.readInt();
        projectId = in.readInt();
        SN = in.readString();
        CNT = in.readLong();
        mac = in.readString();
        qr = in.readString();
        timeStamp = in.readLong();
    }

    public static final Creator<ProductInfo> CREATOR = new Creator<ProductInfo>() {
        @Override
        public ProductInfo createFromParcel(Parcel in) {
            return new ProductInfo(in);
        }

        @Override
        public ProductInfo[] newArray(int size) {
            return new ProductInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getSN() {
        return SN;
    }

    public void setSN(String SN) {
        this.SN = SN;
    }

    public long getCNT() {
        return CNT;
    }

    public void setCNT(long CNT) {
        this.CNT = CNT;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(projectId);
        dest.writeString(SN);
        dest.writeLong(CNT);
        dest.writeString(mac);
        dest.writeString(qr);
        dest.writeLong(timeStamp);
    }
}
