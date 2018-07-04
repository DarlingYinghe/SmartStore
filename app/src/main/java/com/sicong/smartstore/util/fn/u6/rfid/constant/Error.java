package com.sicong.smartstore.util.fn.u6.rfid.constant;

/**
 * ����ִ֡��ʧ�ܴ�����
 * 
 * @author huwentao
 * 
 */
public class Error {

	/**
	 * ������Ϣ ָ��֡����ʧ�ܣ�IO�쳣
	 */
	public static final String COMMAND_SEND_FAILED = "ָ��֡����ʧ�ܣ�IO�쳣!";

	/**
	 * ������Ϣ ������Ӧ֡ʱ����IO�쳣
	 */
	public static final String RECEIVE_RESPONSE_IOEXCEPTION = "������Ӧ֡ʱ����IO�쳣!";

	/**
	 * ������Ϣ ������Ӧ֡��ʱ
	 */
	public static final String RECEIVE_RESPONSE_TIMEOUT = "������Ӧ֡��ʱ!";

	/**
	 * ������Ϣ У��Ͳ���ȷ
	 */
	public static final String CHECK_SUM_ERROR = "У��Ͳ���ȷ!";

	/**
	 * ������Ϣ ����ִ�гɹ�
	 */
	public static final String COMMAND_SUCCEED = "����ִ�гɹ�";

	/**
	 * ����֡��ָ��������
	 */
	public static final byte COMMAND_ERROR = 0x17;
	public static final byte FHSS_FAIL = 0x20;
	public static final byte INVENTORY_FAIL = 0x15;
	public static final byte ACCESS_FAIL = 0x16;
	public static final byte READ_FAIL = 0x09;
	public static final byte WRITE_FAIL = 0x10;
	public static final byte LOCK_FAIL = 0x13;
	public static final byte KILL_FAIL = 0x12;

	public static final byte READ_ERROR_OTHER_ERROR = (byte) 0xA0 | 0x00;
	public static final byte READ_ERROR_MEMORY_OVERRUN = (byte) 0xA0 | 0x03;
	public static final byte READ_ERROR_MEMORY_LOCKED = (byte) 0xA0 | 0x04;
	public static final byte READ_ERROR_INSUFFICIENT_POWER = (byte) 0xA0 | 0x0B;
	public static final byte READ_ERROR_NON_SPECIFIC = (byte) 0xA0 | 0x0F;

	public static final byte WRITE_ERROR_OTHER_ERROR = (byte) 0xB0 | 0x00;
	public static final byte WRITE_ERROR_MEMORY_OVERRUN = (byte) 0xB0 | 0x03;
	public static final byte WRITE_ERROR_MEMORY_LOCKED = (byte) 0xB0 | 0x04;
	public static final byte WRITE_ERROR_INSUFFICIENT_POWER = (byte) 0xB0 | 0x0B;
	public static final byte WRITE_ERROR_NON_SPECIFIC = (byte) 0xB0 | 0x0F;

	public static final byte LOCK_ERROR_OTHER_ERROR = (byte) 0xC0 | 0x00;
	public static final byte LOCK_ERROR_MEMORY_OVERRUN = (byte) 0xC0 | 0x03;
	public static final byte LOCK_ERROR_MEMORY_LOCKED = (byte) 0xC0 | 0x04;
	public static final byte LOCK_ERROR_INSUFFICIENT_POWER = (byte) 0xC0 | 0x0B;
	public static final byte LOCK_ERROR_NON_SPECIFIC = (byte) 0xC0 | 0x0F;

	public static final byte KILL_ERROR_OTHER_ERROR = (byte) 0xD0 | 0x00;
	public static final byte KILL_ERROR_MEMORY_OVERRUN = (byte) 0xD0 | 0x03;
	public static final byte KILL_ERROR_MEMORY_LOCKED = (byte) 0xD0 | 0x04;
	public static final byte KILL_ERROR_INSUFFICIENT_POWER = (byte) 0xD0 | 0x0B;
	public static final byte KILL_ERROR_NON_SPECIFIC = (byte) 0xD0 | 0x0F;

	public static String getErrorMessage(byte errorCode) {
		String message = null;

		switch (errorCode) {
		case COMMAND_ERROR:
			message = "����֡��ָ��������";
			break;

		case FHSS_FAIL:
			message = "��Ƶ�����ŵ���ʱ��";
			break;

		case INVENTORY_FAIL:
			message = "��ѯ��ǩʧ�ܣ�";
			break;

		case ACCESS_FAIL:
			message = "���ʱ�ǩʧ�ܣ����ܷ����������";
			break;

		case READ_FAIL:
			message = "����ǩ���ݴ洢��ʧ�ܣ�û�����ݷ��ػ�CRCУ��ʧ�ܣ�";
			break;

		case WRITE_FAIL:
			message = "д��ǩ���ݴ洢��ʧ�ܣ�û�����ݷ��ػ�CRCУ��ʧ�ܣ�";
			break;

		case LOCK_FAIL:
			message = "������ǩ���ݴ洢��ʧ�ܣ�";
			break;

		case KILL_FAIL:
			message = "����ǩʧ�ܣ�";
			break;

		case READ_ERROR_OTHER_ERROR:
			message = "����ǩʧ�ܻ�����δ֪����";
			break;

		case READ_ERROR_MEMORY_OVERRUN:
			message = "���ݴ洢�������ڣ���ñ�ǩ��֧��ָ�����ȵ�EPC!";
			break;

		case READ_ERROR_MEMORY_LOCKED:
			message = "���ݴ洢��������������������������״̬Ϊ����д�򲻿ɶ���";
			break;

		case READ_ERROR_INSUFFICIENT_POWER:
			message = "��ǩû���յ��㹻������������д������";
			break;

		case READ_ERROR_NON_SPECIFIC:
			message = "��ǩ��֧��Error-code���أ�";
			break;

		case WRITE_ERROR_OTHER_ERROR:
			message = "д��ǩʧ�ܻ�����δ֪����";
			break;

		case WRITE_ERROR_MEMORY_OVERRUN:
			message = "���ݴ洢�������ڣ���ñ�ǩ��֧��ָ�����ȵ�EPC!";
			break;

		case WRITE_ERROR_MEMORY_LOCKED:
			message = "���ݴ洢��������������������������״̬Ϊ����д�򲻿ɶ���";
			break;

		case WRITE_ERROR_INSUFFICIENT_POWER:
			message = "��ǩû���յ��㹻������������д������";
			break;

		case WRITE_ERROR_NON_SPECIFIC:
			message = "��ǩ��֧��Error-code���أ�";
			break;

		case LOCK_ERROR_OTHER_ERROR:
			message = "����ǩʧ�ܻ�����δ֪����";
			break;

		case LOCK_ERROR_MEMORY_OVERRUN:
			message = "���ݴ洢�������ڣ���ñ�ǩ��֧��ָ�����ȵ�EPC!";
			break;

		case LOCK_ERROR_MEMORY_LOCKED:
			message = "���ݴ洢��������������������������״̬Ϊ����д�򲻿ɶ���";
			break;

		case LOCK_ERROR_INSUFFICIENT_POWER:
			message = "��ǩû���յ��㹻������������д������";
			break;

		case LOCK_ERROR_NON_SPECIFIC:
			message = "��ǩ��֧��Error-code���أ�";
			break;

		case KILL_ERROR_OTHER_ERROR:
			message = "����ǩʧ�ܻ�����δ֪����";
			break;

		case KILL_ERROR_MEMORY_OVERRUN:
			message = "���ݴ洢�������ڣ���ñ�ǩ��֧��ָ�����ȵ�EPC!";
			break;

		case KILL_ERROR_MEMORY_LOCKED:
			message = "���ݴ洢��������������������������״̬Ϊ����д�򲻿ɶ���";
			break;

		case KILL_ERROR_INSUFFICIENT_POWER:
			message = "��ǩû���յ��㹻������������д������";
			break;

		case KILL_ERROR_NON_SPECIFIC:
			message = "��ǩ��֧��Error-code���أ�";
			break;

		default:
			message = "δ֪����";
			break;
		}
		return message;
	}

}
