package com.sicong.smartstore.stock_user.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class UnoverMessage {

    @Id(assignable = true)
    public long id;

    public String AccountId;

    public String date;

    public String title;
    /*
     *判断是出库还是入库
     * 0为入库,1为出库,2为盘点
    */
    public int check;

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
    }

    public String getAccountId() {
        return AccountId;
    }

    public void setAccountId(String accountId) {
        AccountId = accountId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
