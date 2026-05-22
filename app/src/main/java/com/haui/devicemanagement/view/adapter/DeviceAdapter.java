package com.haui.devicemanagement.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.entity.Device;
import com.haui.devicemanagement.view.user.DeviceSearchActivity;
import com.haui.devicemanagement.view.user.BorrowCreateActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
        void onSelectionChanged(List<Integer> selectedDeviceIds);
    }

    private final List<Device> devices = new ArrayList<>();
    private final Set<Integer> selectedIds = new HashSet<>();
    private final boolean isSelectionMode;
    private final OnDeviceClickListener listener;

    public DeviceAdapter(boolean isSelectionMode, OnDeviceClickListener listener) {
        this.isSelectionMode = isSelectionMode;
        this.listener = listener;
    }

    public void setDevices(List<Device> list) {
        devices.clear();
        if (list != null) {
            devices.addAll(list);
        }
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedDeviceIds() {
        return new ArrayList<>(selectedIds);
    }

    public void setSelectedDeviceIds(List<Integer> ids) {
        selectedIds.clear();
        if (ids != null) {
            selectedIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final TextView tvDeviceCode;
        private final TextView tvBrand;
        private final TextView tvAvailableCount;
        private final TextView tvCategory;
        private final MaterialButton btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceCode = itemView.findViewById(R.id.tvDeviceCode);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvAvailableCount = itemView.findViewById(R.id.tvAvailableCount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }

        public void bind(Device device) {
            tvDeviceName.setText(device.getDeviceName());
            tvDeviceCode.setText("Mã: " + device.getDeviceCode());
            tvBrand.setText("Hãng: " + device.getBrand() + " — " + device.getModel());
            tvAvailableCount.setText(String.valueOf(device.getAvailableCount()));
            tvCategory.setText(device.getCategory());

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

            if (isSelectionMode) {
                btnSelect.setVisibility(View.VISIBLE);
                // Vô hiệu hóa chọn nếu hết thiết bị vật lý khả dụng
                if (device.getAvailableCount() <= 0) {
                    btnSelect.setEnabled(false);
                    btnSelect.setText("Hết");
                    if (isDark) {
                        btnSelect.setTextColor(android.graphics.Color.parseColor("#8E8E8E"));
                        btnSelect.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#444446")));
                    }
                } else {
                    btnSelect.setEnabled(true);
                    if (selectedIds.contains(device.getId())) {
                        btnSelect.setText("Đã chọn");
                        btnSelect.setIconResource(android.R.drawable.checkbox_on_background);
                        if (isDark) {
                            btnSelect.setTextColor(android.graphics.Color.parseColor("#1962D1"));
                            btnSelect.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
                            btnSelect.setIconTint(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
                        }
                    } else {
                        btnSelect.setText("Chọn mượn");
                        btnSelect.setIcon(null);
                        if (isDark) {
                            btnSelect.setTextColor(android.graphics.Color.WHITE);
                            btnSelect.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#444446")));
                        }
                    }
                }

                btnSelect.setOnClickListener(v -> {
                    int id = device.getId();
                    if (selectedIds.contains(id)) {
                        selectedIds.remove(id);
                    } else {
                        selectedIds.add(id);
                    }
                    notifyItemChanged(getAdapterPosition());
                    if (listener != null) {
                        listener.onSelectionChanged(new ArrayList<>(selectedIds));
                    }
                });
            } else {
                btnSelect.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }

        private void applyDarkThemeToItem(View view) {
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                int id = tv.getId();
                if (id == R.id.btnSelect) {
                    return;
                }
                if (id == R.id.tvDeviceName) {
                    tv.setTextColor(android.graphics.Color.WHITE);
                } else if (id == R.id.tvCategory) {
                    tv.setTextColor(android.graphics.Color.parseColor("#90CAF9"));
                    tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#1A90CAF9")));
                } else if (id == R.id.tvAvailableCount) {
                    // Do not change white text inside the round green badge
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
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                int id = tv.getId();
                if (id == R.id.btnSelect) {
                    return;
                }
                if (id == R.id.tvDeviceName) {
                    tv.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
                } else if (id == R.id.tvCategory) {
                    tv.setTextColor(itemView.getContext().getResources().getColor(R.color.primary));
                    tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#F0F0F0")));
                } else if (id == R.id.tvAvailableCount) {
                    // White text
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
    }
}
