package com.example.agritrack.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import com.example.agritrack.Activities.AccueilActivity;  // ✅ DASHBOARD

public class GraphsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private AgriTrackRoomDatabase db;

    // ✅ FIXED EMAIL - MÊME QUE FinanceActivity
    private final String USER_EMAIL = "sana@agritrack.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

        MaterialToolbar graphsToolbar = findViewById(R.id.graphsToolbar);
        setSupportActionBar(graphsToolbar);

        // ✅ BOUTON RETOUR VERS DASHBOARD (remplace finish())
        graphsToolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(GraphsActivity.this, FinanceActivity.class));
            finish();
        });

        db = AgriTrackRoomDatabase.getInstance(this);
        updateCharts();
    }

    private void updateCharts() {
        updatePieChart();
        updateBarChart();
    }

    // ✅ CORRECTION 2 : FIXED SANS SessionManager
    private void updatePieChart() {
        // ✅ FIXED EMAIL - MÊME QUE FinanceActivity
        String userEmail = USER_EMAIL;

        // ✅ NOUVELLES MÉTHODES avec userEmail
        Double totalRevenus = db.transactionDao().getTotalRevenusForUser(userEmail);
        Double totalDepenses = db.transactionDao().getTotalDepensesForUser(userEmail);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(totalRevenus != null ? totalRevenus.floatValue() : 0f, "Revenus"));
        entries.add(new PieEntry(totalDepenses != null ? totalDepenses.floatValue() : 0f, "Dépenses"));

        PieDataSet dataSet = new PieDataSet(entries, "Résumé Mensuel");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(12f);

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setCenterText("Revenus\nvs\nDépenses");
        pieChart.setCenterTextSize(16f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void updateBarChart() {
        // ✅ Données mensuelles fictives (à remplacer par vraies données DB)
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 120f));
        entries.add(new BarEntry(1, 200f));
        entries.add(new BarEntry(2, 150f));
        entries.add(new BarEntry(3, 180f));
        entries.add(new BarEntry(4, 220f));
        entries.add(new BarEntry(5, 190f));

        BarDataSet dataSet = new BarDataSet(entries, "Dépenses Mensuelles");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"}));

        barChart.animateY(1000);
        barChart.invalidate();
    }
}
