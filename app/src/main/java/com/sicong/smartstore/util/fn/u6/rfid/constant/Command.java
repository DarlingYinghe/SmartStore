package com.sicong.smartstore.util.fn.u6.rfid.constant;

/**
 * ָ�����
 * @author huwentao
 */
public class Command {
	
	/**
	 * ��ȡ��д��ģ����Ϣ
	 */
	public static final byte COMMAND_GET_READER_INFO = 0x03;
	
	/**
	 * ������ѯָ��
	 */
	public static final byte COMMAND_INVENTORY = 0x22;
	
	/**
	 * �����ѯָ��
	 */
	public static final byte COMMAND_INVENTORY_MORE = 0x27;
	
	/**
	 * ֹͣ�����ѯָ��
	 */
	public static final byte COMMAND_STOP_INVENTORY_MORE = 0x28;
	
	/**
	 * ����select����ָ��
	 */
	public static final byte COMMAND_SET_SELECT_PARAMS		= 0x0C;
	
	/**
	 * ����Selectģʽָ��
	 */
	public static final byte COMMAND_SET_SELECT_MODE		    = 0x12;
	
	/**
	 * ����ǩ���ݴ洢��
	 */
	public static final byte COMMAND_READ		= 0x39;
	
	/**
	 * д��ǩ���ݴ洢��
	 */
	public static final byte COMMAND_WRITE		= 0x49;
	
	/**
	 * ������ǩ���ݴ洢��
	 */
	public static final byte COMMAND_LOCK		= (byte) 0x82;
	
	/**
	 * ���ٱ�ǩָ��
	 */
	public static final byte COMMAND_KILL			= 0x65;
	
	/**
	 * ��ȡQuery����
	 */
	public static final byte COMMAND_GET_QUERY_PARAMS = 0x0D;
	
	/**
	 * ����Query����
	 */
	public static final byte COMMAND_SET_QUERY_PARAMS = 0x0E;
	
	/**
	 * ���ù���λ��
	 */
	public static final byte COMMAND_SET_WORK_LOCATION = 0x07;
	
	/**
	 * ��ȡ�����ŵ�
	 */
	public static final byte COMMAND_GET_WORK_CHANNEL = (byte) 0xAA;
	
	/**
	 * ���ù����ŵ�
	 */
	public static final byte COMMAND_SET_WORK_CHANNEL = (byte) 0xAB;
	
	/**
	 * �����Զ���Ƶ�����û�ȡ����
	 */
	public static final byte COMMAND_SET_AUTO_HOPPING = (byte) 0xAD;

	/**
	 * ��ȡ��д�����书��
	 */
	public static final byte COMMAND_GET_RF_POWER = (byte) 0xB7;
	
	/**
	 * ���ö�д�����书��
	 */
	public static final byte COMMAND_SET_RF_POWER = (byte) 0xB6;
	
	/**
	 * ���÷��������ز����򿪻�رգ�
	 */
	public static final byte COMMAND_SET_CONTINUOUS_WAVE = (byte) 0xB0;
	
	/**
	 * ��ȡ���ս��������
	 */
	public static final byte COMMAND_GET_MODEM_PARAMS = (byte) 0xF1;
	
	/**
	 * ���ý��ս��������
	 */
	public static final byte COMMAND_SET_MODEM_PARAMS = (byte) 0xF0;
	
	/**
	 * ������Ƶ����������ź�
	 */
	public static final byte COMMAND_TEST_RF_BLOCKING_SIGNAL = (byte) 0xF2;
	
	/**
	 * ������Ƶ����� RSSI �źŴ�С
	 */
	public static final byte COMMAND_TEST_RF_CHANNEL_RSSI = (byte) 0xF3;
	
	/**
	 * ����IO�˿�
	 */
	public static final byte COMMAND_CONTROL_IO = 0x1A;
	
	/**
	 * NXP ReadProtect/Reset ReadProtect
	 */
	public static final byte COMMAND_CONFIG_READPROTECT = (byte) 0xE1;
	
	/**
	 * NXP Change EAS 
	 */
	public static final byte COMMADN_NXP_CHANGE_EAS = (byte) 0xE3;
	
	/**
	 * NXP EAS_Alarm 
	 */
	public static final byte COMMAND_NXP_EAS_ALARM = (byte) 0xE4;
}
