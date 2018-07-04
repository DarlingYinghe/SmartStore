package com.sicong.smartstore.stock_in.data.model;

import java.io.Serializable;
import java.util.List;

public class InventoryResult implements Serializable{
    private String operatorId;
    private String describe;
    private List<Cargo> cargos;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public void setCargos(List<Cargo> cargos) {
        this.cargos = cargos;
    }

}
