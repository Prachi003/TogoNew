package com.togocourier.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.togocourier.R;


public class ProgressDialog extends Dialog {
    public ProgressDialog(Context context) {
        super(context, R.style.ProgressBarTheme);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.progress_dialog);
    }
}
