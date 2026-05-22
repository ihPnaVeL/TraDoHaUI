package com.haui.devicemanagement.util;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.haui.devicemanagement.R;

public class ThemeHelper {

    public static void applyDarkTheme(Activity activity) {
        if (activity == null) return;
        boolean isDark = ThemeManager.isDarkMode(activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = isDark ? Color.parseColor("#0F0F11") : ContextCompat.getColor(activity, R.color.primary_dark);
            activity.getWindow().setStatusBarColor(color);
            if (isDark) {
                activity.getWindow().setNavigationBarColor(Color.parseColor("#0F0F11"));
            }
        }

        if (isDark) {
            View contentView = activity.findViewById(android.R.id.content);
            if (contentView != null) {
                applyDarkTheme(contentView);
            }
        }
    }

    public static void applyDarkTheme(View view) {
        if (view == null) return;
        // This method is now only called when isDark is true

        if (view.getId() == android.R.id.content) {
            view.setBackgroundColor(Color.parseColor("#121212"));
        }

        if (view instanceof Toolbar) {
            view.setBackgroundColor(Color.parseColor("#0F0F11"));
        } else if (view instanceof CardView) {
            // Cards background handled in layout
        } else if (view instanceof EditText) {
            EditText et = (EditText) view;
            et.setTextColor(Color.WHITE);
            et.setHintTextColor(Color.parseColor("#8E8E8E"));
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            int color = tv.getCurrentTextColor();
            if (color == Color.BLACK || color == Color.parseColor("#212121")) {
                tv.setTextColor(Color.WHITE);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyDarkTheme(vg.getChildAt(i));
            }
        }
    }

    public static void applyDarkThemeToDialog(AlertDialog dialog) {
        if (dialog == null) return;
        boolean isDark = ThemeManager.isDarkMode(dialog.getContext());
        
        if (isDark) {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1C1C1E")));
            }
            // Title
            TextView tvTitle = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (tvTitle != null) tvTitle.setTextColor(Color.WHITE);

            // Message
            TextView tvMessage = dialog.findViewById(android.R.id.message);
            if (tvMessage != null) tvMessage.setTextColor(Color.parseColor("#B0B0B0"));

            // Buttons
            android.widget.Button btnPos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btnPos != null) btnPos.setTextColor(Color.parseColor("#1E88E5")); // primary_light

            android.widget.Button btnNeg = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (btnNeg != null) btnNeg.setTextColor(Color.parseColor("#8E8E8E"));
        } else {
            // Light mode uses default system colors, but we can ensure they are correct
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            }
            TextView tvTitle = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (tvTitle != null) tvTitle.setTextColor(Color.BLACK);

            TextView tvMessage = dialog.findViewById(android.R.id.message);
            if (tvMessage != null) tvMessage.setTextColor(Color.parseColor("#424242"));

            android.widget.Button btnPos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btnPos != null) btnPos.setTextColor(Color.parseColor("#1565C0")); // primary
        }
    }
}
