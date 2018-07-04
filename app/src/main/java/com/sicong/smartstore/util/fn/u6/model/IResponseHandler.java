package com.sicong.smartstore.util.fn.u6.model;

public interface IResponseHandler {

	/**
	 * �ɹ� msg �ɹ�ʱ���ص���Ϣ data �ɹ�ʱ���ص����ݣ���ת��Ϊ�ַ��� parameters
	 * �ɹ�ʱ���ص���Ӧ֡�е�Parameters�������
	 */
	public void onSuccess(String msg, Object data, byte[] parameters);

	/**
	 * ʧ�� msg ʧ�ܵ���Ϣ
	 */
	public void onFailure(String msg);

}
