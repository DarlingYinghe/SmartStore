package com.sicong.smartstore.util.fn.u6.rfid.manager;

import com.sicong.smartstore.util.fn.u6.model.IResponseHandler;

import java.util.List;

public interface IManager {
	
	/**
	 * ���ó�ʱʱ��
	 * @param ms ��ʱʱ��
	 */
	public void setTimeout(int ms);
	
	/**
	 * ��ѯHardware�汾��
	 * @return Hardware�汾��
	 */
	@Deprecated
	public String queryHardwareVersion();
	
	public void queryHardwareVersion(IResponseHandler handler);
	
	/**
	 * ��ѯFirmware�汾��
	 * @return Firmware�汾��
	 */
	@Deprecated
	public String queryFirmwareVersion();
	
	public void queryFirmwareVersion(IResponseHandler handler);
	
	/**
	 * ��ѯ
	 * @return ��ѯ���ı�ǩ����
	 */
	@Deprecated
	public List<?> inventory();
	
	/**
	 * ������ѯ
	 * @param handler
	 */
	public void inventory(IResponseHandler handler);
	
	/**
	 * �����̴棬���Ϸ��͵���ָ��
	 * @param ms  ���͵ļ��ʱ��
	 * @param handler
	 */
	public void inventoryContinuously(int ms, IResponseHandler handler);

	/**
	 * �����ѯ
	 * @param count ��ѯ�Ĵ���
	 * @param handler
	 */
	public void inventoryMore(int count, IResponseHandler handler);

	/**
	 * ֹͣ�����ѯ
	 * @param handler
	 */
	public void stopInventory(IResponseHandler handler);

	/**
	 * ����select����
	 * @param target
	 * @param action
	 * @param memBank
	 * @param isTruncate
	 * @param epc
	 * @param handler
	 */
	public void setSelectParams(int target, int action, int memBank, boolean isTruncate, String epc, IResponseHandler handler);

	/**
	 * ����selectģʽ
	 * @param mode
	 * @param handler
	 */
	public void setSelectMode(byte mode, IResponseHandler handler);

	/**
	 * ��ָ���洢��������
	 * @param memBank ָ�������ݴ洢��
	 * @param address   ��ַƫ��
	 * @param length     ���ݳ���  ��λΪ�֣������ֽ�
	 * @param password ��������
	 * @return  ������������򷵻����ݣ�������������򷵻���λ������
	 */
	@Deprecated
	public String readData(int memBank, int address, int length, byte[] password);

	public void readData(int memBank, int address, int length, byte[] password, IResponseHandler handler);


	/**
	 * ��ָ���Ĵ洢��д����
	 * @param memBank ָ�������ݴ洢��
	 * @param address   ��ַƫ��
	 * @param password ��������
	 * @param dataBytes д������
	 */
	@Deprecated
	public int writeData(int memBank, int address, byte[] password, byte[] dataBytes);

	 public void writeData(int memBank, int address, byte[] password, byte[] dataBytes, IResponseHandler handler);

	/**
	 * ������ǩ
	 * @param memBank  �洢��
	 * @param lockType    ��������
	 * @param password   ��������
	 */
	 @Deprecated
	public boolean lockTag(int memBank, int lockType, byte[] password);

	 public void lockTag(int memBank, int lockType, byte[] password, IResponseHandler handler);

	/**
	 * ���ٱ�ǩ
	 * @param password  ��������
	 */
	 @Deprecated
	public boolean killTag(byte[] password);

	 public void killTag(byte[] password, IResponseHandler handler);

	 /**
	  * ��ȡQuery����
	  * @param handler
	  */
	 public void getQueryParams(IResponseHandler handler);

	 /**
	  * ����Query����
	  * @param para  ���ֽڵĲ���
	  * @param handler
	  */
	 public void setQueryParams(byte[] para, IResponseHandler handler);

	 /**
	  * ���ù�������
	  * @param location  ���ҵ�������
	  * @param handler
	  */
	 public void setWorkLocation(byte location, IResponseHandler handler);

	 /**
	  * ���ù����ŵ�
	  * @param ch_index  �ŵ�����
	  * @param handler
	  */
	 public void setWorkChannel(byte ch_index, IResponseHandler handler);

	 /**
	  * ��ȡ�����ŵ�
	  * @param handler
	  */
	 public void getWorkChannel(IResponseHandler handler);

	 /**
	  * �����Զ���Ƶ
	  * @param isOn
	  * @param handler
	  */
	 public void setAutoFreqHop(boolean isOn, IResponseHandler handler);

	 /**
	  * ��ȡ���书��
	  * @param handler
	  */
	public void getRFPower(IResponseHandler handler);

	/**
	 * ���÷��书��
	 * @param power
	 * @param handler
	 */
	public void setRFPower(int power, IResponseHandler handler);

	/**
	 * ���÷��������ز�
	 * @param isOn
	 * @param handler
	 */
	public void setContinuousWave(boolean isOn, IResponseHandler handler);

	/**
	 * ��ȡ���ս��������
	 */
	public void getModemParams(IResponseHandler handler);

	/**
	 * ���ý��ս��������
	 * @param mixer_g ��Ƶ������
	 * @param if_g ��Ƶ�Ŵ�������
	 * @param thrd �źŽ����ֵ
	 * @param handler
	 */
	public void setModemParams(byte mixer_g, byte if_g, int thrd, IResponseHandler handler);

	/**
	 * ������Ƶ����������ź�
	 * @param handler
	 */
	public void testRFBlockSignal(IResponseHandler handler);

	/**
	 * �����ŵ�RSSI
	 * @param handler
	 */
	public void testChannelRSSI(IResponseHandler handler);

	/**
	 * ����IO��
	 * @param type �������� 0x00������IO����   0x01������IO��ƽ   0x02����ȡIO��ƽ
	 * @param which  ������IO�� 0x01~0x04
	 * @param io  0x00������ģʽ�����õ͵�ƽ   0x00�����ģʽ�������øߵ�ƽ
	 * @param handler
	 */
	public void controlIO(byte type, byte which, byte io, IResponseHandler handler);

	/**
	 * NXP ReadProtect/Reset ReadProtect
	 * @param type    0x00 ��ReadProtect     0x01��Reset ReadProtect
	 * @param password  ��������
	 * @param handler
	 */
	public void configNXPReadProtect(byte type, byte[] password, IResponseHandler handler);

	/**
	 * NXP Change EAS
	 * @param PSF
	 * @param password
	 * @param handler
	 */
	public void configNXPChangeEAS(boolean PSF, byte[] password, IResponseHandler handler);
	
	/**
	 * NXP EAS_Alarm
	 * @param handler
	 */
	public void NXPEASAlarm(IResponseHandler handler);
	
	/**
	 * �ͷ���Դ
	 */
	public void release();
	
	
}
