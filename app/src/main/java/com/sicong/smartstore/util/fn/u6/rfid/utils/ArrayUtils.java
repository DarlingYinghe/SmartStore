package com.sicong.smartstore.util.fn.u6.rfid.utils;


public class ArrayUtils {
	
	/**
	 * ��ָ����Դ��������ȡ����
	 * @param source Դ����
	 * @param offset  ��Դ�����е��±�
	 * @param len      ��ȡ�ĳ���
	 * @return     ��ȡ������
	 */
	public static byte[] copyArray(byte[] source,int offset,int len)
	{
		if(source  == null)
		{
			throw new RuntimeException("Դ����Ϊ��");
		}
		
		if(offset + len > source.length)
		{
			throw new RuntimeException("��������");
		}
		
		byte[] des = new byte[len];
		for(int i=offset,j=0;j<len;i++,j++)
		{
			des[j] = source[i];
		}
		return des;
	}
}
