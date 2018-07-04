package com.sicong.smartstore.util.fn.u6.utils;

import android.media.MediaPlayer;


import com.sicong.smartstore.R;
import com.sicong.smartstore.util.fn.u6.UHFApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class Tools {

	// byte תʮ������
	public static String Bytes2HexString(byte[] b, int size) {
		String ret = "";
		for (int i = 0; i < size; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 })).byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	// ʮ������תbyte
	public static byte[] HexString2Bytes(String src) {
		int len = src.length() / 2;
		byte[] ret = new byte[len];
		byte[] tmp = src.getBytes();

		for (int i = 0; i < len; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}

	/* byte[]תInt */
	public static int bytesToInt(byte[] bytes) {
		int addr = bytes[0] & 0xFF;
		addr |= ((bytes[1] << 8) & 0xFF00);
		addr |= ((bytes[2] << 16) & 0xFF0000);
		addr |= ((bytes[3] << 25) & 0xFF000000);
		return addr;

	}

	/* Intתbyte[] */
	public static byte[] intToByte(int i) {
		byte[] abyte0 = new byte[4];
		abyte0[0] = (byte) (0xff & i);
		abyte0[1] = (byte) ((0xff00 & i) >> 8);
		abyte0[2] = (byte) ((0xff0000 & i) >> 16);
		abyte0[3] = (byte) ((0xff000000 & i) >> 24);
		return abyte0;
	}

	/**
	 * ��ȡϵͳʱ�䣬ʱ���ʽΪ�� ��-��-�� ʱ���� ��
	 * 
	 * @return
	 */
	public static String getTime() {
		String model = "yyyy-MM-dd HH:mm:ss";
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(model);
		String dateTime = format.format(date);
		return dateTime;
	}

	public static byte getCRC(byte[] cmd, int pos, int len) {
		byte crc = (byte) 0x00;
		for (int i = pos; i < len - 1; i++) {
			crc += cmd[i];
		}
		return crc;
	}

	/**
	 * �ַ���ת16�������飬�ַ����Կո�ָ
	 * 
	 * @param strHexValue
	 *            16�����ַ���
	 * @return ����
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
	 * 
	 * @param strAryHex
	 *            Ҫת�����ַ�������
	 * @param nLen
	 *            ����
	 * @return ����
	 */
	public static byte[] stringArrayToByteArray(String[] strAryHex, int nLen) {
		if (strAryHex == null)
			return null;

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
	 * 
	 * @param btAryHex
	 *            Ҫת�����ַ�������
	 * @param nIndex
	 *            ��ʼλ��
	 * @param nLen
	 *            ����
	 * @return �ַ���
	 */
	public static String byteArrayToString(byte[] btAryHex, int nIndex, int nLen) {
		if (nIndex + nLen > btAryHex.length) {
			nLen = btAryHex.length - nIndex;
		}

		String strResult = String.format("%02X", btAryHex[nIndex]);
		for (int nloop = nIndex + 1; nloop < nIndex + nLen; nloop++) {
			String strTemp = String.format(" %02X", btAryHex[nloop]);

			strResult += strTemp;
		}

		return strResult;
	}

	/**
	 * ���ַ�������ָ�����Ƚ�ȡ��ת��Ϊ�ַ����飬�ո���ԡ�
	 * 
	 * @param strValue
	 *            �����ַ���
	 * @return ����
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

					if (!Pattern.compile("^(([A-F])*([a-f])*(\\d)*)$").matcher(strValue.substring(nloop, nloop + 1)).matches()) {
						return strAryResult;
					}

					strTemp += strValue.substring(nloop, nloop + 1);

					// �ж��Ƿ񵽴��ȡ����
					if ((nTemp == nLen) || (nloop == strValue.length() - 1 && (strTemp != null && !strTemp.equals("")))) {
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

	private static MediaPlayer player = null;

	public static void playSuccessMedia() {
		player = MediaPlayer.create(UHFApplication.applicationContext, R.raw.ok);
		if (player.isPlaying()) {
			// player.release();
			// player.reset();
			// player = MediaPlayer.create(context, R.raw.msg);
			return;
		}
		try {
			// player.prepare();
			player.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			player.release();
			player = null;
		}

	}

	public static void playFailureMedia() {
		player = MediaPlayer.create(UHFApplication.applicationContext, R.raw.error);
		if (player.isPlaying()) {
			// player.release();
			// player.reset();
			// player = MediaPlayer.create(context, R.raw.msg);
			return;
		}
		try {
			// player.prepare();
			player.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			player.release();
			player = null;
		}
	}
}
