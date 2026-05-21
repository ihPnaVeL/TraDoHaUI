package com.haui.devicemanagement.view.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.entity.Admin;
import com.haui.devicemanagement.data.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onToggleActive(Object userOrAdmin);
        void onEdit(Object userOrAdmin);
    }

    private final List<Object> items = new ArrayList<>();
    private final OnUserActionListener listener;

    public UserAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<?> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserCode;
        private final TextView tvFullName;
        private final TextView tvSubInfo;
        private final TextView tvEmail;
        private final TextView tvPhone;
        private final TextView tvStatus;
        private final TextView tvRole;
        private final MaterialButton btnToggleActive;
        private final MaterialButton btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserCode = itemView.findViewById(R.id.tvUserCode);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvSubInfo = itemView.findViewById(R.id.tvSubInfo);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnToggleActive = itemView.findViewById(R.id.btnToggleActive);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }

        public void bind(Object item) {
            com.haui.devicemanagement.util.ThemeHelper.applyDarkTheme(itemView);
            boolean isActive;
            if (item instanceof User) {
                User user = (User) item;
                tvUserCode.setText("MSSV: " + user.getMssv());
                tvFullName.setText(user.getFullName());
                tvSubInfo.setText("Lớp: " + user.getClassName() + " — Khoa: " + user.getFaculty());
                tvEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
                tvPhone.setText("SĐT: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
                isActive = user.isActive();
                tvRole.setVisibility(View.GONE);
            } else if (item instanceof Admin) {
                Admin admin = (Admin) item;
                tvUserCode.setText("Mã CB: " + admin.getAdminCode());
                tvFullName.setText(admin.getFullName());
                tvSubInfo.setText("Cấp quyền: " + admin.getPermissionLevel().toUpperCase());
                tvEmail.setText("Email: " + (admin.getEmail() != null ? admin.getEmail() : "N/A"));
                tvPhone.setText("SĐT: " + (admin.getPhone() != null ? admin.getPhone() : "N/A"));
                isActive = admin.isActive();
                tvRole.setVisibility(View.VISIBLE);
                tvRole.setText(admin.isManager() ? "MANAGER" : "STAFF");
            } else {
                return;
            }

            // Set Active Badge
            tvStatus.setText(isActive ? "Hoạt động" : "Khóa");
            int colorRes = isActive ? R.color.status_available : R.color.status_overdue;
            int statusColor = itemView.getContext().getResources().getColor(colorRes);
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(12f);
            shape.setColor(statusColor);
            tvStatus.setBackground(shape);

            btnToggleActive.setText(isActive ? "Khóa" : "Mở khóa");
            btnToggleActive.setIconResource(isActive ? android.R.drawable.ic_secure : android.R.drawable.ic_partial_secure);
            btnToggleActive.setOnClickListener(v -> {
                if (listener != null) listener.onToggleActive(item);
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(item);
            });
        }
    }
}
