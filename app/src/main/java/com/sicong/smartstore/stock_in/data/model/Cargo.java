package com.sicong.smartstore.stock_in.data.model;

import java.io.Serializable;

public class Cargo implements Serializable{

    private String name;
    private String rfid;

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
