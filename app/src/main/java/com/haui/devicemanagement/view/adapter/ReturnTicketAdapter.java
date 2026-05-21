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

import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.entity.ReturnTicket;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class ReturnTicketAdapter extends RecyclerView.Adapter<ReturnTicketAdapter.ViewHolder> {

    public interface OnReturnTicketClickListener {
        void onTicketClick(ReturnTicket ticket);
    }

    private final List<ReturnTicket> tickets = new ArrayList<>();
    private final boolean isAdminView;
    private final OnReturnTicketClickListener listener;

    public ReturnTicketAdapter(boolean isAdminView, OnReturnTicketClickListener listener) {
        this.isAdminView = isAdminView;
        this.listener = listener;
    }

    public void setTickets(List<ReturnTicket> list) {
        tickets.clear();
        if (list != null) {
            tickets.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReturnTicket ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvReturnTicketCode;
        private final TextView tvStatusBadge;
        private final TextView tvBorrowTicketCode;
        private final LinearLayout layoutUserDetails;
        private final TextView tvStudentName;
        private final TextView tvReturnedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReturnTicketCode = itemView.findViewById(R.id.tvReturnTicketCode);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvBorrowTicketCode = itemView.findViewById(R.id.tvBorrowTicketCode);
            layoutUserDetails = itemView.findViewById(R.id.layoutUserDetails);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvReturnedAt = itemView.findViewById(R.id.tvReturnedAt);
        }

        public void bind(ReturnTicket ticket) {
            tvReturnTicketCode.setText(ticket.getTicketCode());
            tvBorrowTicketCode.setText(ticket.getBorrowTicketCode() != null ? ticket.getBorrowTicketCode() : "N/A");
            tvReturnedAt.setText(DateUtils.formatDisplayDateTime(ticket.getReturnedAt()));

            if (true) {
                if (itemView instanceof androidx.cardview.widget.CardView) {
                    ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(
                            android.graphics.Color.parseColor("#1C1C1E"));
                }
                applyDarkThemeToItem(itemView);
            }

            if (isAdminView) {
                layoutUserDetails.setVisibility(View.VISIBLE);
                String details = ticket.getUserFullName() != null ? 
                        ticket.getUserFullName() + " (" + ticket.getUserMssv() + ")" : "MSSV: " + ticket.getUserId();
                tvStudentName.setText(details);
            } else {
                layoutUserDetails.setVisibility(View.GONE);
            }

            // Bind trạng thái
            bindStatus(ticket.getStatus());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTicketClick(ticket);
                }
            });
        }

        private void applyDarkThemeToItem(View view) {
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                int id = tv.getId();
                if (id == R.id.tvReturnTicketCode || id == R.id.tvBorrowTicketCode || id == R.id.tvStudentName || id == R.id.tvReturnedAt) {
                    tv.setTextColor(android.graphics.Color.WHITE);
                } else if (id == R.id.tvStatusBadge) {
                    // Do not modify status badge text colors
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

        private void bindStatus(String status) {
            int colorRes;
            String text;

            switch (status) {
                case Constants.RETURN_PENDING:
                    colorRes = R.color.status_pending;
                    text = "Chờ kiểm tra";
                    break;
                case Constants.RETURN_COMPLETED:
                    colorRes = R.color.status_returned;
                    text = "Hoàn tất trả";
                    break;
                case Constants.RETURN_DAMAGED:
                    colorRes = R.color.status_overdue; // Đỏ/Cam
                    text = "Có thiết bị hỏng";
                    break;
                case Constants.RETURN_LOST:
                    colorRes = R.color.status_rejected; // Đỏ sẫm
                    text = "Mất thiết bị";
                    break;
                default:
                    colorRes = R.color.grey;
                    text = status;
            }

            int color = ContextCompat.getColor(itemView.getContext(), colorRes);
            tvStatusBadge.setText(text);
            tvStatusBadge.setTextColor(color);
            tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }
}
