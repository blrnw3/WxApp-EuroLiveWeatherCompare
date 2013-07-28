package co.uk.wxApp.controllers;

import co.uk.wxApp.models.Data;
import android.content.Context;
import android.widget.Toast;

/**
 * Simplifies the calling of Toast
 * @author Ben LR
 */
public class Toaster {
	
	private int length;
	private Context ctx;
	
	public Toaster(Context ctx, boolean isLong) {
		this.ctx = ctx;
		this.length = isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
//		this.model = model;
	}
	
	public void pop(String text) {
		if(Data.getInstance().toastable()) {
			Toast.makeText(ctx, text, length).show();
		}
	}

}