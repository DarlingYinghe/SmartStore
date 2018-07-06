package com.sicong.smartstore.stock_out.model;

/**
 *
 * @author next
 * @date 2018/7/5
 * 获取单内列表数据
 * 出库时从客户端获取单号列表数据消息
 *
 */
public class StockOutCargoReceiveMessage {
    private String check;

    public StockOutCargoReceiveMessage(String check) {
        this.check = check;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }
}
