package com.sicong.smartstore.stock_in.data.model;

import java.io.Serializable;
import java.util.List;

/**
 * 扫描的数据的统计值
 */
public class Statistic implements Serializable{

    private String name;
    private int num;
    private List<String> rfid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public List<String> getRfid() {
        return rfid;
    }

    public void setRfid(List<String> rfid) {
        this.rfid = rfid;
    }
}
