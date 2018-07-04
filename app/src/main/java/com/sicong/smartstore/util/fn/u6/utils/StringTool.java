package com.sicong.smartstore.util.fn.u6.utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class StringTool {

	/**
	 * �ַ���ת16�������飬�ַ����Կո�ָ
	 * @param strHexValue	16�����ַ���
	 * @return	����
	 */
	public static byte[] stringToByteArray(String strHexValue) {
		String[] strAryHex = strHexValue.split(" ");
        byte[] btAryHex = new byte[strAryHex.length];

        try {
			int nIndex = 0;
			for (String strTemp : strAryHex) {
			    btAryHex[nIndex] = (byte) Integer.parseInt(strTemp, 16);
			    nIndex++;
			}
        } catch (NumberFormatException e) {

        }

        return btAryHex;
    }

	/**
	 * �ַ�����תΪ16�������顣
	 * @param strAryHex	Ҫת�����ַ�������
	 * @param nLen		����
	 * @return	����
	 */
    public static byte[] stringArrayToByteArray(String[] strAryHex, int nLen) {
    	if (strAryHex == null) return null;

    	if (strAryHex.length < nLen) {
    		nLen = strAryHex.length;
    	}

    	byte[] btAryHex = new byte[nLen];

    	try {
    		for (int i = 0; i < nLen; i++) {
    			btAryHex[i] = (byte) Integer.parseInt(strAryHex[i], 16);
    		}
    	} catch (NumberFormatException e) {
	
        }

    	return btAryHex;
    }

	/**
	 * 16�����ַ�����ת���ַ�����
	 * @param btAryHex	Ҫת�����ַ�������
	 * @param nIndex	��ʼλ��
	 * @param nLen		����
	 * @return	�ַ���
	 */
    public static String byteArrayToString(byte[] btAryHex, int nIndex, int nLen) {
    	if (nIndex + nLen > btAryHex.length) {
    		nLen = btAryHex.length - nIndex;
    	}

    	String strResult = String.format("%02X", btAryHex[nIndex]);
    	for (int nloop = nIndex + 1; nloop < nIndex + nLen; nloop++ ) {
    		String strTemp = String.format(" %02X", btAryHex[nloop]);

    		strResult += strTemp;
    	}

    	return strResult;
    }

	/**
	 * ���ַ�������ָ�����Ƚ�ȡ��ת��Ϊ�ַ����飬�ո���ԡ�
	 * @param strValue	�����ַ���
	 * @return	����
	 */
    public static String[] stringToStringArray(String strValue, int nLen) {
        String[] strAryResult = null;

        if (strValue != null && !strValue.equals("")) {
            ArrayList<String> strListResult = new ArrayList<String>();
            String strTemp = "";
            int nTemp = 0;

            for (int nloop = 0; nloop < strValue.length(); nloop++) {
                if (strValue.charAt(nloop) == ' ') {
                    continue;
                } else {
                    nTemp++;
                    
                    if (!Pattern.compile("^(([A-F])*([a-f])*(\\d)*)$")
                    		.matcher(strValue.substring(nloop, nloop + 1))
                    		.matches()) {
                        return strAryResult;
                    }

                    strTemp += strValue.substring(nloop, nloop + 1);

                    //�ж��Ƿ񵽴��ȡ����
                    if ((nTemp == nLen) || (nloop == strValue.length() - 1 
                    		&& (strTemp != null && !strTemp.equals("")))) {
                        strListResult.add(strTemp);
                        nTemp = 0;
                        strTemp = "";
                    }
                }
            }

            if (strListResult.size() > 0) {
            	strAryResult = new String[strListResult.size()];
                for (int i = 0; i < strAryResult.length; i++) {
                	strAryResult[i] = strListResult.get(i);
                }
            }
        }

        return strAryResult;
    }
}
