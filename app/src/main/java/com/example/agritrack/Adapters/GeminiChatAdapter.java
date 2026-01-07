package com.example.agritrack.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agritrack.Activities.GeminiChatActivity;
import com.example.agritrack.R;
import java.util.List;

public class GeminiChatAdapter extends RecyclerView.Adapter<GeminiChatAdapter.ChatViewHolder> {

    private List<GeminiChatActivity.ChatMessage> messages;

    public GeminiChatAdapter(List<GeminiChatActivity.ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        GeminiChatActivity.ChatMessage message = messages.get(position);
        holder.tvMessage.setText(message.text);

        // ✅ USER = DROITE (bleu clair)
        if (message.isUser) {
            holder.itemView.setBackgroundResource(R.drawable.bg_user_message);
            holder.tvMessage.setTextColor(0xFF000000); // Noir
            holder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        // ✅ IA = GAUCHE (vert)
        else {
            holder.itemView.setBackgroundResource(R.drawable.bg_ai_message);
            holder.tvMessage.setTextColor(0xFFFFFFFF); // Blanc
            holder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        ChatViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
