package com.sicong.smartstore.util.fn.u6.operation;

import com.sicong.smartstore.util.fn.u6.model.IResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Message;

public interface IUSeries {
	
	/**
	 * �򿪴���
	 * @param moduleName ģ����
	 * @return true_ �򿪴��ڳɹ�,false_ �򿪴���ʧ��
	 */
	Message openSerialPort(String moduleName);

	/**
	 * �رմ���
	 * 
	 * @return true_ �رմ��ڳɹ�,false_ �رմ���ʧ��
	 */
	Message closeSerialPort();

	/**
	 * ģ���ϵ�
	 * @param moduleName ģ����
	 * @return true_ �ϵ�ɹ�,false_ �ϵ�ʧ��
	 */
	Message modulePowerOn(String moduleName);

	/**
	 * ģ���µ�
	 * @param moduleName ģ����
	 * @return true_ �µ�ɹ�,false_ �µ�ʧ��
	 */
	Message modulePowerOff(String moduleName);

	/**
	 * ��ʼ��ѯ
	 * @param responseHandler ��ѯ����ص�
	 * @return true_ ��ʼ��ѯ�ɹ��ɹ�,false_ ��ʼ��ѯʧ��
	 */
	boolean startInventory(IResponseHandler responseHandler);

	/**
	 * ֹͣ��ѯ
	 * @return true_ ֹͣ��ѯ�ɹ�,false_ ֹͣ��ѯʧ��
	 */
	boolean stopInventory();

	/**
	 * ������ѯ
	 * @return ��ѯ���
	 */
	Message Inventory();

	/**
	 * ����ǩ
	 * 
	 * @param block
	 *            ��ȡ����
	 * @param w_count
	 *            ��ȡ����
	 * @param w_offset
	 *            ƫ��
	 * @param acs_pwd
	 *            ��������
	 * @return ��ȡ��ǩ����
	 */
	Message readTagMemory(byte[] EPC, byte block, byte w_count, byte w_offset, byte[] acs_pwd);

	/**
	 * д��ǩ
	 * 
	 * @param block
	 *            д������
	 * @param w_count
	 *            д�볤��
	 * @param w_offset
	 *            ƫ��
	 * @param data
	 *            д������
	 * @param acs_pwd
	 *            ��������
	 * @return �Ƿ�д��ɹ�
	 */
	Message writeTagMemory(byte[] EPC, byte block, byte w_count, byte w_offset, byte[] data, byte[] acs_pwd);

	/**
	 * ����ǩ
	 * 
	 * @param block
	 *            ��������
	 * @param operation
	 *            ��������
	 * @param acs_pwd
	 *            ��������
	 * @return ���ش������
	 */
	Message lockTagMemory(byte[] EPC, byte block, Enum operation, byte[] acs_pwd);

	/**
	 * ���ٱ�ǩ
	 * 
	 * @param kill_pwd
	 *            ��������
	 * @return ���ش������
	 */
	Message killTag(byte[] EPC, byte[] kill_pwd);

	/**
	 * ���ò���
	 * 
	 * @param paraName
	 *            ������(���SDK)
	 * @return
	 */
	String getParams(String paraName);

	/**
	 * ���ò���
	 * 
	 * @param paraName
	 *            ������(���SDK)
	 * @param paraValue
	 *            ����ֵ(���SDK)
	 * @return
	 */
	boolean setParams(String paraName, String paraValue);

}
