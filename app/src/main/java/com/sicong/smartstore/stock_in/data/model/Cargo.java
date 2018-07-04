package com.sicong.smartstore.stock_in.data.model;

import java.io.Serializable;

public class Cargo implements Serializable{

    private String typeFirst;
    private String typeSecond;
    private String rfid;

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getTypeFirst() {
        return typeFirst;
    }

    public void setTypeFirst(String typeFirst) {
        this.typeFirst = typeFirst;
    }

    public String getTypeSecond() {
        return typeSecond;
    }

    public void setTypeSecond(String typeSecond) {
        this.typeSecond = typeSecond;
    }
}
