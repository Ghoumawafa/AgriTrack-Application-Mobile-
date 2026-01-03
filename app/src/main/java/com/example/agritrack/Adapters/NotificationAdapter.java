package com.example.agritrack.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Activities.NotificationsActivity;
import com.example.agritrack.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationsActivity.NotificationItem> notifications;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onCancelNotification(long scheduleId);
        void onRescheduleNotification(long scheduleId);
    }

    public NotificationAdapter(List<NotificationsActivity.NotificationItem> notifications,
                               OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    public void setNotifications(List<NotificationsActivity.NotificationItem> notifications) {
        this.notifications = notifications;
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
        NotificationsActivity.NotificationItem item = notifications.get(position);

        holder.tvAnimalName.setText(item.getAnimalName());
        holder.tvFeedingTime.setText("⏰ " + item.getFeedingTime());
        holder.tvDay.setText(item.getDay());

        if (item.getFoodType() != null && !item.getFoodType().isEmpty()) {
            holder.tvFoodType.setText("🍽️ " + item.getFoodType());
            holder.tvFoodType.setVisibility(View.VISIBLE);
        } else {
            holder.tvFoodType.setVisibility(View.GONE);
        }

        // Icône et statut selon l'état
        if (item.isScheduled()) {
            holder.ivStatus.setImageResource(R.drawable.ic_notification_active);
            holder.tvStatus.setText("✅ Programmé");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.green_active));

            // Bouton annuler visible
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnReschedule.setVisibility(View.GONE);
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_notification_inactive);
            holder.tvStatus.setText("⏸️ En attente");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.orange_pending));

            // Bouton reprogrammer visible
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnReschedule.setVisibility(View.VISIBLE);
        }

        // Boutons d'action
        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelNotification(item.getScheduleId());
            }
        });

        holder.btnReschedule.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRescheduleNotification(item.getScheduleId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAnimalName, tvFeedingTime, tvFoodType, tvDay, tvStatus;
        ImageView ivStatus;
        View btnCancel, btnReschedule;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvFeedingTime = itemView.findViewById(R.id.tvFeedingTime);
            tvFoodType = itemView.findViewById(R.id.tvFoodType);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnReschedule = itemView.findViewById(R.id.btnReschedule);
        }
    }
}