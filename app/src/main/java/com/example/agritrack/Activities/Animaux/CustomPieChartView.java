package com.example.agritrack.Activities.Animaux;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CustomPieChartView extends View {

    private List<PieSlice> slices = new ArrayList<>();
    private Paint paint;
    private Paint textPaint;
    private RectF rectF;

    public CustomPieChartView(Context context) {
        super(context);
        init();
    }

    public CustomPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        rectF = new RectF();
    }

    public void setData(List<PieSlice> slices) {
        this.slices = slices;
        invalidate(); // Redessiner
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (slices.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - 100;
        int centerX = width / 2;
        int centerY = height / 2;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Calculer le total
        float total = 0;
        for (PieSlice slice : slices) {
            total += slice.getValue();
        }

        // Dessiner les parts
        float startAngle = 0;
        for (PieSlice slice : slices) {
            float sweepAngle = (slice.getValue() / total) * 360f;

            paint.setColor(slice.getColor());
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            // Dessiner le pourcentage
            float percentage = (slice.getValue() / total) * 100;
            if (percentage > 5) { // N'afficher que si > 5%
                float angle = startAngle + (sweepAngle / 2);
                double radians = Math.toRadians(angle);
                float x = (float) (centerX + (radius / 2) * Math.cos(radians));
                float y = (float) (centerY + (radius / 2) * Math.sin(radians));

                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(36f);
                textPaint.setFakeBoldText(true);
                canvas.drawText(String.format("%.0f%%", percentage), x, y, textPaint);
            }

            startAngle += sweepAngle;
        }

        // Dessiner la légende
        drawLegend(canvas, width, height);
    }

    private void drawLegend(Canvas canvas, int width, int height) {
        int legendX = 50;
        int legendY = height - 200;
        int boxSize = 40;
        int spacing = 60;

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setFakeBoldText(false);

        for (int i = 0; i < slices.size(); i++) {
            PieSlice slice = slices.get(i);

            // Dessiner le carré de couleur
            paint.setColor(slice.getColor());
            canvas.drawRect(legendX, legendY, legendX + boxSize, legendY + boxSize, paint);

            // Dessiner le label
            canvas.drawText(slice.getLabel(), legendX + boxSize + 20, legendY + boxSize - 10, textPaint);

            legendY += spacing;
        }
    }

    // Classe interne pour les données
    public static class PieSlice {
        private String label;
        private float value;
        private int color;

        public PieSlice(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }

        public String getLabel() { return label; }
        public float getValue() { return value; }
        public int getColor() { return color; }
    }
}