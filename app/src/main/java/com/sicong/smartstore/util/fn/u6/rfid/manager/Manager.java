package com.sicong.smartstore.util.fn.u6.rfid.manager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sicong.smartstore.util.fn.u6.model.IResponseHandler;
import com.sicong.smartstore.util.fn.u6.rfid.constant.Command;
import com.sicong.smartstore.util.fn.u6.rfid.frame.CommandFrame;
import com.sicong.smartstore.util.fn.u6.rfid.frame.Frame;
import com.sicong.smartstore.util.fn.u6.rfid.frame.MessageFrame;
import com.sicong.smartstore.util.fn.u6.rfid.frame.ResponseFrame;
import com.sicong.smartstore.util.fn.u6.rfid.reader.ReaderAndWriter;
import com.sicong.smartstore.util.fn.u6.rfid.utils.ArrayUtils;
import com.sicong.smartstore.util.fn.u6.rfid.utils.DataTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manager implements IManager {

	private static final String TAG = "Manager";
	private ReaderAndWriter raw;
	private static Manager manager;
	private IResponseHandler handler;
	private int inventory_time;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				inventoryContinuously(inventory_time, handler);
			}
		};
	};

	public static Manager getInstance(String serialPort, int baudrate) throws SecurityException, IOException {
		if (manager == null) {
			manager = new Manager(serialPort,baudrate);
		}
		return manager;
	}

	private Manager(String serialPort, int baudrate) throws SecurityException, IOException {
		raw = ReaderAndWriter.getInstance(serialPort,baudrate);
		setTimeout(1000);
	}

	@Override
	public void setTimeout(int ms) {
		raw.setTimeout(ms);
	}

	@Deprecated
	@Override
	public String queryHardwareVersion() {
		CommandFrame frame = new CommandFrame(Command.COMMAND_GET_READER_INFO, new byte[] { 0x00 });
		byte[] cmd = frame.createCmd();
		if (!raw.sendCmd(cmd)) {
			return null;
		}

		byte[] recvData = raw.getRecvData();
		String hardwareVersion = null;

		if (Frame.checkPacket(recvData)) {
			// ���ݰ���ȷ
			if (Frame.getFrameType(recvData) == Frame.TYPE_RESPONSE) {
				// ����Ӧ֡
				ResponseFrame resFrame = new ResponseFrame(recvData);
				if (resFrame.checkCheckSum()) {
					// У��ɹ�
					if (resFrame.command == Command.COMMAND_GET_READER_INFO) {
						// ����ִ�гɹ�
						if (resFrame.parameters[0] == 0x00) {
							// ��Ӳ���汾
							int dl = (resFrame.pl_msb * 256 + resFrame.pl_msb) - 1;
							byte[] info = ArrayUtils.copyArray(resFrame.parameters, 1, dl);
							hardwareVersion = DataTools.Bytes2HexString(info, dl);
						} else {
							// ����Ӳ���汾
						}
					} else {
						// ����ִ��ʧ��
					}

				} else {
					// У��ʧ��
				}
			} else {
				// ������Ӧ֡
			}
		} else {
			// ���ݰ�����ȷ
		}

		return hardwareVersion;
	}

	@Deprecated
	@Override
	public String queryFirmwareVersion() {
		return null;
	}

	@Deprecated
	@Override
	public List<?> inventory() {
		List<String> list = new ArrayList<String>();
		// ��������֡
		CommandFrame frame = new CommandFrame(Command.COMMAND_INVENTORY, null);
		byte[] cmd = frame.createCmd();

		// ����ָ��
		if (!raw.sendCmd(cmd)) {
			return null;
		}

		// ��������
		byte[] recvData = raw.getRecvData();

		// �����Ƿ���������
		if (Frame.checkPacket(recvData)) {
			// ��ȡ֡����
			byte type = Frame.getFrameType(recvData);
			if (type == Frame.TYPE_MESSAGE) {
				// ֪ͨ֡����ѯ����ǩ
				MessageFrame msgFrame = new MessageFrame(recvData);
				// CRCУ��
				if (msgFrame.checkCheckSum()) {
					// ����ɹ�����ȡepc,12���ֽ�
					byte[] epcBytes = ArrayUtils.copyArray(msgFrame.parameters, 3, 12);
					String epc = DataTools.Bytes2HexString(epcBytes, epcBytes.length);
					list.add(epc);
				} else {
					// У��ʧ��
					return null;
				}
			} else if (type == Frame.TYPE_RESPONSE) {
				// ��Ӧ֡����������
				ResponseFrame resFrame = new ResponseFrame(recvData);
				Log.i(TAG, "û���յ���ǩ��CRCУ����󣬴����룺" + resFrame.parameters[0]);
				return null;
			} else {
				return null;
			}
		}
		return list;
	}

	@Deprecated
	@Override
	public String readData(int memBank, int address, int length, byte[] password) {

		if (password == null || password.length != 4) {
			password = new byte[] { 0x00, 0x00, 0x00, 0x00 };
		}

		// ��������֡����
		byte[] parameters = {
				// Access Password
				password[0], password[1], password[2], password[3],

				// MemBank �洢��
				(byte) memBank,

				// ��ַƫ��
				(byte) (address / 256), (byte) (address % 256),

				// DL ���ݳ���
				(byte) (length / 256), (byte) (length % 256) };

		CommandFrame cmdFrame = new CommandFrame(Command.COMMAND_READ, parameters);

		// ��������
		byte[] cmd = cmdFrame.createCmd();

		// ��������
		raw.sendCmd(cmd);

		// ��ȡ��Ӧ֡
		byte[] recvData = raw.getRecvData();
		String data = null;

		if (Frame.checkPacket(recvData)) {
			// ���ݰ���������
			if (Frame.getFrameType(recvData) == Frame.TYPE_RESPONSE) {
				// ��������Ӧ֡
				ResponseFrame resFrame = new ResponseFrame(recvData);
				if (resFrame.checkCheckSum()) {
					// У��ɹ�
					if (resFrame.command == Command.COMMAND_READ) {
						// �����ݳɹ������ض�ȡ��������
						// �����򳤶�
						int pl = resFrame.pl_msb * 256 + resFrame.pl_lsb;
						// PC+EPC�ĳ���
						int ul = resFrame.parameters[0];
						// ���ݵĳ���
						int dl = pl - (ul + 1);
						// ��ȡ����
						byte[] dataBytes = ArrayUtils.copyArray(resFrame.parameters, ul + 1, dl);
						// ת��ΪString
						data = DataTools.Bytes2HexString(dataBytes, dl);
					} else if (resFrame.command == (byte) 0xFF) {
						// ������ʧ�ܣ����ش�����
						data = DataTools.Bytes2HexString(new byte[] { resFrame.parameters[0] }, 1);
					}
				} else {
					// У��ʧ��
					data = "У��ʧ��";
				}
			} else {
				// ������Ӧ֡
				data = "������Ӧ֡";
			}
		} else {
			// ���ݰ�����������
			data = "���ݰ�������Э��涨";
		}

		return data;
	}

	@Deprecated
	@Override
	public int writeData(int memBank, int address, byte[] password, byte[] dataBytes) {

		// �����������͵����ݲ���Ϊ�գ����ܳ���64���ֽ�
		if (dataBytes == null || dataBytes.length > 64 || dataBytes.length <= 0) {
			return -1;
		}

		int dataLen = dataBytes.length;
		if (dataLen % 2 != 0) {
			// ���Ҫд������ݲ�����˫�ֵ�����
			return -1;
		}

		// ����������������
		if (password == null || password.length != 4) {
			password = new byte[] { 0x00, 0x00, 0x00, 0x00 };
		}

		int paramsLen = 9 + dataLen;
		dataLen = dataLen / 2;

		// ������
		byte[] params = new byte[paramsLen];
		params[0] = password[0];
		params[1] = password[1];
		params[2] = password[2];
		params[3] = password[3];
		params[4] = (byte) memBank;
		params[5] = (byte) (address / 256);
		params[6] = (byte) (address % 256);
		params[7] = (byte) (dataLen / 256);
		params[8] = (byte) (dataLen % 256);

		// Ҫд������
		for (int i = 9, j = 0; j < dataBytes.length; i++, j++) {
			params[i] = dataBytes[j];
		}

		CommandFrame frame = new CommandFrame(Command.COMMAND_WRITE, params);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd);

		int flag = -1;
		byte[] recvData = raw.getRecvData();
		// ��鷵�ص������Ƿ�����Ҫ��
		if (Frame.checkPacket(recvData)) {

			if (Frame.getFrameType(recvData) == Frame.TYPE_RESPONSE) {

				// ������ص�����Ӧ֡
				ResponseFrame resFrame = new ResponseFrame(recvData);

				if (resFrame.checkCheckSum()) {
					// У��ɹ�
					if (resFrame.command == Command.COMMAND_WRITE) {
						// д��ɹ� 0x00
						flag = resFrame.parameters[resFrame.parameters.length - 3];
					} else if (resFrame.command == (byte) 0xFF) {
						// д��ʧ�� ������
						flag = resFrame.parameters[0];
					} else {
						// δ֪����
					}
				} else {
					// У��ʧ��
				}
			} else {
				// ������Ӧ֡
			}
		} else {
			// ���ص����ݰ�����������
		}
		return flag;
	}

	@Deprecated
	@Override
	public boolean lockTag(int memBank, int lockType, byte[] password) {
		return false;
	}

	@Deprecated
	@Override
	public boolean killTag(byte[] password) {
		return false;
	}

	@Override
	public void release() {
		manager = null;
		raw.release();
	}

	/********************************************* �·��� *******************************************************/

	@Override
	public void queryHardwareVersion(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_GET_READER_INFO, new byte[] { 0x00 });
		byte[] cmd = frame.createCmd();
		raw.sendCmd(Command.COMMAND_GET_READER_INFO, cmd, handler);
	}

	@Override
	public void queryFirmwareVersion(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_GET_READER_INFO, new byte[] { 0x01 });
		byte[] cmd = frame.createCmd();
		raw.sendCmd(Command.COMMAND_GET_READER_INFO, cmd, handler);
	}

	@Override
	public void inventory(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_INVENTORY, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(Command.COMMAND_INVENTORY, cmd, handler);
	}

	@Override
	public void inventoryContinuously(int ms, IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_INVENTORY, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd);

		if (this.handler == null) {
			this.inventory_time = ms;
			this.handler = handler;
			raw.recvData(Command.COMMAND_INVENTORY, handler);
		}

		mHandler.sendEmptyMessageDelayed(0, ms);
	}

	/**
	 * ֹͣ�����̴�
	 */
	public void stopContinuouslyInventory() {
		raw.stopInventory();
		mHandler.removeCallbacksAndMessages(null);
		this.handler = null;
	}

	/**
	 * ֹͣ�̴���߳�
	 */
	public void stopInventory() {
		raw.stopInventory();
	}

	@Override
	public void inventoryMore(int count, IResponseHandler handler) {
		if (count <= 0) {
			count = 1;
		}

		if (count >= 65535) {
			count = 65535;
		}

		byte[] params = { 0x22,// ����λ
				(byte) (count / 256), (byte) (count % 256) };

		CommandFrame frame = new CommandFrame(Command.COMMAND_INVENTORY_MORE, params);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(Command.COMMAND_INVENTORY_MORE, cmd, handler);
	}

	@Override
	public void stopInventory(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_STOP_INVENTORY_MORE, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(Command.COMMAND_STOP_INVENTORY_MORE, cmd, handler);
	}

	@Override
	public void setSelectParams(int target, int action, int memBank, boolean isTruncate, String epc, IResponseHandler handler) {
		if (epc == null) {
			return;
		}
		int len = 0;
		byte[] temp = new byte[19];

		byte[] epcBytes = DataTools.HexString2Bytes(epc);

		temp[0] = (byte) 0x01;// (target << 5 | action << 2 | memBank);
		temp[1] = 0x00;
		temp[2] = 0x00;
		temp[3] = 0x00;
		temp[4] = 0x20;
		temp[5] = Byte.decode("0x" + (epcBytes.length / 2) + "0"); // 0x60;
		temp[6] = (byte) (isTruncate ? 0x80 : 0x00);

		len = len + 7;

		for (int i = 7, j = 0; j < epcBytes.length; i++, j++) {
			temp[i] = epcBytes[j];
			len = len + 1;
		}

		byte[] params = new byte[len];

		System.arraycopy(temp, 0, params, 0, params.length);

		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_SELECT_PARAMS, params);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void setSelectMode(byte mode, IResponseHandler handler) {
		if (mode != 0x00 && mode != 0x01 && mode != 0x02) {
			return;
		}
		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_SELECT_MODE, new byte[] { mode });
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void readData(int memBank, int address, int length, byte[] password, IResponseHandler handler) {

		if (password == null || password.length != 4) {
			password = new byte[] { 0x00, 0x00, 0x00, 0x00 };
		}

		// ��������֡����
		byte[] params = {
				// Access Password
				password[0], password[1], password[2], password[3],

				// MemBank �洢��
				(byte) memBank,

				// ��ַƫ��
				(byte) (address / 256), (byte) (address % 256),

				// DL ���ݳ���
				(byte) (length / 256), (byte) (length % 256) };

		CommandFrame cmdFrame = new CommandFrame(Command.COMMAND_READ, params);

		byte[] cmd = cmdFrame.createCmd();

		raw.sendCmd(Command.COMMAND_READ, cmd, handler);
	}

	@Override
	public void writeData(int memBank, int address, byte[] password, byte[] dataBytes, IResponseHandler handler) {

		// �����������͵����ݲ���Ϊ�գ����ܳ���64���ֽ�
		if (dataBytes == null || dataBytes.length > 64 || dataBytes.length <= 0) {
			return;
		}

		int dataLen = dataBytes.length;
		if (dataLen % 2 != 0) {
			// ���Ҫд������ݲ�����˫�ֵ�����
			return;
		}

		// ����������������
		if (password == null || password.length != 4) {
			password = new byte[] { 0x00, 0x00, 0x00, 0x00 };
		}

		int paramsLen = 9 + dataLen;
		dataLen = dataLen / 2;

		// ������
		byte[] params = new byte[paramsLen];
		params[0] = password[0];
		params[1] = password[1];
		params[2] = password[2];
		params[3] = password[3];
		params[4] = (byte) memBank;
		params[5] = (byte) (address / 256);
		params[6] = (byte) (address % 256);
		params[7] = (byte) (dataLen / 256);
		params[8] = (byte) (dataLen % 256);

		// Ҫд������
		for (int i = 9, j = 0; j < dataBytes.length; i++, j++) {
			params[i] = dataBytes[j];
		}

		CommandFrame frame = new CommandFrame(Command.COMMAND_WRITE, params);
		byte[] cmd = frame.createCmd();

		raw.sendCmd(Command.COMMAND_WRITE, cmd, handler);

	}

	@Override
	public void lockTag(int memBank, int lockType, byte[] password, IResponseHandler handler) {

		if (password == null || password.length != 4) {
			password = new byte[] { 0x00, 0x00, 0x00, 0x00 };
		}

		byte lock_type = (byte) lockType; // 0 ,1 ,2 ,3 ,4 0x00 ,0x01 ,0x10
											// ,0x11
		// 00000000 00000001 00000010 00000011 00000100
		StringBuilder data = new StringBuilder("00000000000000000000");
		// String data = "000000000000000000000000";

		// Lock������ LD
		byte[] ld = new byte[3];
		switch (memBank) {
		case 0:// ����������
			if (lockType == 0) {
				data.replace(0, 1, "1");
			} else if (lockType == 1) {
				data.replace(0, 2, "11");
				data.replace(11, 12, "1");
			} else if (lockType == 2) {
				data.replace(0, 1, "1");
				data.replace(10, 11, "1");
			} else if (lockType == 3) {
				data.replace(0, 2, "11");
				data.replace(10, 12, "11");
			}
			break;
		case 1:// ����������
			if (lockType == 0) {
				data.replace(2, 3, "1");
			} else if (lockType == 1) {
				data.replace(2, 4, "11");
				data.replace(13, 14, "1");
			} else if (lockType == 2) {
				data.replace(2, 3, "1");
				data.replace(12, 13, "1");
			} else if (lockType == 3) {
				data.replace(2, 4, "11");
				data.replace(12, 14, "11");
			}
			break;
		case 2:// EPC��
			if (lockType == 0) {
				data.replace(4, 5, "1");
			} else if (lockType == 1) {
				data.replace(4, 6, "11");
				data.replace(15, 16, "1");
			} else if (lockType == 2) {
				data.replace(4, 5, "1");
				data.replace(14, 15, "1");
			} else if (lockType == 3) {
				data.replace(4, 6, "11");
				data.replace(14, 16, "11");
			}
			break;
		case 3:// TID��
			if (lockType == 0) {
				data.replace(6, 7, "1");
			} else if (lockType == 1) {
				data.replace(6, 8, "11");
				data.replace(17, 18, "1");
			} else if (lockType == 2) {
				data.replace(6, 7, "1");
				data.replace(16, 17, "1");
			} else if (lockType == 3) {
				data.replace(6, 8, "11");
				data.replace(16, 18, "11");
			}
			break;
		case 4:// USER��
			if (lockType == 0) {
				data.replace(8, 9, "1");
			} else if (lockType == 1) {
				data.replace(19, 20, "1");
				data.replace(8, 10, "11");
			} else if (lockType == 2) {
				data.replace(8, 9, "1");
				data.replace(18, 19, "1");
			} else if (lockType == 3) {
				data.replace(8, 10, "11");
				data.replace(18, 20, "11");
			}
			break;
		}

		data.insert(0, "0000");
		System.out.println("���������ݳ��ȣ�" + data.length() + data.toString());
		ld[0] = (byte) Integer.parseInt(data.substring(0, 8), 2);
		ld[1] = (byte) Integer.parseInt(data.substring(8, 16), 2);
		ld[2] = (byte) Integer.parseInt(data.substring(16, 24), 2);

		// ��������֡����
		byte[] params = {
				// Access Password 0x00000000
				password[0], password[1], password[2], password[3], ld[0], ld[1], ld[2] };

		// ��������֡����
		CommandFrame cmdFrame = new CommandFrame(Command.COMMAND_LOCK, params);
		// ͨ������֡���󴴽������ֽ�����
		byte[] cmd = cmdFrame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void killTag(byte[] password, IResponseHandler handler) {
		if (password == null || password.length != 4) {
			password = new byte[] { 0x00, 0x00, 0x00, 0x00 };
		}
		// ��������֡����
		CommandFrame cmdFrame = new CommandFrame(Command.COMMAND_KILL, password);
		// ͨ������֡���󴴽������ֽ�����
		byte[] cmd = cmdFrame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void getQueryParams(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_GET_QUERY_PARAMS, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void setQueryParams(byte[] para, IResponseHandler handler) {
		if (para == null || para.length != 2) {
			return;
		}
		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_QUERY_PARAMS, para);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void setWorkLocation(byte location, IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_WORK_LOCATION, new byte[] { location });
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void setWorkChannel(byte ch_index, IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_WORK_CHANNEL, new byte[] { ch_index });
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void getWorkChannel(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_GET_WORK_CHANNEL, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void setAutoFreqHop(boolean isOn, IResponseHandler handler) {
		byte para = (byte) 0xFF;
		if (isOn) {
			para = (byte) 0xFF;
		} else {
			para = 0x00;
		}
		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_AUTO_HOPPING, new byte[] { para });
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void getRFPower(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_GET_RF_POWER, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void setRFPower(int power, IResponseHandler handler) {
		byte[] params = { (byte) (power / 256), (byte) (power % 256) };
		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_RF_POWER, params);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void setContinuousWave(boolean isOn, IResponseHandler handler) {
		byte para = (byte) 0xFF;
		if (isOn) {
			para = (byte) 0xFF;
		} else {
			para = 0x00;
		}
		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_CONTINUOUS_WAVE, new byte[] { para });
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void getModemParams(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_GET_MODEM_PARAMS, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void setModemParams(byte mixer_g, byte if_g, int thrd, IResponseHandler handler) {
		byte[] params = { mixer_g, if_g, (byte) (thrd / 256), (byte) (thrd % 256) };
		CommandFrame frame = new CommandFrame(Command.COMMAND_SET_MODEM_PARAMS, params);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void testRFBlockSignal(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_TEST_RF_BLOCKING_SIGNAL, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void testChannelRSSI(IResponseHandler handler) {
		CommandFrame frame = new CommandFrame(Command.COMMAND_TEST_RF_CHANNEL_RSSI, null);
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void controlIO(byte type, byte which, byte io, IResponseHandler handler) {
		if (type < 0x00 || type > 0x02) {
			return;
		}

		if (which < 0x01 || which > 0x04) {
			return;
		}

		if (io != 0x00 && io != 0x01) {
			return;
		}

		CommandFrame frame = new CommandFrame(Command.COMMAND_CONTROL_IO, new byte[] { type, which, io });
		byte[] cmd = frame.createCmd();
		raw.sendCmd(cmd, handler);
	}

	@Override
	public void configNXPReadProtect(byte type, byte[] password, IResponseHandler handler) {

	}

	@Override
	public void configNXPChangeEAS(boolean PSF, byte[] password, IResponseHandler handler) {

	}

	@Override
	public void NXPEASAlarm(IResponseHandler handler) {

	}
}
