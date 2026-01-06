package com.example.agritrack.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    /** Minimal contract to decouple adapter from NotificationsActivity.NotificationItem */
    public interface NotificationItem {
        String getTitle();
        String getMessage();
        boolean isActive();
    }

    private final List<NotificationItem> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notification);
    }

    public NotificationAdapter(List<NotificationItem> notifications, OnNotificationClickListener listener) {
        if (notifications != null) this.notifications.addAll(notifications);
        this.listener = listener;
    }

    public void setNotifications(List<NotificationItem> notifications) {
        this.notifications.clear();
        if (notifications != null) this.notifications.addAll(notifications);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvMessage.setText(item.getMessage());

        if (item.isActive()) {
            holder.ivStatus.setImageResource(R.drawable.ic_notification_active);
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.green_active));
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_notification_inactive);
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.orange_pending));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvStatus;
        ImageView ivStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivStatus = itemView.findViewById(R.id.ivStatus);
        }
    }
}