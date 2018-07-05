package com.sicong.smartstore.stock_in.data.model;

import java.util.List;

/**
 * 扫描的数据的统计值
 */
public class Statistic {

    private String typeFirst;
    private String typeSecond;
    private int num;
    private List<String> rfid;

    public String getTypeSecond() {
        return typeSecond;
    }

    public void setTypeSecond(String typeSecond) {
        this.typeSecond = typeSecond;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getTypeFirst() {
        return typeFirst;
    }

    public void setTypeFirst(String typeFirst) {
        this.typeFirst = typeFirst;
    }

    public List<String> getRfid() {
        return rfid;
    }

    public void setRfid(List<String> rfid) {
        this.rfid = rfid;
    }
}
