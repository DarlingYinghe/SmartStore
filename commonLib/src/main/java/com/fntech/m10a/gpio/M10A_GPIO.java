package com.fntech.m10a.gpio;

import java.io.FileOutputStream;

import com.fntech.Loger;

public class M10A_GPIO {
	public static void BdPowerOn() {
		
		//writeFile("/sys/class/gpio/export", "909");
		writeFile("/sys/class/gpio/gpio909/direction", "out");
		writeFile("/sys/class/gpio/gpio909/value", "1");
		writeFile("/sys/class/gpio/gpio908/direction", "out");
		writeFile("/sys/class/gpio/gpio908/value", "0");
	}
	public static void _433PowerOn(){
		writeFile("/sys/class/gpio/gpio910/direction", "out");
		writeFile("/sys/class/gpio/gpio910/value", "0");
	}
	public static void BdPowerOff() {
		writeFile("/sys/class/gpio/gpio909/direction", "out");
		writeFile("/sys/class/gpio/gpio909/value", "0");
	}

	public static void PowerOn() {
		writeFile("/sys/class/gpio/gpio898/direction", "out");
		writeFile("/sys/class/gpio/gpio898/value", "1");
		Loger.disk_log("PowerOn","上电成功","IDcard");
	}

	public static void PowerOff() {
		writeFile("/sys/class/gpio/gpio898/direction", "out");
		writeFile("/sys/class/gpio/gpio898/value", "0");
	}
	
	public static void _uhf_SwitchSerialPort() {
		writeFile("/sys/class/gpio/gpio910/direction", "out");
		writeFile("/sys/class/gpio/gpio910/value", "0");
		Loger.disk_log("PowerOn","上电成功2","IDcard");
	}
	
	public static void _14443_SwitchSerialPort() {
		writeFile("/sys/class/gpio/gpio908/direction", "out");
		writeFile("/sys/class/gpio/gpio908/value", "1");
	}

	public static void _gama_SwitchSerialPort() {
		writeFile("/sys/class/gpio/gpio908/direction", "out");
		writeFile("/sys/class/gpio/gpio908/value", "0");
	}
	
	public static void _1D_PowerOn()
	{
		writeFile("/sys/bus/platform/devices/odroid-sysfs/bardecoder_enable_switch", "1");
	}
	
	private static void writeFile(String fileName, String writestr) {
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			byte[] bytes = writestr.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			Loger.disk_log("wirtefile","wirtefile", "M10_1D");
			e.printStackTrace();
		}
	}
}
