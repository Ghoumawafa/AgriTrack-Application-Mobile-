package com.example.agritrack.Activities;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.GeminiChatAdapter;
import com.example.agritrack.R;
import com.example.agritrack.Utils.GeminiService;
import java.util.ArrayList;
import java.util.List;

public class GeminiChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private GeminiChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private EditText etMessage;
    private ImageButton btnSend;
    private GeminiService geminiService;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini_chat);

        initToolbar();
        initViews();
        initGemini();
        setupRecyclerView();
        setupListeners();
        showWelcomeMessage();
    }

    private void initToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("🤖 IA AgriTrack");
        }
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void initGemini() {
        geminiService = new GeminiService();
    }

    private void setupRecyclerView() {
        adapter = new GeminiChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) return;

        // Ajoute message utilisateur
        messages.add(new ChatMessage(message, true)); // true = user
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
        etMessage.setText("");

        // Demande à Gemini
        btnSend.setImageResource(R.drawable.bg_ai_message); // Icon loading
        tvEmptyState.setVisibility(android.view.View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ✅ CORRECT (2 paramètres seulement)
            geminiService.askGemini(message, new GeminiService.GeminiCallback() {
                @Override
                public void onResponse(String response) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage(response, false));
                        adapter.notifyItemInserted(messages.size() - 1);
                        rvChat.scrollToPosition(messages.size() - 1);
                        btnSend.setImageResource(android.R.drawable.ic_menu_send);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage(error, false));
                        adapter.notifyItemInserted(messages.size() - 1);
                        rvChat.scrollToPosition(messages.size() - 1);
                        btnSend.setImageResource(android.R.drawable.ic_menu_send);
                        Toast.makeText(GeminiChatActivity.this, error, Toast.LENGTH_SHORT).show();
                    });
                }
            });


        }
    }

    private void showWelcomeMessage() {
        messages.add(new ChatMessage(
                "🤖 Bonjour ! Je suis l'IA AgriTrack. Demande-moi :\n" +
                        "• Conseils finances agriculture\n" +
                        "• Prix moutons FR→DT\n" +
                        "• Engrais importés\n" +
                        "• Optimisation récoltes olives\n" +
                        "• Plan irrigation\n\n" +
                        "Ex: \"Revenus 1500DT, que faire ?\"", false));
        adapter.notifyDataSetChanged();
        tvEmptyState.setVisibility(android.view.View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Classes internes
    public static class ChatMessage {
        public String text;
        public boolean isUser;

        ChatMessage(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
        }
    }
}

