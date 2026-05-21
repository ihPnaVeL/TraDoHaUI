package com.haui.devicemanagement.view.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.entity.ReturnItem;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReturnItemAdapter extends RecyclerView.Adapter<ReturnItemAdapter.ViewHolder> {

    public interface OnReturnItemCheckListener {
        void onItemCheck(ReturnItem item);
    }

    private final List<ReturnItem> items = new ArrayList<>();
    private final boolean isAdminView;
    private final OnReturnItemCheckListener listener;

    public ReturnItemAdapter(boolean isAdminView, OnReturnItemCheckListener listener) {
        this.isAdminView = isAdminView;
        this.listener = listener;
    }

    public void setItems(List<ReturnItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReturnItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final TextView tvAssetSerial;
        private final MaterialButton btnCheck;
        private final TextView tvCheckStatus;
        private final LinearLayout layoutInspectionDetails;
        private final TextView tvConditionIn;
        private final TextView tvAccessoriesIn;
        private final TextView tvDamageNote;
        private final TextView tvPenalty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvAssetSerial = itemView.findViewById(R.id.tvAssetSerial);
            btnCheck = itemView.findViewById(R.id.btnCheck);
            tvCheckStatus = itemView.findViewById(R.id.tvCheckStatus);
            layoutInspectionDetails = itemView.findViewById(R.id.layoutInspectionDetails);
            tvConditionIn = itemView.findViewById(R.id.tvConditionIn);
            tvAccessoriesIn = itemView.findViewById(R.id.tvAccessoriesIn);
            tvDamageNote = itemView.findViewById(R.id.tvDamageNote);
            tvPenalty = itemView.findViewById(R.id.tvPenalty);
        }

        public void bind(ReturnItem item) {
            tvDeviceName.setText(item.getDeviceName() != null ? item.getDeviceName() : "Thiết bị #" + item.getDeviceDetailId());
            String info = "Mã TS: " + (item.getAssetCode() != null ? item.getAssetCode() : "N/A") + 
                    " | Serial: " + (item.getSerialNumber() != null ? item.getSerialNumber() : "N/A");
            tvAssetSerial.setText(info);

            boolean completed = item.getIsCompleted() == 1;

            if (completed) {
                btnCheck.setVisibility(View.GONE);
                tvCheckStatus.setText("Đã check");
                tvCheckStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_returned));
                tvCheckStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.status_returned)));
                
                layoutInspectionDetails.setVisibility(View.VISIBLE);
                
                // Tình trạng
                String condText = "Tình trạng nhận: ";
                if (Constants.CONDITION_GOOD.equals(item.getConditionIn())) {
                    condText += "Tốt";
                } else if (Constants.CONDITION_DAMAGED.equals(item.getConditionIn())) {
                    condText += "Hỏng";
                } else if ("lost".equals(item.getConditionIn())) {
                    condText += "Mất";
                } else {
                    condText += item.getConditionIn();
                }
                tvConditionIn.setText(condText);

                // Phụ kiện
                tvAccessoriesIn.setText("Phụ kiện trả: " + (item.getAccessoriesIn() != null ? item.getAccessoriesIn() : "N/A"));

                // Ghi chú hỏng hóc
                tvDamageNote.setText("Ghi chú: " + (item.getDamageNote() != null && !item.getDamageNote().isEmpty() ? item.getDamageNote() : "Không"));

                // Phạt bồi thường
                if (item.getPenaltyAmount() > 0) {
                    tvPenalty.setVisibility(View.VISIBLE);
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                    tvPenalty.setText("Bồi thường: " + formatter.format(item.getPenaltyAmount()));
                } else {
                    tvPenalty.setVisibility(View.GONE);
                }
            } else {
                tvCheckStatus.setText("Chờ check");
                tvCheckStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_pending));
                tvCheckStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.status_pending)));
                layoutInspectionDetails.setVisibility(View.GONE);
                
                if (isAdminView) {
                    btnCheck.setVisibility(View.VISIBLE);
                    btnCheck.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onItemCheck(item);
                        }
                    });
                } else {
                    btnCheck.setVisibility(View.GONE);
                }
            }

            SessionManager session = new SessionManager(itemView.getContext());
            if (true) {
                if (itemView instanceof androidx.cardview.widget.CardView) {
                    ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(
                            android.graphics.Color.parseColor("#1C1C1E"));
                }
                applyDarkThemeToItem(itemView);
            }
        }

        private void applyDarkThemeToItem(View view) {
            if (view.getId() == R.id.layoutInspectionDetails) {
                view.setBackgroundColor(android.graphics.Color.parseColor("#2C2C2E"));
            }
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                int id = tv.getId();
                if (id == R.id.tvDeviceName) {
                    tv.setTextColor(android.graphics.Color.WHITE);
                } else if (id == R.id.tvCheckStatus) {
                    // Keep status text colors
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
    }
}
