package com.haui.devicemanagement.util;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.haui.devicemanagement.R;

public class ThemeHelper {

    public static void applyDarkTheme(Activity activity) {
        if (activity == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.parseColor("#0F0F11"));
            activity.getWindow().setNavigationBarColor(Color.parseColor("#0F0F11"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = activity.getWindow().getDecorView();
            int flags = decor.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // Ensure white status bar text
            decor.setSystemUiVisibility(flags);
        }

        View contentView = activity.findViewById(android.R.id.content);
        if (contentView != null) {
            applyDarkTheme(contentView);
        }
    }

    public static void applyDarkTheme(View view) {
        if (view == null) return;

        // 1. Root/Background view styling
        if (view.getId() == android.R.id.content || isGeneralContainer(view)) {
            view.setBackgroundColor(Color.parseColor("#0F0F11"));
        }

        // 2. Class specific styling
        if (view instanceof Toolbar || view instanceof androidx.appcompat.widget.Toolbar) {
            if (view instanceof Toolbar) {
                Toolbar toolbar = (Toolbar) view;
                toolbar.setBackgroundColor(Color.parseColor("#0F0F11"));
                toolbar.setTitleTextColor(Color.WHITE);
                if (toolbar.getNavigationIcon() != null) {
                    toolbar.getNavigationIcon().setTint(Color.WHITE);
                }
            } else {
                androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) view;
                toolbar.setBackgroundColor(Color.parseColor("#0F0F11"));
                toolbar.setTitleTextColor(Color.WHITE);
                if (toolbar.getNavigationIcon() != null) {
                    toolbar.getNavigationIcon().setTint(Color.WHITE);
                }
            }
        } else if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            int id = card.getId();
            // Don't modify special status cards on dashboard
            if (id != R.id.cardPending && id != R.id.cardBorrowed && id != R.id.cardOverdue &&
                id != R.id.cardPendingBorrowStat && id != R.id.cardPendingReturnStat && id != R.id.cardOverdueStat) {
                card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#2C2C34")));
                card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#4A4A58")));
            }
        } else if (view instanceof CardView) {
            CardView card = (CardView) view;
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#2C2C34")));
        } else if (view instanceof TextInputLayout) {
            TextInputLayout til = (TextInputLayout) view;
            til.setBoxBackgroundColor(Color.parseColor("#252529"));
            til.setBoxStrokeColor(Color.parseColor("#444446"));

            // Set focused hint blue, unfocused hint gray
            int[][] hintStates = new int[][] {
                new int[] {android.R.attr.state_focused},
                new int[] {}
            };
            int[] hintColors = new int[] {
                Color.parseColor("#1962D1"),
                Color.parseColor("#8E8E8E")
            };
            ColorStateList hintColorStateList = new ColorStateList(hintStates, hintColors);
            til.setHintTextColor(hintColorStateList);
            til.setDefaultHintTextColor(hintColorStateList);

            // Tint start and end icons to grey
            til.setStartIconTintList(ColorStateList.valueOf(Color.parseColor("#B0B0B0")));
            til.setEndIconTintList(ColorStateList.valueOf(Color.parseColor("#B0B0B0")));

            // Box stroke: blue on focus, dark gray otherwise
            int[][] strokeStates = new int[][] {
                new int[] {android.R.attr.state_focused},
                new int[] {}
            };
            int[] strokeColors = new int[] {
                Color.parseColor("#1962D1"),
                Color.parseColor("#444446")
            };
            til.setBoxStrokeColorStateList(new ColorStateList(strokeStates, strokeColors));

        } else if (view instanceof android.widget.Spinner) {
            final android.widget.Spinner spinner = (android.widget.Spinner) view;
            spinner.setBackgroundResource(R.drawable.bg_spinner);

            // Fix padding so text doesn't overlap the dropdown arrow on the right
            int paddingStart = spinner.getPaddingStart();
            int paddingTop = spinner.getPaddingTop();
            int paddingEnd = spinner.getPaddingEnd();
            int paddingBottom = spinner.getPaddingBottom();
            if (paddingStart < 16) paddingStart = 24;
            if (paddingEnd < 40) paddingEnd = 56; // Reserve space for arrow on right
            if (paddingTop < 8) paddingTop = 12;
            if (paddingBottom < 8) paddingBottom = 12;
            spinner.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);

            // Make popup window themed dark
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                spinner.setPopupBackgroundDrawable(new ColorDrawable(Color.parseColor("#252529")));
            }

            // Intercept selected text changes to force white text color
            spinner.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    if (child instanceof TextView) {
                        ((TextView) child).setTextColor(Color.WHITE);
                    }
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {}
            });

            // Style already-present child views
            for (int i = 0; i < spinner.getChildCount(); i++) {
                View child = spinner.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(Color.WHITE);
                }
            }

        } else if (view instanceof android.widget.CompoundButton) {
            // Handles RadioButton and CheckBox
            android.widget.CompoundButton cb = (android.widget.CompoundButton) view;
            cb.setTextColor(Color.WHITE);
            int[][] states = new int[][] {
                new int[] {android.R.attr.state_checked},
                new int[] {}
            };
            int[] colors = new int[] {
                Color.parseColor("#1962D1"), // checked: accent blue
                Color.parseColor("#8E8E8E")  // unchecked: gray
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cb.setButtonTintList(new ColorStateList(states, colors));
            }

        } else if (view instanceof EditText) {
            EditText et = (EditText) view;
            et.setTextColor(Color.WHITE);
            et.setHintTextColor(Color.parseColor("#8E8E8E"));
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            int color = tv.getCurrentTextColor();
            tv.setTextColor(adjustTextColor(color));
        } else if (view instanceof MaterialButton) {
            MaterialButton btn = (MaterialButton) view;
            btn.setTextColor(Color.WHITE);
            btn.setIconTint(ColorStateList.valueOf(Color.WHITE));
        } else if (view instanceof Button) {
            Button btn = (Button) view;
            btn.setTextColor(Color.WHITE);
        } else if (view instanceof android.widget.ImageView) {
            android.widget.ImageView iv = (android.widget.ImageView) view;
            if (iv.getDrawable() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ColorStateList tintList = iv.getImageTintList();
                    if (tintList != null) {
                        int defaultColor = tintList.getDefaultColor();
                        iv.setImageTintList(ColorStateList.valueOf(adjustIconColor(defaultColor)));
                    } else {
                        // Apply white color filter to untinted icons
                        iv.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                } else {
                    iv.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        }

        // 3. Recurse down children
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyDarkTheme(vg.getChildAt(i));
            }
        }
    }

    public static void applyDarkThemeToDialog(AlertDialog dialog) {
        if (dialog == null) return;
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1C1C1E")));
            View decorView = dialog.getWindow().getDecorView();
            applyDarkTheme(decorView);
        }

        Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (posBtn != null) {
            posBtn.setTextColor(Color.parseColor("#1962D1"));
        }
        Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negBtn != null) {
            negBtn.setTextColor(Color.parseColor("#E74C3C"));
        }
        Button neuBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (neuBtn != null) {
            neuBtn.setTextColor(Color.parseColor("#B0B0B0"));
        }
    }

    private static boolean isGeneralContainer(View view) {
        if (view instanceof ViewGroup) {
            if (view instanceof CardView || view instanceof MaterialCardView ||
                view instanceof Toolbar || view instanceof androidx.appcompat.widget.Toolbar ||
                view instanceof TextInputLayout ||
                view instanceof android.widget.Spinner ||
                view instanceof com.google.android.material.navigation.NavigationView) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Converts a color to its dark-theme-friendly equivalent using HSV math.
     * - Grayscale (low saturation): dark values → white, mid values → light gray
     * - Saturated colors (status colors: red, green, orange, blue) → preserved
     */
    private static int adjustTextColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float saturation = hsv[1];
        float value = hsv[2];

        if (saturation < 0.20f) {
            // Grayscale color
            if (value < 0.40f) {
                // Black or very dark gray → White
                return Color.WHITE;
            } else if (value < 0.85f) {
                // Medium gray → Light gray
                return Color.parseColor("#B0B0B0");
            } else {
                // Already white or near-white → Keep white
                return Color.WHITE;
            }
        }

        // Saturated color (blue, red, green, orange, etc.) → Keep as-is
        return color;
    }

    /**
     * Converts icon tint colors that are dark/grayscale to white for dark theme.
     * Keeps saturated accent colors intact.
     */
    private static int adjustIconColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float saturation = hsv[1];
        float value = hsv[2];

        if (saturation < 0.20f) {
            // Grayscale icon: dark/medium → White
            if (value < 0.85f) {
                return Color.WHITE;
            }
        }
        return color;
    }
}
