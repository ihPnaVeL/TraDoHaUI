package com.haui.devicemanagement.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.DeviceDao;
import com.haui.devicemanagement.data.entity.BorrowItem;
import com.haui.devicemanagement.data.entity.Device;
import com.haui.devicemanagement.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class BorrowItemAdapter extends RecyclerView.Adapter<BorrowItemAdapter.ViewHolder> {

    public interface OnAssignClickListener {
        void onAssignClick(BorrowItem item, int deviceId);
    }

    private final List<BorrowItem> items = new ArrayList<>();
    private final boolean showAssignButton;
    private final OnAssignClickListener assignListener;
    private DeviceDao deviceDao;

    public BorrowItemAdapter(boolean showAssignButton, OnAssignClickListener assignListener) {
        this.showAssignButton = showAssignButton;
        this.assignListener = assignListener;
    }

    public void setItems(List<BorrowItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (deviceDao == null) {
            deviceDao = new DeviceDao(DatabaseHelper.getInstance(parent.getContext()));
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrow_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BorrowItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final TextView tvOriginalRequest;
        private final MaterialButton btnAssign;
        private final LinearLayout layoutPhysicalDetails;
        private final TextView tvAssetCode;
        private final TextView tvSerialNumber;
        private final TextView tvConditionOut;
        private final TextView tvAccessoriesOut;
        private final TextView tvUnassignedWarning;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvOriginalRequest = itemView.findViewById(R.id.tvOriginalRequest);
            btnAssign = itemView.findViewById(R.id.btnAssign);
            layoutPhysicalDetails = itemView.findViewById(R.id.layoutPhysicalDetails);
            tvAssetCode = itemView.findViewById(R.id.tvAssetCode);
            tvSerialNumber = itemView.findViewById(R.id.tvSerialNumber);
            tvConditionOut = itemView.findViewById(R.id.tvConditionOut);
            tvAccessoriesOut = itemView.findViewById(R.id.tvAccessoriesOut);
            tvUnassignedWarning = itemView.findViewById(R.id.tvUnassignedWarning);
        }

        public void bind(BorrowItem item) {
            tvOriginalRequest.setText(item.getNote());

            final int reqDeviceId = parseDeviceIdFromNote(item.getNote());
            String resolvedDeviceName = item.getDeviceName();

            // Nếu chưa gán, resolve tên thiết bị loại mong muốn
            if (resolvedDeviceName == null || resolvedDeviceName.isEmpty()) {
                if (reqDeviceId > 0) {
                    Device d = deviceDao.getById(reqDeviceId);
                    if (d != null) {
                        resolvedDeviceName = d.getDeviceName();
                    } else {
                        resolvedDeviceName = "Loại thiết bị ID: " + reqDeviceId;
                    }
                } else {
                    resolvedDeviceName = "Loại thiết bị không xác định";
                }
            }

            tvDeviceName.setText(resolvedDeviceName);

            if (item.getDeviceDetailId() > 0) {
                // Đã gán thiết bị vật lý
                layoutPhysicalDetails.setVisibility(View.VISIBLE);
                tvUnassignedWarning.setVisibility(View.GONE);

                tvAssetCode.setText("Mã TS: " + item.getAssetCode());
                tvSerialNumber.setText("Serial: " + (item.getSerialNumber() != null ? item.getSerialNumber() : "N/A"));
                tvConditionOut.setText("Tình trạng: " + item.getConditionOut());
                tvAccessoriesOut.setText("Phụ kiện: " + item.getAccessoriesOut());

                if (showAssignButton) {
                    btnAssign.setVisibility(View.VISIBLE);
                    btnAssign.setText("Đổi TB");
                } else {
                    btnAssign.setVisibility(View.GONE);
                }
            } else {
                // Chưa gán thiết bị vật lý
                layoutPhysicalDetails.setVisibility(View.GONE);
                tvUnassignedWarning.setVisibility(View.VISIBLE);

                if (showAssignButton) {
                    btnAssign.setVisibility(View.VISIBLE);
                    btnAssign.setText("Gán TB");
                } else {
                    btnAssign.setVisibility(View.GONE);
                }
            }

            btnAssign.setOnClickListener(v -> {
                if (assignListener != null) {
                    assignListener.onAssignClick(item, reqDeviceId);
                }
            });

            SessionManager session = new SessionManager(itemView.getContext());
            boolean isDark = com.haui.devicemanagement.util.ThemeManager.isDarkMode(itemView.getContext());
            if (isDark) {
                if (itemView instanceof androidx.cardview.widget.CardView) {
                    ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(
                            android.graphics.Color.parseColor("#1C1C1E"));
                }
                applyDarkThemeToItem(itemView);
            } else {
                if (itemView instanceof androidx.cardview.widget.CardView) {
                    ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(
                            android.graphics.Color.WHITE);
                }
                applyLightThemeToItem(itemView);
            }
        }

        private void applyDarkThemeToItem(View view) {
            if (view.getId() == R.id.layoutPhysicalDetails) {
                view.setBackgroundColor(android.graphics.Color.parseColor("#2C2C2E"));
            }
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                int id = tv.getId();
                if (id == R.id.tvDeviceName) {
                    tv.setTextColor(android.graphics.Color.WHITE);
                } else if (id == R.id.tvUnassignedWarning) {
                    // Keep red warning
                } else {
                    tv.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
                }
            } else if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) view;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    applyDarkThemeToItem(vg.getChildAt(i));
                }
            }
        }

        private void applyLightThemeToItem(View view) {
            if (view.getId() == R.id.layoutPhysicalDetails) {
                view.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            }
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                int id = tv.getId();
                if (id == R.id.tvDeviceName) {
                    tv.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
                } else if (id == R.id.tvUnassignedWarning) {
                    // Keep red warning
                } else {
                    tv.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
                }
            } else if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) view;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    applyLightThemeToItem(vg.getChildAt(i));
                }
            }
        }

        private int parseDeviceIdFromNote(String note) {
            if (note != null && note.startsWith("Yêu cầu thiết bị loại ID: ")) {
                try {
                    String idStr = note.substring("Yêu cầu thiết bị loại ID: ".length()).trim();
                    return Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        }
    }
}
