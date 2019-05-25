package com.nexless.sfbaglock.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.LitePalSupport;

/**
 * @date: 2019/5/5
 * @author: su qinglin
 * @description: 项目表
 */
public class ProjectInfo extends LitePalSupport implements Parcelable {

    private int id;
    private String projectNo;
    private String projectName;
    private String userId;
    private String userKey;
    private String weChart;
    private long snStart;
    private long snEnd;

    public ProjectInfo() {
    }

    protected ProjectInfo(Parcel in) {
        id = in.readInt();
        projectNo = in.readString();
        projectName = in.readString();
        userId = in.readString();
        userKey = in.readString();
        weChart = in.readString();
        snStart = in.readLong();
        snEnd = in.readLong();
    }

    public static final Creator<ProjectInfo> CREATOR = new Creator<ProjectInfo>() {
        @Override
        public ProjectInfo createFromParcel(Parcel in) {
            return new ProjectInfo(in);
        }

        @Override
        public ProjectInfo[] newArray(int size) {
            return new ProjectInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getWeChart() {
        return weChart;
    }

    public void setWeChart(String weChart) {
        this.weChart = weChart;
    }

    public long getSnStart() {
        return snStart;
    }

    public void setSnStart(long snStart) {
        this.snStart = snStart;
    }

    public long getSnEnd() {
        return snEnd;
    }

    public void setSnEnd(long snEnd) {
        this.snEnd = snEnd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(projectNo);
        dest.writeString(projectName);
        dest.writeString(userId);
        dest.writeString(userKey);
        dest.writeString(weChart);
        dest.writeLong(snStart);
        dest.writeLong(snEnd);
    }
}
