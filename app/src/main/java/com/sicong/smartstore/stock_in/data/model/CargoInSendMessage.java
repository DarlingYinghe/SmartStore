package com.sicong.smartstore.stock_in.data.model;

import java.util.List;
import java.util.Map;

/**
 *
 * @author next
 * @date 2018/7/5
 * 入库管理
 * 获取产品类型
 *
 * 往客户端发送消息
 *
 */
public class CargoInSendMessage {
    private List<Map<String,Object>> type;

    public CargoInSendMessage() {

    }

    public List<Map<String, Object>> getType() {
        return type;
    }

    public CargoInSendMessage(List<Map<String, Object>> type) {
        this.type = type;
    }

    public void setType(List<Map<String, Object>> type) {
        this.type = type;
    }
}
