package com.sicong.smartstore.stock_in.data.model;

/**
 *
 * @author next
 * @date 2018/7/5
 * 往客户端发送认证消息
 *获取产品类型
 */

public class CheckMessage {

    private String check;

    public CheckMessage(String check) {
        this.check = check;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }
}
