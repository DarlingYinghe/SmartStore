package com.sicong.smartstore.stock_out.model;

import java.util.List;
import java.util.Map;

/**
 *
 * @author next
 * @date 2018/7/5
 * 获取单内列表数据
 *
 * 出库时发送客户端消息
 *
 */
public class CargoSendListMessage {
    private List<Map<String,String>> List;
    public CargoSendListMessage() {

    }

    public CargoSendListMessage(List<Map<String, String>> cargoList) {
        List = cargoList;
    }

    public List<Map<String, String>> getCargoList() {
        return List;
    }

    public void setCargoList(List<Map<String, String>> cargoList) {
        List = cargoList;
    }
}
