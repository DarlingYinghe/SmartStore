package com.sicong.smartstore.stock_in.data.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author next
 * @date 2018/7/5
 * 入库管理
 * 数据上传
 *
 * 客户端发送入库信息
 *
 */
public class CargoInMessage implements Serializable{
    private String check;
    private List<Statistic> statistic;
    private String company;
    private String username;
    private String describe;

    public CargoInMessage() {

    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public List<Statistic> getStatistic() {
        return statistic;
    }

    public void setStatistic(List<Statistic> statistic) {
        this.statistic = statistic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String operatorId) {
        this.username = operatorId;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public String toString() {
        return "CargoInMessage{" +
                "check='" + check + '\'' +
                ", statistic=" + statistic +
                ", operatorId='" + username + '\'' +
                ", describe='" + describe + '\'' +
                '}';
    }
}
