package com.sicong.smartstore.util.fn.u6.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class PromptUtils {

	public static void ShowBaseDialog(Context context, String title,
                                      String msg, String ok, String cancel, final DialogListener listener) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(title).setMessage(msg)
				.setPositiveButton(ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						if (listener != null) {
							listener.ok(dialog);
						} else {
							dialog.dismiss();
						}
					}
				}).setNegativeButton(cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						if (listener != null) {
							listener.cancel(dialog);
						} else {
							dialog.dismiss();
						}
					}
				});

		AlertDialog dialog = dialogBuilder.create();
		dialog.show();
	}

	public interface DialogListener {
		public void ok(DialogInterface dialog);

		public void cancel(DialogInterface dialog);
	}

}
