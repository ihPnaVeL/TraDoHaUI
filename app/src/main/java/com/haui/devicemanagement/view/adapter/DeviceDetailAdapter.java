package com.haui.devicemanagement.view.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.entity.DeviceDetail;
import com.haui.devicemanagement.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetailAdapter extends RecyclerView.Adapter<DeviceDetailAdapter.ViewHolder> {

    public interface OnDeviceDetailClickListener {
        void onEditClick(DeviceDetail detail);
        void onDeleteClick(DeviceDetail detail);
    }

    private final List<DeviceDetail> list = new ArrayList<>();
    private final OnDeviceDetailClickListener listener;

    public DeviceDetailAdapter(OnDeviceDetailClickListener listener) {
        this.listener = listener;
    }

    public void setDetails(List<DeviceDetail> items) {
        list.clear();
        if (items != null) {
            list.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAssetCode;
        private final TextView tvDeviceName;
        private final TextView tvSerial;
        private final TextView tvRoom;
        private final TextView tvStatus;
        private final TextView tvCondition;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAssetCode = itemView.findViewById(R.id.tvAssetCode);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvSerial = itemView.findViewById(R.id.tvSerial);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(DeviceDetail detail) {
            com.haui.devicemanagement.util.ThemeHelper.applyDarkTheme(itemView);
            tvAssetCode.setText("Mã TS: " + detail.getAssetCode());
            tvDeviceName.setText(detail.getDeviceName() != null ? detail.getDeviceName() : "Loại thiết bị: #" + detail.getDeviceId());
            tvSerial.setText("Serial: " + (detail.getSerialNumber() != null && !detail.getSerialNumber().isEmpty() ? detail.getSerialNumber() : "N/A"));
            tvRoom.setText("Vị trí: " + (detail.getRoomLocation() != null && !detail.getRoomLocation().isEmpty() ? detail.getRoomLocation() : "N/A"));

            // Availability Status
            String status = detail.getAvailabilityStatus();
            tvStatus.setText(status.toUpperCase());
            int statusColor = itemView.getContext().getResources().getColor(R.color.grey);
            switch (status) {
                case Constants.DEVICE_AVAILABLE:
                    statusColor = itemView.getContext().getResources().getColor(R.color.status_available);
                    break;
                case Constants.DEVICE_BORROWED:
                    statusColor = itemView.getContext().getResources().getColor(R.color.status_borrowed);
                    break;
                case Constants.DEVICE_MAINTENANCE:
                    statusColor = itemView.getContext().getResources().getColor(R.color.status_maintenance);
                    break;
                case Constants.DEVICE_LOST:
                    statusColor = itemView.getContext().getResources().getColor(R.color.status_lost);
                    break;
            }
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(12f);
            shape.setColor(statusColor);
            tvStatus.setBackground(shape);

            // Physical Condition
            String cond = detail.getConditionStatus();
            tvCondition.setText("Tình trạng: " + cond.toUpperCase());
            int condColor = itemView.getContext().getResources().getColor(R.color.success);
            if (Constants.CONDITION_FAIR.equals(cond)) {
                condColor = itemView.getContext().getResources().getColor(R.color.warning);
            } else if (Constants.CONDITION_DAMAGED.equals(cond)) {
                condColor = itemView.getContext().getResources().getColor(R.color.error);
            }
            GradientDrawable condShape = new GradientDrawable();
            condShape.setCornerRadius(12f);
            condShape.setColor(android.graphics.Color.parseColor("#2C2C2E"));
            tvCondition.setBackground(condShape);
            tvCondition.setTextColor(condColor);

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(detail);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(detail);
            });
        }
    }
}
