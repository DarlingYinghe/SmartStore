package com.sicong.smartstore.stock_out.model;

import java.util.List;
import java.util.Map;

/**
 *
 * @author next
 * @date 2018/7/5
 * 获取单内数据
 * 往客户端发送信息
 */
public class CargoListSendMessage {
    private List<Map<String,Object>> statistic;

    public CargoListSendMessage(List<Map<String, Object>> listState) {
        statistic = listState;
    }

    public CargoListSendMessage() {
    }

    public List<Map<String, Object>> getListState() {
        return statistic;
    }

    public void setListState(List<Map<String, Object>> listState) {
        statistic = listState;
    }
}
