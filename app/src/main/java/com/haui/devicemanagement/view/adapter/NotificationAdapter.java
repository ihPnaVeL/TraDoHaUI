package com.haui.devicemanagement.view.adapter;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.entity.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private final List<Notification> list = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> items) {
        list.clear();
        if (items != null) {
            list.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
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
        private final LinearLayout layoutContainer;
        private final View viewUnreadIndicator;
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTime;
        private final TextView tvTypeBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutContainer = itemView.findViewById(R.id.layoutContainer);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTypeBadge = itemView.findViewById(R.id.tvTypeBadge);
        }

        public void bind(Notification notif) {
            tvTitle.setText(notif.getTitle());
            tvMessage.setText(notif.getMessage());
            tvTime.setText(notif.getCreatedAt());

            boolean isRead = notif.isRead();
            if (isRead) {
                viewUnreadIndicator.setVisibility(View.GONE);
                tvTitle.setTypeface(null, Typeface.NORMAL);
                tvTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
                layoutContainer.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.white));
            } else {
                viewUnreadIndicator.setVisibility(View.VISIBLE);
                tvTitle.setTypeface(null, Typeface.BOLD);
                tvTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
                // Light grey/blue background tint for unread
                layoutContainer.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.grey_light));
            }

            // Notification Type Badge styling
            String type = notif.getType();
            tvTypeBadge.setText(type.toUpperCase());
            int badgeColor = itemView.getContext().getResources().getColor(R.color.primary);
            switch (type) {
                case "borrow":
                    badgeColor = itemView.getContext().getResources().getColor(R.color.status_pending);
                    break;
                case "return":
                    badgeColor = itemView.getContext().getResources().getColor(R.color.status_completed);
                    break;
                case "overdue":
                    badgeColor = itemView.getContext().getResources().getColor(R.color.status_overdue);
                    break;
                case "system":
                    badgeColor = itemView.getContext().getResources().getColor(R.color.primary);
                    break;
            }
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(8f);
            shape.setColor(badgeColor);
            tvTypeBadge.setBackground(shape);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notif);
                }
            });
        }
    }
}
