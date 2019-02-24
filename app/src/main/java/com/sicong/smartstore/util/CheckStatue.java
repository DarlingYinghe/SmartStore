package com.sicong.smartstore.util;

public class CheckStatue {
    public static String checkStatus(String status) {
        switch (status) {
            case "0":
                return "未出库";
            case "1":
                return "出库中";
            case "2":
                return "已完成";
        }
        return "undefined";
    }

}
