package com.sicong.smartstore.util.fn.u6.rfid.reader;

import com.sicong.smartstore.util.fn.u6.model.IResponseHandler;

public interface IReaderAndWriter {
	
	/**
	 * ����ָ��֡
	 * @param cmd ָ��֡�ֽ�����
	 * @return  ���ͳɹ�����true���쳣ʱ����false
	 */
	public boolean sendCmd(byte[] cmd);
	
	/**
	 * 
	 * @param cmd
	 * @param handler
	 */
	public void sendCmd(byte[] cmd, IResponseHandler handler);

	/**
	 * ����Ӧ�����ָ��֡����
	 * @param command  ָ�����
	 * @param cmd      ָ��֡�ֽ�����
	 * @param handler ��Ӧ����
	 */
	public void sendCmd(byte command, byte[] cmd, IResponseHandler handler);

	/**
	 * ��������
	 * @param command �������
	 * @param handler     ��Ӧ����
	 */
	public void recvData(byte command, IResponseHandler handler);
	
	/**
	 * ���ö�ȡ��Ӧ�ĳ�ʱʱ��
	 * @param ms ��ʱʱ�� ��λ������
	 */
	public void setTimeout(int ms);
	
	/**
	 * ��ȡ��������
	 * @return ���ص��ֽ�����
	 * @deprecated
	 */
	public byte[] getRecvData();
	
	/**
	 * �ͷ���Դ
	 */
	public void release();
	
}
