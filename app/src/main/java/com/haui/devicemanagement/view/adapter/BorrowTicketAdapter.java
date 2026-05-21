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
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class BorrowTicketAdapter extends RecyclerView.Adapter<BorrowTicketAdapter.ViewHolder> {

    public interface OnTicketClickListener {
        void onTicketClick(BorrowTicket ticket);
    }

    private final List<BorrowTicket> tickets = new ArrayList<>();
    private final boolean isAdminView;
    private final OnTicketClickListener listener;

    public BorrowTicketAdapter(boolean isAdminView, OnTicketClickListener listener) {
        this.isAdminView = isAdminView;
        this.listener = listener;
    }

    public void setTickets(List<BorrowTicket> list) {
        tickets.clear();
        if (list != null) {
            tickets.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrow_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BorrowTicket ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTicketCode;
        private final TextView tvStatusBadge;
        private final LinearLayout layoutUserDetails;
        private final TextView tvStudentName;
        private final TextView tvReason;
        private final TextView tvCreatedAt;
        private final TextView tvExpectedReturnDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketCode = itemView.findViewById(R.id.tvTicketCode);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            layoutUserDetails = itemView.findViewById(R.id.layoutUserDetails);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvExpectedReturnDate = itemView.findViewById(R.id.tvExpectedReturnDate);
        }

        public void bind(BorrowTicket ticket) {
            tvTicketCode.setText(ticket.getTicketCode());
            tvReason.setText(ticket.getBorrowReason());
            tvCreatedAt.setText(DateUtils.formatDisplayDateTime(ticket.getCreatedAt()));
            tvExpectedReturnDate.setText(DateUtils.formatDisplayDate(ticket.getExpectedReturnDate()));

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
                if (id == R.id.tvTicketCode || id == R.id.tvStudentName || id == R.id.tvExpectedReturnDate || id == R.id.tvCreatedAt) {
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
                case Constants.BORROW_PENDING:
                    colorRes = R.color.status_pending;
                    text = "Chờ duyệt";
                    break;
                case Constants.BORROW_APPROVED:
                    colorRes = R.color.status_approved;
                    text = "Đã duyệt";
                    break;
                case Constants.BORROW_BORROWED:
                    colorRes = R.color.status_borrowed;
                    text = "Đang mượn";
                    break;
                case Constants.BORROW_RETURNED:
                    colorRes = R.color.status_returned;
                    text = "Đã trả";
                    break;
                case Constants.BORROW_REJECTED:
                    colorRes = R.color.status_rejected;
                    text = "Từ chối";
                    break;
                case Constants.BORROW_OVERDUE:
                    colorRes = R.color.status_overdue;
                    text = "Quá hạn";
                    break;
                case Constants.BORROW_PARTIALLY_RETURNED:
                    colorRes = R.color.status_pending; // hoặc kết hợp
                    text = "Trả một phần";
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
