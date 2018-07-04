package com.sicong.smartstore.util.fn.u6.model;

public class InventoryBuf {
	private static InventoryBuf mInventoryBuf;

	private InventoryBuf() {

	}

	public static InventoryBuf getInstance() {
		if (mInventoryBuf == null) {
			mInventoryBuf = new InventoryBuf();
			return mInventoryBuf;
		}
		return mInventoryBuf;
	}
	
	
}
