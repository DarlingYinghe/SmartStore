package com.sicong.smartstore.util.fn.u6.model;

public abstract class ResponseHandler implements IResponseHandler {
	private static final String TAG = "ResponseHandler";

	@Override
	public void onSuccess(String msg, Object data, byte[] parameters) {
		if (parameters != null) {
			System.out.println("onSuccess---msg:" + msg + " data:" + data
					+ " response:" + parameters.toString());
		} else {
			System.out.println("onSuccess---msg:" + msg + " data:" + data);
		}
	}

	@Override
	public void onFailure(String msg) {
		System.out.println("onFailure---msg:" + msg);
	}

}
