package com.sicong.smartstore.util.fn.u6.rfid.reader;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.sicong.smartstore.util.fn.u6.model.IResponseHandler;
import com.sicong.smartstore.util.fn.u6.operation.U6Series;
import com.sicong.smartstore.util.fn.u6.rfid.constant.Command;
import com.sicong.smartstore.util.fn.u6.rfid.constant.Error;
import com.sicong.smartstore.util.fn.u6.rfid.frame.Frame;
import com.sicong.smartstore.util.fn.u6.rfid.frame.MessageFrame;
import com.sicong.smartstore.util.fn.u6.rfid.frame.ResponseFrame;
import com.sicong.smartstore.util.fn.u6.rfid.utils.DataTools;
import com.fntech.Loger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class ReaderAndWriter implements IReaderAndWriter {

	public static String DEVICE = "/dev/ttySAC2";

	/**
	 * ��Ϣ����
	 */
	private Handler mHandler;

	/**
	 * ����
	 */
	private SerialPort seriapPort;

	/**
	 * �����
	 */
	private OutputStream os;

	/**
	 * ������
	 */
	private InputStream is;

	/**
	 * ��ʱʱ�䣬Ĭ��Ϊ1000ms
	 */
	private int timeout = 2000;

	private boolean isReceiving = false;

	private static ReaderAndWriter raw;

	private boolean isInventory = false;

	private boolean shouldExit = false;

	private String data;
	private Object readLockObject = U6Series.THREADLOCK_OBJECT;
	private Object setParamLock = U6Series.GETPARAMS_OBJECT;

	public static ReaderAndWriter getInstance(String serialPort, int baudrate) throws SecurityException, IOException {
		if (raw == null) {
			raw = new ReaderAndWriter(serialPort, baudrate);
		}
		return raw;
	}

	private ReaderAndWriter(String serialPort, int baudrate) throws SecurityException, IOException {
		seriapPort = new SerialPort(new File(serialPort), baudrate, 0);
		os = seriapPort.getOutputStream();
		is = seriapPort.getInputStream();
		mHandler = new Handler();
		getRecvData();
	}

	@Override
	public boolean sendCmd(byte[] cmd) {
		Log.i("toolsdebug", "����֡��" + DataTools.Bytes2HexString(cmd, cmd.length));
		// System.out.println("����֡��" + DataTools.Bytes2HexString(cmd,
		// cmd.length));
		try {
			os.write(cmd);
			Loger.disk_log("Write", cmd, "M10_U6");
			os.flush();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public void sendCmd(byte[] cmd, IResponseHandler handler) {
		Log.i("toolsdebug", "����֡��" + DataTools.Bytes2HexString(cmd, cmd.length));
		if (isInventory) {
			isInventory = false;
			shouldExit = true;
			SystemClock.sleep(100);
		}

		// if (isReceiving) {
		// System.out.println("isReceiving");
		// return;
		// }

		if (!sendCmd(cmd)) {
			// ���ָ��֡����ʧ��
			System.out.println("���ָ��֡����ʧ��");
			synchronized (readLockObject) {
				U6Series.haveNotified = true;
				readLockObject.notifyAll();
			}
			if (handler != null) {
				handler.onFailure(Error.COMMAND_SEND_FAILED);
				/*
				 * mHandler.post(new Runnable() {
				 * 
				 * @Override public void run() { // �����̵߳��øûص��������Ա���ʵ�ֵĵط����д���
				 * handler.onFailure(Error.COMMAND_SEND_FAILED); } });
				 */
			}
			return;
		} else {

		}
		// ָ��֡���ͳɹ�����ʼ��������
		recvData(cmd[2], handler);
	}

	@Override
	public void sendCmd(byte command, byte[] cmd, final IResponseHandler handler) {
		Log.i("toolsdebug", "����֡��" + DataTools.Bytes2HexString(cmd, cmd.length));
		if (isInventory) {
			isInventory = false;
			shouldExit = true;
			SystemClock.sleep(100);
		}

		if (isReceiving) {
			return;
		}

		if (!sendCmd(cmd)) {
			// ���ָ��֡����ʧ��
			synchronized (readLockObject) {
				U6Series.haveNotified = true;
				readLockObject.notifyAll();
			}
			if (handler != null) {
				handler.onFailure(Error.COMMAND_SEND_FAILED);
				/*
				 * mHandler.post(new Runnable() {
				 * 
				 * @Override public void run() { // �����̵߳��øûص��������Ա���ʵ�ֵĵط����д���
				 * handler.onFailure(Error.COMMAND_SEND_FAILED); } });
				 */
			}
			return;
		}

		// ָ��֡���ͳɹ�����ʼ��������
		recvData(command, handler);
		synchronized (readLockObject) {
			try {
				readLockObject.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				readLockObject.notifyAll();
			}
		}
	}

	public void stopInventory() {
		isInventory = false;
		shouldExit = true;
		SystemClock.sleep(100);
	}

	@Override
	public void recvData(final byte command, final IResponseHandler handler) {
		if (isReceiving) {
			return;
		}

		isReceiving = true;
		shouldExit = false;
		isInventory = false;
		if (command == Command.COMMAND_INVENTORY || command == Command.COMMAND_INVENTORY_MORE) {
			// ����ǵ�����ѯ������ѯ���̲߳�Ӧ���˳���Ӧ�����������б�ǩ���˳�
			isInventory = true;
		}

		// ���̶߳�ȡ��Ӧ����
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("���߳̿�ʼִ�У�");
				SystemClock.sleep(100);
				boolean isTimeout = false;
				long startTime = System.currentTimeMillis();
				while ((isInventory ? true : !isTimeout) && (!shouldExit)) {
					SystemClock.sleep(10);
					long endTime = System.currentTimeMillis();
					if (endTime - startTime >= timeout && !isInventory) {
						// ��ʱ
						if (handler != null) {
							handler.onFailure(Error.RECEIVE_RESPONSE_TIMEOUT);
							/*
							 * mHandler.post(new Runnable() {
							 * 
							 * @Override public void run() {
							 * handler.onFailure(Error
							 * .RECEIVE_RESPONSE_TIMEOUT); } });
							 */
						}
						isTimeout = true;
						continue;
					}

					try {
						// ��ʼ��������
						int count = -1;
						count = is.available();
						// ���������ݿ���
						if (count > 0) {
							// �ȶ���ʼ5���ֽڣ��ж�֡ͷ�ͼ����������
							byte[] header = new byte[5];
							is.read(header, 0, 5);
							Loger.disk_log("Read:header", header, "M10_U6");
							// �ж�֡ͷ
							if (header[0] != Frame.HEADER) {
								// ֡ͷ���󣬲��ټ�����������һ��ѭ��
								continue;
							}

							// �ж�֡����
							if (header[1] != Frame.TYPE_RESPONSE && header[1] != Frame.TYPE_MESSAGE) {
								// ������Ӧ֡��֪ͨ֡��������һ��ѭ��
								continue;
							}

							// �����������
							int pl = header[3] * 256 + header[4];
							// ��ȡ��������Ӧ֡����ʼλ��5������pl+2(�����֡β)
							byte[] frame = new byte[7 + pl];
							frame[0] = header[0];
							frame[1] = header[1];
							frame[2] = header[2];
							frame[3] = header[3];
							frame[4] = header[4];

							is.read(frame, 5, pl + 2);
							Loger.disk_log("Read:", frame, "M10_U6");

							System.out.println("��Ӧ֡��" + DataTools.Bytes2HexString(frame, frame.length));

							if (header[2] == command) {
								// ����ִ�гɹ���У���ȡ����
								if (Frame.checkPacket(frame)) {
									byte type = Frame.getFrameType(frame);
									if (type == Frame.TYPE_MESSAGE) {
										Log.i("toolsdebug", "TYPE_MESSAGE");
										// ֪ͨ֡
										final MessageFrame msgFrame = new MessageFrame(frame);
										if (msgFrame.checkCheckSum()) {
											if (msgFrame.parameters != null) {
												U6Series.parameters = msgFrame.parameters;

											}
											// У��ɹ���ȡ����
											if (handler != null) {
												/*
												 * mHandler.post(new Runnable()
												 * {
												 * 
												 * @Override public void run() {
												 * String data = null; if
												 * (msgFrame.parameters != null)
												 * { data =
												 * Tools.Bytes2HexString
												 * (msgFrame.parameters,
												 * msgFrame.parameters.length);
												 * } handler.onSuccess(Error.
												 * COMMAND_SUCCEED, data,
												 * msgFrame.parameters); } });
												 */
												String data = null;
												if (msgFrame.parameters != null) {
													data = DataTools.Bytes2HexString(msgFrame.parameters, msgFrame.parameters.length);
												}
												handler.onSuccess(Error.COMMAND_SUCCEED, data, msgFrame.parameters);
											}
										} else {
											// У��ʧ��
											if (handler != null) {
												/*
												 * mHandler.post(new Runnable()
												 * {
												 * 
												 * @Override public void run() {
												 * handler
												 * .onFailure(Error.CHECK_SUM_ERROR
												 * ); // handler.onError((byte)
												 * // -2, //
												 * Error.CHECK_SUM_ERROR);
												 * 
												 * } });
												 */
												handler.onFailure(Error.CHECK_SUM_ERROR);
											}
										}
									} else

									if (type == Frame.TYPE_RESPONSE) {
										Log.i("toolsdebug", "TYPE_RESPONSE");
										// ��Ӧ֡
										final ResponseFrame resFrame = new ResponseFrame(frame);
										if (resFrame.checkCheckSum()) {
											if (resFrame.parameters != null) {
												U6Series.parameters = resFrame.parameters;
												U6Series.readDataString = DataTools.Bytes2HexString(resFrame.parameters, resFrame.parameters.length);
											}
											// У��ɹ�
											if (handler != null) {
												String data = null;
												if (resFrame.parameters != null) {
													data = DataTools.Bytes2HexString(resFrame.parameters, resFrame.parameters.length);
												}

												handler.onSuccess(Error.COMMAND_SUCCEED, data, resFrame.parameters);
												/*
												 * mHandler.post(new Runnable()
												 * {
												 * 
												 * @Override public void run() {
												 * String data = null; if
												 * (resFrame.parameters != null)
												 * { data =
												 * Tools.Bytes2HexString
												 * (resFrame.parameters,
												 * resFrame.parameters.length);
												 * } handler.onSuccess(Error.
												 * COMMAND_SUCCEED, data,
												 * resFrame.parameters); } });
												 */
											}
										} else {
											// У��ʧ��
											if (handler != null) {
												handler.onFailure(Error.CHECK_SUM_ERROR);
												/*
												 * mHandler.post(new Runnable()
												 * {
												 * 
												 * @Override public void run() {
												 * handler
												 * .onFailure(Error.CHECK_SUM_ERROR
												 * ); // handler.onError((byte)
												 * // -2, //
												 * Error.CHECK_SUM_ERROR); } });
												 */
											}
										}
										break;
									}

								} else {
									continue;
								}

							} else if (header[2] == Frame.COMMAND_FAILED) {
								Log.i("toolsdebug", "COMMAND_FAILED");
								// ����ִ��ʧ�ܣ�У���ȡ������
								if (Frame.checkPacket(frame)) {
									// ������Ӧ֡
									byte type = Frame.getFrameType(frame);
									if (type == Frame.TYPE_RESPONSE) {
										final ResponseFrame resFrame = new ResponseFrame(frame);
										if (resFrame.checkCheckSum()) {
											if (handler != null) {
												byte errorCode = resFrame.parameters[0];
												handler.onFailure(Error.getErrorMessage(errorCode));
												System.out.println("onFailure");
												/*
												 * mHandler.post(new Runnable()
												 * {
												 * 
												 * @Override public void run() {
												 * byte errorCode =
												 * resFrame.parameters[0];
												 * handler
												 * .onFailure(Error.getErrorMessage
												 * (errorCode)); //
												 * handler.onError(errorCode, //
												 * Error
												 * .getErrorMessage(errorCode));
												 * } });
												 */
											}
										} else {
											// У��ʧ��
											if (handler != null) {
												handler.onFailure(Error.CHECK_SUM_ERROR);
												/*
												 * mHandler.post(new Runnable()
												 * {
												 * 
												 * @Override public void run() {
												 * handler
												 * .onFailure(Error.CHECK_SUM_ERROR
												 * ); // handler.onError((byte)
												 * // -2, //
												 * Error.CHECK_SUM_ERROR); } });
												 */
											}
										}
									}
								} else {
									continue;
								}
							} else {
								// ��������
								continue;
							}

							// ��������˳�ѭ�����������ѯ���˳�
							if (!isInventory) {
								Log.i("toolsdebug", "!isInventory");
								break;
							} else {
								continue;
							}
						}
					} catch (IOException e) {
						if (handler != null) {
							handler.onFailure(Error.RECEIVE_RESPONSE_IOEXCEPTION);
						}
					}
				}
				// if (handler != null) {
				// mHandler.post(new Runnable() {
				// @Override
				// public void run() {
				// // handler.onFinish();
				// }
				// });
				// }
				isReceiving = false;
				Log.i("toolsdebug", "111111111111111111");
				synchronized (readLockObject) {
					readLockObject.notifyAll();
					U6Series.haveNotified = true;
				}
				Log.i("toolsdebug", "222222222222222");
				System.out.println("���߳�ִ�����");
			}
		}).start();
	}

	@Override
	public void setTimeout(int ms) {
		if (ms <= 0 || ms >= 5000) {
			timeout = 5000;
			return;
		}

		timeout = ms;
	}

	@Deprecated
	@Override
	public byte[] getRecvData() {
		int count = 0;
		int timeout = 0;
		byte[] recvData = null;
		try {
			while (count < 1) {
				count = is.available();
				if (timeout > 50) {
					return null;
				} else {
					timeout++;
					SystemClock.sleep(10);
				}
			}
			count = is.available();
			recvData = new byte[count];
			is.read(recvData);
			SystemClock.sleep(50);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return recvData;
	}

	@Override
	public void release() {
		try {
			raw = null;
			is.close();
			os.close();
			seriapPort.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			raw = null;
			is = null;
			os = null;
			seriapPort = null;
		}
	}
}
