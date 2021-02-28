package com.cody.ammeter.util;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import androidx.appcompat.app.AlertDialog;

public class LoadingUtil {

    private static AlertDialog showLoading(Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    private static void showLoading(AlertDialog dialog) {
        if (dialog != null) {
            dialog.show();
        }
    }

    private static void hideLoading(AlertDialog dialog) {
        if (dialog != null) {
            dialog.hide();
        }
    }
}
