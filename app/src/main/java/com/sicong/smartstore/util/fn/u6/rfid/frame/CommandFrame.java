package com.sicong.smartstore.util.fn.u6.rfid.frame;

public final class CommandFrame extends Frame {
	
	/**
	 * @param command    ����ָ֡�� 
	 * @param parameters  ����֡����
	 */
	public CommandFrame(byte command, byte[] parameters) {
		super(TYPE_COMMAND, command, parameters);
	}
}
