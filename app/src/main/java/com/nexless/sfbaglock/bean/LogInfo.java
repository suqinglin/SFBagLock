package com.nexless.sfbaglock.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @date: 2019/5/20
 * @author: su qinglin
 * @description: 日志信息
 */
public class LogInfo extends LitePalSupport {

    private long id; // 主键ID
    private String content; // 内容
    private long timeStamp; // 时间戳
    private long sn; // 序列号
    private String mac; // MAC地址
    private int type; // 日志类型 1:操作日志，2:Toast日志

    public LogInfo() {
    }

    public LogInfo(String content, long timeStamp, long sn, String mac, int type) {
        this.content = content;
        this.timeStamp = timeStamp;
        this.sn = sn;
        this.mac = mac;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getSn() {
        return sn;
    }

    public void setSn(long sn) {
        this.sn = sn;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
