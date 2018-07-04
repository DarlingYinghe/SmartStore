package com.sicong.smartstore.util.fn.u6.model;

public class Tag {
	public int id;
	public String epc;
	public String pc;
	public int count;
	public String rssi;
	public String riss;
	public Tag(){}
	
	@Override
	public String toString() {
		return "EPC [id=" + id + ", epc=" + epc + "pc="+pc+", count=" + count + "rssi"+rssi+"riss"+riss+"]";
	}
	
	

}
