package com.sicong.smartstore.util.fn.u6.rfid.frame;

public interface IFrame {
	
	/**
	 * ����ָ��֡
	 * @return ָ��֡�ֽ�����
	 */
	public byte[] createCmd();
	
	/**
	 * ����У���
	 * @return У���
	 */
	public byte calcCheckSum();
	
	/**
	 * ���У���
	 * @return У���Ƿ�ͨ��
	 */
	public boolean checkCheckSum();
}
