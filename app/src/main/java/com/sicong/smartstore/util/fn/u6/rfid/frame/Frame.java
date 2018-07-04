package com.sicong.smartstore.util.fn.u6.rfid.frame;

import com.sicong.smartstore.util.fn.u6.rfid.utils.ArrayUtils;


public  abstract class Frame implements IFrame{
	
	/**
	 * ֡���ͣ�����֡
	 */
	public static final byte TYPE_COMMAND = 0x00;
	
	/**
	 * ֡���ͣ���Ӧ֡
	 */
	public static final byte TYPE_RESPONSE   = 0x01;
	
	/**
	 * ֡���ͣ�֪ͨ֡
	 */
	public static final byte TYPE_MESSAGE     = 0x02;
	
	/**
	 * ����ִ��ʧ��
	 */
	public static final byte COMMAND_FAILED = (byte) 0xFF;
	
	
	/**
	 * ָ��֡֡ͷ
	 */
	public static final byte HEADER = (byte) 0xBB;
	
	/**
	 * ָ��֡֡β
	 */
	public static final byte END       = 0x7E;
	
	/**
	 * ֡�й̶�����ĳ���
	 */
	public static final int FRAME_UNCHANGE_LENGTH = 7;
	
	/**
	 * 
	 * ָ��֡��ʽ
	 * Header 			֡ͷ
	 * Type     			֡����
	 * Command 	ָ�����
	 * PL(MSB) 		ָ��������ȣ���8λ��
	 * PL(LSB) 			ָ��������ȣ���8λ��
	 * Parameter		ָ��������ɱ䳤�ȣ�
	 * Checksum 	У��λ�����㷽ʽ��Type��Parameter���ۼӺ͵����8λ��
	 * End				֡β
	 * 
	 */
	
	/**
	 * ֡ͷ
	 */
	public byte header;
	
	/**
	 * ֡����
	 */
	public byte type;
	
	/**
	 * ָ�����
	 */
	public byte command;
	
	/**
	 * ָ���������
	 */
	public byte pl_msb;
	public byte pl_lsb;
	
	/**
	 * ָ�����
	 */
	public byte[] parameters;
	
	/**
	 * У��λ
	 */
	public byte checkSum;
	
	/**
	 * ֡β
	 */
	public byte end;

	/**
	 * @param type ָ��֡����
	 * @param command ָ�����
	 * @param parameters ָ�����
	 */
	public  Frame(byte type, byte command,byte[] parameters) {
		
		this.header        = HEADER;
		this.type            = type;
		this.command   = command;
		
		if(parameters != null && parameters.length >0)
		{
			this.pl_msb        =  (byte) (parameters.length/256);
			this.pl_lsb          =  (byte) (parameters.length%256);
			this.parameters = parameters;
		}else{
			this.pl_msb = 0x00;
			this.pl_lsb = 0x00;
			this.parameters = null;
		}
		
		this.checkSum   = calcCheckSum();
		this.end             =  END;
	}
	
	/**
	 * ���ֽ����鴴��ָ��֡��֪ͨ֡����Ӧ֡��
	 * @param bytes
	 */
	public Frame(byte[] bytes)
	{
		//ָ��֡����Ϊ7���ֽڣ�7���ֽ�ʱ������Ϊ��
		if(bytes != null && bytes.length >= 7)
		{
			if(bytes.length == 7)
			{
				//������λ��
				this.header = bytes[0];
				this.type = bytes[1];
				this.command = bytes[2];
				this.pl_msb = bytes[3];
				this.pl_lsb = bytes[4];
				this.parameters = null;
				this.checkSum = bytes[5];
				this.end = bytes[6];
			}
			else
			{
				//������Ϊ��
				this.header = bytes[0];
				this.type = bytes[1];
				this.command = bytes[2];
				this.pl_msb = bytes[3];
				this.pl_lsb = bytes[4];
				this.parameters = ArrayUtils.copyArray(bytes, 5, bytes.length-7);
				this.checkSum = bytes[bytes.length - 2];
				this.end = bytes[bytes.length - 1];
			}
		}else
		{
			throw new RuntimeException("����λ�ջ򳤶Ȳ��㣡");
		}
	}
	
	/**
	 * ��������֡����
	 * @return ��������֡
	 */
	public byte[] createCmd()
	{
		int frame_length = getFrameLength();
		byte[] frame = new byte[frame_length];
		
		frame[0] = header;
		frame[1] = type;
		frame[2] = command;
		frame[3] = pl_msb;
		frame[4] = pl_lsb;
		
		if(this.parameters != null && this.parameters.length > 0)
		{
			//ָ��֡����
			for (int i=5,j=0;j<parameters.length;i++,j++)
			{
				frame[i] = parameters[j];
			}
			
			//У��λ
			frame[frame_length-2] = checkSum;
			
			//֡β
			frame[frame_length-1] = end;
			
		}else{
			frame[5] = checkSum;
			frame[6] = end;
		}
		
		return frame;
	}
	
	/**
	 * @return ��������֡�ĳ���
	 */
	private int getFrameLength()
	{
		if(this.parameters != null && this.parameters.length > 0)
		{
		return FRAME_UNCHANGE_LENGTH + parameters.length;
		}
		return FRAME_UNCHANGE_LENGTH;
	}
	
	/**
	 * ����У���
	 * @return CRCУ���
	 */
	public byte calcCheckSum()
	{
		//У��λλ��֡����Type�����һ��ָ��������ۼӺ͵����8λ
		int sum=0;
		//���
		sum = type+command+pl_msb+pl_lsb;
		if(this.parameters != null && this.parameters.length > 0)
		{
			for(int i=0;i<parameters.length;i++)
			{
				sum+=parameters[i];
			}
		}
		//ȡ���8λ
		return (byte) (sum&0x00ff);
	}
	
	@Override
	public boolean checkCheckSum() {
		//�������õ�У����Ƿ�ͷ��ص�У������
		return calcCheckSum() == this.checkSum;
	}
	
	/**
	 * �����Ӧ֡�Ƿ���������
	 * @param bytes  ���յ��ֽ�����
	 * @return  ���ݰ����Ƿ���������
	 */
	public static boolean checkPacket(byte[] bytes)
	{
		if(bytes != null && bytes.length >= 7)
		{
			if(bytes[0] == HEADER && bytes[bytes.length - 1] == END)
			{
				return true;
			}
			else
			{
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * ��ȡ֡����
	 * @return ֡����
	 */
	public static byte getFrameType(byte[] bytes) {
		if(bytes != null && bytes.length >= 7)
		{
			return bytes[1];
		}
		return -1;
	}
}
