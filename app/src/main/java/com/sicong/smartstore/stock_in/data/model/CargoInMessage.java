package com.sicong.smartstore.stock_in.data.model;

import java.io.Serializable;
import java.util.List;

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
    private String description;



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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
