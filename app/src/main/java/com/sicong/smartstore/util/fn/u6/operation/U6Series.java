package com.sicong.smartstore.util.fn.u6.operation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.util.fn.u6.model.IResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Message;
import com.sicong.smartstore.util.fn.u6.model.ResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Tag;
import com.sicong.smartstore.util.fn.u6.rfid.manager.Manager;
import com.sicong.smartstore.util.fn.u6.rfid.utils.ArrayUtils;
import com.sicong.smartstore.util.fn.u6.rfid.utils.DataTools;
import com.sicong.smartstore.util.fn.u6.utils.MusicPlayer;
import com.fntech.Loger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import cn.fuen.xmldemo.entity.Device;
import cn.fuen.xmldemo.model.DeviceModel;

public class U6Series implements IUSeries {
	private static final String TAG = "USeries";
	private static U6Series mUSeries;
	private static Context mContext;
	private Handler mHandler = new Handler();
	private static Manager manager;// ������,�򿪴���.����ָ��Ȳ����ڴ�
	public List<Tag> tagList;// �����ѯ���ı�ǩ
	public List<String> epcList;// �����ѯ����epc��,ֻΪ�жϴ˴ζ�ȡ�ı�ǩ�Ƿ��ظ�
	private int id = 0;// ��ѯprimaryKey
	public static final Object THREADLOCK_OBJECT= new Object();// ����ǩ�߳���
	public static final Object GETPARAMS_OBJECT = new Object();// ��ȡ�����߳���
	public static boolean haveNotified;// notify��ʶ

	private static final int SUCCESS = 0;// �ɹ���ʶ
	private static final int FAILURE = 1;// ʧ�ܱ�ʶ
	private boolean executionSucceed = false;// ָ��ִ�н����־
	private String errorInfo;
	public static String readDataString = "";
	private static String getSetParaString = "";
	public static byte[] parameters;
	private static String moduleName = "M10_U6";// loger
	private double ch_freq = 920.125;
	private double div = 0.25;
	private byte ch_index = 0x00;

	public static enum lockOperation {
		LOCK_FREE, LOCK_FREE_EVER, LOCK_LOCK, LOCK_LOCK_EVER
	}

	public static enum SETTYPE {
		SELECTMODE, OPERATIONALAREA, QUERYPARAM, WORKCHANNEL, AUTOFREQHOP, TXPOWER, CONTINUOUSWAVE, MIXER_G, IF_G, THRD
	}

	private U6Series() {
	}

	public static void setContext(Context context) {
		mContext = context;
	}

	public static U6Series getInstance() {
		if (mUSeries == null) {
			mUSeries = new U6Series();
			return mUSeries;
		}
		return mUSeries;
	}

	// Header Type Command PL(MSB) PL(LSB) RSSI PC(MSB) PC(LSB)
	// BB 02 22 00 11 C9 34 00
	// EPC(MSB)
	// 30 75 1F EB 70 5C 59 04
	// EPC(LSB) CRC(MSB) CRC(LSB) Checksum End
	// E3 D5 0D 70 3A 76 EF 7E

	@Override
	public boolean startInventory(final IResponseHandler responseHandler) {
		// TODO Auto-generated method stub
		epcList = new ArrayList<String>();
		tagList = new ArrayList<Tag>();

		try {
			manager.inventoryContinuously(150, new ResponseHandler() {
				@Override
				public synchronized void onSuccess(final String msg, Object data, final byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					Log.e(TAG, "onSuccess", null);
					byte[] copyArray = ArrayUtils.copyArray(parameters, 3, parameters.length - 5);
					String epc = DataTools.Bytes2HexString(copyArray, copyArray.length);
					int repetitionPosition = 0;
					/*MusicPlayer.getInstance().play(MusicPlayer.Type.OK);*/
					if (!epcList.contains(epc)) {
						epcList.add(epc);
						Log.i("see", "inventoryContinuously  EPC:  " + epc);
						byte[] RSSIs = ArrayUtils.copyArray(parameters, 0, 1);
						String RSSI = DataTools.Bytes2HexString(RSSIs, RSSIs.length);
						byte[] PCs = ArrayUtils.copyArray(parameters, 1, 2);
						String PC = DataTools.Bytes2HexString(PCs, PCs.length);
						// �̵���Ϣ
						Tag tag = new Tag();
						tag.id = id;
						tag.count = 1;
						tag.epc = epc;
						tag.pc = PC;
						tag.rssi = RSSI;
						tagList.add(tag);
						id++;
					} else {
						for (int i = 0; i < epcList.size(); i++) {
							if (epcList.get(i).equals(epc)) {
								repetitionPosition = i;
							}
						}
						tagList.get(repetitionPosition).count++;
					}
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							responseHandler.onSuccess(msg, tagList, parameters);

						}
					});
				}

				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					responseHandler.onFailure(msg);
				}
			});
		} catch (Exception e) {
			Loger.disk_log("startInventory", e.getMessage(), moduleName);
			return false;
		}
		return true;
	}

	@Override
	public boolean stopInventory() {
		try {
			manager.stopContinuouslyInventory();
		} catch (Exception e) {
			Loger.disk_log("stopInventory", e.getMessage(), moduleName);
			return false;
		}
		return true;
	}

	@Override
	public Message Inventory() {
		// manager.inventory();
		return null;
	}

	@Override
	public Message readTagMemory(byte[] EPC, byte block, byte w_count, byte w_offset, byte[] acs_pwd) {
		setEPCMatch(EPC);
		readDataString = "";
		int memBank = getMenBank(block);
		Log.e("toolsdebug", "readTagMemory readTagMemory");
		SystemClock.sleep(0);
		haveNotified = false;
		manager.readData(memBank, w_offset, w_count, acs_pwd, new ResponseHandler() {
			@Override
			public void onSuccess(String msg, Object data, byte[] parameters) {
				super.onSuccess(msg, data, parameters);
				Log.i("see", "��ȡ�ɹ� ��Ϣ: " + msg);
				executionSucceed = true;
				Log.i("toolsdebug", "msg = " + msg);
				Log.i("toolsdebug", "data = " + data);
				Log.i("toolsdebug", "parameters = " + DataTools.Bytes2HexString(parameters, parameters.length));
				// synchronized (THREADLOCK_OBJECT) {
				// haveNotified = true;
				// THREADLOCK_OBJECT.notifyAll();
				// }
			}

			@Override
			public void onFailure(String msg) {
				super.onFailure(msg);
				Log.i("see", "��ȡʧ�� ������Ϣ: " + msg);
				Loger.disk_log("readTag", msg, moduleName);
				executionSucceed = false;
				errorInfo = msg;
				// synchronized (THREADLOCK_OBJECT) {
				// haveNotified = true;
				// THREADLOCK_OBJECT.notifyAll();
				// }
			}
		});
		synchronized (THREADLOCK_OBJECT) {
			try {

				while (!haveNotified) {
					THREADLOCK_OBJECT.wait();
				}
			} catch (InterruptedException e) {
				THREADLOCK_OBJECT.notify();
				System.out.println("InterruptedException");
			}
		}
		Message msg = new Message();
		if (executionSucceed) {
			msg.setCode(SUCCESS);
			msg.setResult(readDataString);
			return msg;
		} else {
			msg.setCode(FAILURE);
			msg.setMessage(errorInfo);
			return msg;
		}
	}

	private boolean setEPCMatch(byte[] EPC) {
		haveNotified = false;
		executionSucceed = false;
		if (new String(EPC).equalsIgnoreCase("cancel")) {
			return false;
		}
		try {
			manager.setSelectParams(0, 0, 0, false, DataTools.Bytes2HexString(EPC, EPC.length), new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					executionSucceed = false;
				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					executionSucceed = true;
				}
			});
			synchronized (U6Series.THREADLOCK_OBJECT) {
				try {

					if (!U6Series.haveNotified) {
						U6Series.THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					U6Series.THREADLOCK_OBJECT.notify();
				}
			}

			return executionSucceed;
		} catch (SecurityException e) {
			e.printStackTrace();
			Loger.disk_log("Exception", "setEPCMatchException" + getExceptionAllinformation(e), "M10_U6");
			return false;
		}
	}

	@Override
	public Message writeTagMemory(byte[] EPC, byte block, byte w_count, byte w_offset, byte[] data, byte[] acs_pwd) {
		setEPCMatch(EPC);
		int memBank = getMenBank(block);
		haveNotified = false;
		manager.writeData(memBank, w_offset, acs_pwd, data, new ResponseHandler() {
			@Override
			public void onFailure(String msg) {
				super.onFailure(msg);
				Loger.disk_log("writeTag", msg, moduleName);
				executionSucceed = false;
				errorInfo = msg;
			}

			@Override
			public void onSuccess(String msg, Object data, byte[] parameters) {
				super.onSuccess(msg, data, parameters);
				executionSucceed = true;
			}
		});
		synchronized (THREADLOCK_OBJECT) {
			try {

				if (!haveNotified) {
					THREADLOCK_OBJECT.wait();
				}
			} catch (InterruptedException e) {
				THREADLOCK_OBJECT.notify();
			}
		}
		Message msg = new Message();
		if (executionSucceed) {
			msg.setCode(SUCCESS);
			return msg;
		} else {
			msg.setCode(FAILURE);
			msg.setMessage(errorInfo);
			return msg;
		}
	}

	@Override
	public Message lockTagMemory(byte[] EPC, byte block, Enum operation, byte[] acs_pwd) {
		setEPCMatch(EPC);
		int memBank = getLockBlock(block);
		// getLockBlock(block);
		int lockType = 5;

		if (operation.name().equals(lockOperation.LOCK_FREE.name())) {
			lockType = 0;
		} else if (operation.name().equals(lockOperation.LOCK_FREE_EVER.name())) {
			lockType = 1;
		} else if (operation.name().equals(lockOperation.LOCK_LOCK.name())) {
			lockType = 2;
		} else if (operation.name().equals(lockOperation.LOCK_LOCK_EVER.name())) {
			lockType = 3;
		}
		haveNotified = false;
		manager.lockTag(memBank, lockType, acs_pwd, new ResponseHandler() {
			@Override
			public void onFailure(String msg) {
				super.onFailure(msg);
				Loger.disk_log("lockTag", msg, moduleName);
				executionSucceed = false;
				errorInfo = msg;
			}

			@Override
			public void onSuccess(String msg, Object data, byte[] parameters) {
				super.onSuccess(msg, data, parameters);
				executionSucceed = true;
			}
		});
		synchronized (THREADLOCK_OBJECT) {
			try {

				if (!haveNotified) {
					THREADLOCK_OBJECT.wait();
				}
			} catch (InterruptedException e) {
				THREADLOCK_OBJECT.notify();
			}
		}
		Message msg = new Message();
		if (executionSucceed) {
			msg.setCode(SUCCESS);
			return msg;
		} else {
			msg.setCode(FAILURE);
			msg.setMessage(errorInfo);
			return msg;
		}
	}

	@Override
	public Message killTag(byte[] EPC, byte[] kill_pwd) {
		setEPCMatch(EPC);
		haveNotified = false;
		manager.killTag(kill_pwd, new ResponseHandler() {
			@Override
			public void onFailure(String msg) {
				super.onFailure(msg);
				Loger.disk_log("killTag", msg, moduleName);
				executionSucceed = false;
				errorInfo = msg;
			}

			@Override
			public void onSuccess(String msg, Object data, byte[] parameters) {
				super.onSuccess(msg, data, parameters);
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
				executionSucceed = true;
			}
		});
		synchronized (THREADLOCK_OBJECT) {
			try {

				if (!haveNotified) {
					THREADLOCK_OBJECT.wait();
				}
			} catch (InterruptedException e) {
				THREADLOCK_OBJECT.notify();
			}
		}
		Message msg = new Message();
		if (executionSucceed) {
			msg.setCode(SUCCESS);
			return msg;
		} else {
			msg.setCode(FAILURE);
			msg.setMessage(errorInfo);
			return msg;
		}
	}

	@Override
	public String getParams(String paraName) {
		haveNotified = false;
		parameters = null;
		IResponseHandler responseHandler = new ResponseHandler() {
			@Override
			public void onFailure(String msg) {
				// TODO Auto-generated method stub
				super.onFailure(msg);
				Loger.disk_log("getParams", msg, moduleName);
				Log.i("see", msg);
				Log.i("toolsdebug", "getParams onSuccess");
				synchronized (THREADLOCK_OBJECT) {
					haveNotified = true;
					THREADLOCK_OBJECT.notifyAll();
				}
			}

			@Override
			public void onSuccess(String msg, Object data, byte[] parameters) {
				// TODO Auto-generated method stub
				super.onSuccess(msg, data, parameters);
				Log.i("toolsdebug", "getParams onSuccess");
				synchronized (THREADLOCK_OBJECT) {
					haveNotified = true;
					THREADLOCK_OBJECT.notifyAll();
				}
			}
		};
		if (paraName.equals(SETTYPE.QUERYPARAM.name())) {
			manager.getQueryParams(responseHandler);
			synchronized (THREADLOCK_OBJECT) {
				while (!haveNotified) {
					try {
						System.out.println("Query��ʼ�ȴ�");
						THREADLOCK_OBJECT.wait();
					} catch (InterruptedException e) {
						THREADLOCK_OBJECT.notify();
					}
				}
			}
			System.out.println("Query������");
			if (parameters != null) {
				byte query_sel = (byte) ((parameters[0] >> 2) & 0x03);
				byte query_session = (byte) (parameters[0] & 0x03);
				byte query_target = (byte) ((parameters[1] >> 7) & 0x01);
				byte query_q = (byte) ((parameters[1] >> 3) & 0x0F);
				return byte2String(query_sel, query_session, query_target, query_q);
			} else {
				return null;
			}
		} else if (paraName.equals(SETTYPE.TXPOWER.name())) {
			manager.getRFPower(responseHandler);
			synchronized (THREADLOCK_OBJECT) {
				while (!haveNotified) {
					try {
						// System.out.println("txpower��ʼ�ȴ�");
						THREADLOCK_OBJECT.wait();
					} catch (InterruptedException e) {
						THREADLOCK_OBJECT.notify();
					}
				}
			}
			// System.out.println("TXpower������");
			if (parameters != null) {
				Log.i("toolsdebug", DataTools.Bytes2HexString(parameters, parameters.length));
				int dB = (parameters[0] * 256 + (parameters[1] & 0x000000FF)) / 100;

				try {
					return Integer.toString(dB);
				} catch (Exception e) {
					return null;
				}
			}
			return null;
		} else if (paraName.equals(SETTYPE.WORKCHANNEL.name())) {
			manager.getWorkChannel(responseHandler);
			synchronized (THREADLOCK_OBJECT) {
				while (!haveNotified) {
					try {
						// System.out.println("work cannel��ʼ�ȴ�");
						THREADLOCK_OBJECT.wait();
					} catch (InterruptedException e) {
						THREADLOCK_OBJECT.notify();
					}
				}
			}
			// System.out.println("work cannel������");
			if (parameters != null) {
				byte ch_index = parameters[0];
				double channel = ch_index * div + ch_freq;
				return Double.toString(channel);
			}
			return null;
		} else if (paraName.equals(SETTYPE.THRD.name())) {
			manager.getModemParams(responseHandler);
			synchronized (THREADLOCK_OBJECT) {
				while (!haveNotified) {
					try {
						THREADLOCK_OBJECT.wait();
					} catch (InterruptedException e) {
						THREADLOCK_OBJECT.notify();
					}
				}
			}
			if (parameters != null) {
				byte mixer_g = parameters[0];
				byte if_g = parameters[1];
				int thrd = parameters[2] * 256 + (parameters[3] & 0x00FF);
				return byte2String(mixer_g, if_g) + ":" + Integer.toString(thrd);
			}
			return null;
		}

		return null;
	}

	@Override
	public boolean setParams(String paraName, String paraValue) {
		// Selectģʽ
		if (paraName.equals(SETTYPE.SELECTMODE.name())) {
			manager.setSelectMode(Byte.parseByte(paraValue), new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					// Toast.makeText(mContext, "����ʧ��,������Ϣ: " + msg,
					// Toast.LENGTH_SHORT).show();
					executionSucceed = false;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}

				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
					executionSucceed = true;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}
			});
			synchronized (THREADLOCK_OBJECT) {
				try {
					haveNotified = false;
					while (!haveNotified) {
						THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					THREADLOCK_OBJECT.notify();
				}
			}
			return executionSucceed;

			// ��������
		} else if (paraName.equals(SETTYPE.OPERATIONALAREA.name())) {
			manager.setWorkLocation(Byte.parseByte(paraValue), new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					Loger.disk_log("setParams", msg, moduleName);
					// Toast.makeText(mContext, "����ʧ��,������Ϣ: " + msg,
					// Toast.LENGTH_SHORT).show();
					executionSucceed = false;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
					executionSucceed = true;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}
			});
			synchronized (THREADLOCK_OBJECT) {
				try {
					haveNotified = false;
					while (!haveNotified) {
						THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					THREADLOCK_OBJECT.notify();
				}
			}
			return executionSucceed;
			// �����ŵ�
		} else if (paraName.equals(SETTYPE.WORKCHANNEL.name())) {
			manager.setWorkChannel(Byte.parseByte(paraValue), new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					Loger.disk_log("setParams", msg, moduleName);
					// Toast.makeText(mContext, "����ʧ��,������Ϣ: " + msg,
					// Toast.LENGTH_SHORT).show();
					executionSucceed = false;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
					executionSucceed = true;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}
			});
			synchronized (THREADLOCK_OBJECT) {
				try {
					haveNotified = false;
					while (!haveNotified) {
						THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					THREADLOCK_OBJECT.notify();
				}
			}
			return executionSucceed;
			// Q
		} else if (paraName.equals(SETTYPE.QUERYPARAM.name())) {
			String[] Q = paraValue.split(":");
			byte query_params_msb = Byte.parseByte(Q[0]);
			byte query_params_lsb = Byte.parseByte(Q[1]);
			manager.setQueryParams(new byte[] { query_params_msb, query_params_lsb }, new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					Loger.disk_log("setParams", msg, moduleName);
					// Toast.makeText(mContext, "����ʧ��,������Ϣ: " + msg,
					// Toast.LENGTH_SHORT).show();
					executionSucceed = false;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
					executionSucceed = true;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}
			});
			synchronized (THREADLOCK_OBJECT) {
				try {
					haveNotified = false;
					while (!haveNotified) {
						THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					THREADLOCK_OBJECT.notify();
				}
			}
			return executionSucceed;
		} else if (paraName.equals(SETTYPE.AUTOFREQHOP.name())) {
			manager.setAutoFreqHop(Boolean.parseBoolean(paraValue), new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					Loger.disk_log("setParams", msg, moduleName);
					// Toast.makeText(mContext, "����ʧ��,������Ϣ: " + msg,
					// Toast.LENGTH_SHORT).show();
					executionSucceed = false;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
					executionSucceed = true;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}
			});
			synchronized (THREADLOCK_OBJECT) {
				try {
					haveNotified = false;
					while (!haveNotified) {
						THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					THREADLOCK_OBJECT.notify();
				}
			}
			return executionSucceed;
		} else if (paraName.equals(SETTYPE.TXPOWER.name())) {
			manager.setRFPower(Integer.parseInt(paraValue) * 100, new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					Loger.disk_log("setParams", msg, moduleName);
					// Toast.makeText(mContext, "����ʧ��,������Ϣ: " + msg,
					// Toast.LENGTH_SHORT).show();
					executionSucceed = false;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
					executionSucceed = true;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}
			});
			synchronized (THREADLOCK_OBJECT) {
				try {
					haveNotified = false;
					while (!haveNotified) {
						THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					THREADLOCK_OBJECT.notify();
				}
			}
			return executionSucceed;
		} else if (paraName.equals(SETTYPE.CONTINUOUSWAVE.name())) {
			manager.setContinuousWave(Boolean.parseBoolean(paraValue), new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					Loger.disk_log("setParams", msg, moduleName);
					// Toast.makeText(mContext, "����ʧ��,������Ϣ: " + msg,
					// Toast.LENGTH_SHORT).show();
					executionSucceed = false;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
					executionSucceed = true;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}
			});
			synchronized (THREADLOCK_OBJECT) {
				try {
					haveNotified = false;
					while (!haveNotified) {
						THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					THREADLOCK_OBJECT.notify();
				}
			}
			return executionSucceed;
		} else if (paraName.equals(SETTYPE.THRD.name())) {
			String[] params = paraValue.split(":");
			Log.e("toolsdebug", paraValue);
			byte mixer_g = Byte.parseByte(params[0]);
			byte if_g = Byte.parseByte(params[1]);
			// int thrd = Integer.parseInt(params[2]);
			DataTools.hexStringToBytes(params[2]);
			int thrd = Integer.parseInt(params[2], 16);
			manager.setModemParams(mixer_g, if_g, thrd, new ResponseHandler() {
				@Override
				public void onFailure(String msg) {
					super.onFailure(msg);
					Loger.disk_log("setParams", msg, moduleName);
					// Toast.makeText(mContext, "����ʧ��,������Ϣ: " + msg,
					// Toast.LENGTH_SHORT).show();
					executionSucceed = false;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}

				@Override
				public void onSuccess(String msg, Object data, byte[] parameters) {
					super.onSuccess(msg, data, parameters);
					// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
					executionSucceed = true;
					synchronized (THREADLOCK_OBJECT) {
						haveNotified = true;
						THREADLOCK_OBJECT.notifyAll();
					}
				}
			});
			synchronized (THREADLOCK_OBJECT) {
				try {
					haveNotified = false;
					while (!haveNotified) {
						THREADLOCK_OBJECT.wait();
					}
				} catch (InterruptedException e) {
					THREADLOCK_OBJECT.notify();
				}
			}
			return executionSucceed;
		}
		return false;
	}

	private int getLockBlock(byte block) {
		switch (block) {
		case 0x00:
			return 0;
		case 0x01:
			return 1;
		case 0x02:
			return 2;
		case 0x03:
			return 3;
		case 0x04:
			return 4;

		default:
			break;
		}
		return 5;
	}

	private int getMenBank(byte block) {
		int memBank = 0;
		switch (block) {
		case 0x00:
			memBank = 0;
			break;
		case 0x01:
			memBank = 1;
			break;
		case 0x02:
			memBank = 2;
			break;
		case 0x03:
			memBank = 3;
			break;
		case 0x04:
			memBank = 4;
		default:
			break;
		}
		return memBank;
	}

	private static void saveSetState(String setType, String param) {
		SharedPreferences spf = mContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = spf.edit();
		editor.putString(setType, param);
		editor.commit();
	}

	private static String getSetState(String setType) {
		SharedPreferences spf = mContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		// SharedPreferences.Editor editor = spf.edit();
		// int state = spf.getInt("_session", 0);

		return spf.getString(setType, setType);
	}

	private String byte2String(byte... data) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			builder.append(Byte.toString(data[i]));
			if (i < data.length - 1) {
				builder.append(":");
			}
		}
		return builder.toString();
	}

	@Override
	public Message openSerialPort(String moduleName) {
		Message msg = new Message();
		DeviceModel deviceModel = new DeviceModel(mContext);
		Device device = deviceModel.getDeviceFromModel(moduleName);
		if (device == null) {
			msg.setCode(1);
			msg.setMessage(mContext.getResources().getString(R.string.lose_configurationfile));
			return msg;
		} else {
			try {
				// ReaderAndWriter.DEVICE = device.getSerialPort();
				manager = Manager.getInstance(device.getSerialPort(), device.getBaudRate());
				msg.setCode(0);
				msg.setMessage(mContext.getResources().getString(R.string.success));
				return msg;
			} catch (SecurityException e) {
				e.printStackTrace();
				msg.setCode(1);
				msg.setMessage(String.format(mContext.getResources().getString(R.string.exception_occurred), e.toString()));
				return msg;
			} catch (IOException e) {
				e.printStackTrace();
				msg.setCode(1);
				msg.setMessage(String.format(mContext.getResources().getString(R.string.exception_occurred), e.toString()));
				return msg;
			}
		}

	}

	@Override
	public Message closeSerialPort() {
		Message msg = new Message();
		try {
			manager.release();
		} catch (Exception e) {
			msg.setCode(1);
			return msg;
		}
		msg.setCode(0);
		return msg;
	}

	@Override
	public Message modulePowerOn(String moduleName) {
		Message msg = new Message();
		cn.fuen.xmldemo.model.DeviceModel deviceModel = new cn.fuen.xmldemo.model.DeviceModel(mContext);
		cn.fuen.xmldemo.entity.Device device = deviceModel.getDeviceFromModel(moduleName);
		if (device == null) {
			msg.setCode(1);
			msg.setMessage(mContext.getResources().getString(R.string.lose_configurationfile));
			return msg;
		} else {
			try {
				device.powerOn();
				msg.setCode(0);
				msg.setMessage(mContext.getResources().getString(R.string.success));
				return msg;
			} catch (Exception e) {
				e.printStackTrace();
				msg.setCode(1);
				msg.setMessage(String.format(mContext.getResources().getString(R.string.exception_occurred), e.toString()));
				return msg;
			}
		}
	}

	@Override
	public Message modulePowerOff(String moduleName) {
		Message msg = new Message();
		cn.fuen.xmldemo.model.DeviceModel deviceModel = new cn.fuen.xmldemo.model.DeviceModel(mContext);
		cn.fuen.xmldemo.entity.Device device = deviceModel.getDeviceFromModel(moduleName);
		if (device == null) {
			msg.setCode(1);
			msg.setMessage(mContext.getResources().getString(R.string.lose_configurationfile));
			return msg;
		} else {
			try {
				device.powerOff();
				msg.setCode(0);
				msg.setMessage(mContext.getResources().getString(R.string.success));
				return msg;
			} catch (Exception e) {
				e.printStackTrace();
				msg.setCode(1);
				msg.setMessage(String.format(mContext.getResources().getString(R.string.exception_occurred), e.toString()));
				return msg;
			}
		}
	}

	/**
	 * 异常信息处理
	 * @param ex 异常
	 * @return 异常信息字符串
	 */
	private static String getExceptionAllinformation(Exception ex) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream pout = new PrintStream(out);
		ex.printStackTrace(pout);
		String ret = new String(out.toByteArray());
		pout.close();
		try {
			out.close();
		} catch (Exception e) {
		}
		return ret;
	}
}
