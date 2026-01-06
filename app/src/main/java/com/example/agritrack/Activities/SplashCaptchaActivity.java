package com.example.agritrack.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agritrack.R;
import java.util.Locale;
import java.util.Random;

public class SplashCaptchaActivity extends AppCompatActivity {

    private ImageView imgCaptcha;
    private TextView tvTimer;
    private EditText etAnswer;
    private Button btnVerify, btnRefresh, btnAudio;
    private String currentCaptcha = "";
    private CountDownTimer timer;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_captcha);

        initViews();
        generateNewCaptcha();
        startTimer();
    }

    private void initViews() {
        imgCaptcha = findViewById(R.id.imgCaptcha);
        tvTimer = findViewById(R.id.tvTimer);
        etAnswer = findViewById(R.id.etAnswer);
        btnVerify = findViewById(R.id.btnVerify);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnAudio = findViewById(R.id.btnAudio);

        btnVerify.setOnClickListener(v -> verifyAnswer());
        btnRefresh.setOnClickListener(v -> generateNewCaptcha());
        btnAudio.setOnClickListener(v -> speakCaptcha());
    }

    private void generateNewCaptcha() {
        currentCaptcha = randomText(8);
        Bitmap bmp = createCaptchaBitmap(currentCaptcha, 700, 160);
        imgCaptcha.setImageBitmap(bmp);
        etAnswer.setText("");
        etAnswer.requestFocus();
    }

    private String randomText(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // avoid confusing 0/O, 1/I
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private Bitmap createCaptchaBitmap(String text, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#DCEFF1"));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        // Draw noise: lines
        for (int i = 0; i < 6; i++) {
            paint.setColor(randomColor(100, 200));
            paint.setStrokeWidth(2 + random.nextInt(3));
            int startY = random.nextInt(height);
            int endY = random.nextInt(height);
            canvas.drawLine(0, startY, width, endY, paint);
        }

        // Draw text characters with random rotation and vertical jitter
        int chars = text.length();
        int baseX = 40;
        int gap = (width - 80) / chars;
        for (int i = 0; i < chars; i++) {
            char c = text.charAt(i);
            int fontSize = 48 + random.nextInt(36);
            paint.setTextSize(fontSize);
            paint.setTypeface(randomTypeface());
            paint.setColor(randomColor(20, 80));

            float x = baseX + i * gap + random.nextInt(Math.max(1, gap / 4));
            float y = height / 2f + (random.nextInt(40) - 10);

            canvas.save();
            float angle = random.nextInt(40) - 20;
            canvas.rotate(angle, x, y);
            canvas.drawText(String.valueOf(c), x, y, paint);
            canvas.restore();
        }

        // Add some arcs/paths
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        for (int i = 0; i < 4; i++) {
            paint.setColor(randomColor(80, 160));
            Path p = new Path();
            p.moveTo(random.nextInt(width), random.nextInt(height));
            p.quadTo(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
            canvas.drawPath(p, paint);
        }

        return bitmap;
    }

    private int randomColor(int min, int max) {
        int r = min + random.nextInt(Math.max(1, max - min));
        int g = min + random.nextInt(Math.max(1, max - min));
        int b = min + random.nextInt(Math.max(1, max - min));
        return Color.rgb(r, g, b);
    }

    private Typeface randomTypeface() {
        // Use default styles; could load from assets for more variety
        int pick = random.nextInt(3);
        switch (pick) {
            case 0: return Typeface.SERIF;
            case 1: return Typeface.SANS_SERIF;
            default: return Typeface.MONOSPACE;
        }
    }

    private void verifyAnswer() {
        String answer = etAnswer.getText().toString().trim().toUpperCase(Locale.ROOT);
        if (answer.isEmpty()) return;
        if (answer.equals(currentCaptcha)) {
            Toast.makeText(this, "✅ Vérification réussie !", Toast.LENGTH_SHORT).show();
            if (timer != null) timer.cancel();
            goToFinances(); // Réussite → FinanceActivity
        } else {
            Toast.makeText(this, "❌ Mauvais code. Réessaye.", Toast.LENGTH_SHORT).show();
            generateNewCaptcha();
        }
    }

    private void speakCaptcha() {
        // Simple fallback: show toast with characters. For production use TTS.
        Toast.makeText(this, "Captcha: " + currentCaptcha, Toast.LENGTH_SHORT).show();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(30000, 1000) {  // 30s
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("⏱️ " + (millisUntilFinished / 1000) + "s");
            }
            @Override
            public void onFinish() {
                Toast.makeText(SplashCaptchaActivity.this, "⚠️ Timeout - Accès autorisé", Toast.LENGTH_SHORT).show();
                goToAccueil(); // Timeout → AccueilActivity
            }
        }.start();
    }

    private void goToFinances() {
        startActivity(new Intent(this, FinanceActivity.class));
        finish();
    }

    private void goToAccueil() {
        startActivity(new Intent(this, AccueilActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}