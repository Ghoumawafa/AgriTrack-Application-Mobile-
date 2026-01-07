package com.example.agritrack.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agritrack.Adapters.TransactionAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Models.Transaction;
import com.example.agritrack.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.agritrack.Activities.GeminiChatActivity;
import com.example.agritrack.Utils.GeminiService;
import android.view.Menu;




public class FinanceActivity extends AppCompatActivity {

    private AgriTrackRoomDatabase db;
    private TransactionAdapter adapter;
    private List<Transaction> transactions = new ArrayList<>();
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isLoading = false;
    private int editTransactionId = -1;

    private final String USER_EMAIL = "sana@agritrack.com";

    private MaterialButton btnRevenu, btnDepense;
    private String typeTransaction = "Revenu";

    private EditText etDescription, etAmount;
    private Button btnDatePicker, btnSave;
    private TextView tvTotalRevenus, tvTotalDepenses;
    private RecyclerView recyclerView;
    private MaterialToolbar topAppBar;
    private MaterialButton btnExportPdf;
    //private TextView tvEuroRate;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

        initViews();
        initToolbar();
        initDatabase();  // ✅ DB AVANT API
        setupDatePicker();
        setupTypeButtons();
        setupRecyclerView();
        setupRealTimeValidation();
        setupListeners();
        loadData();

//        // ✅ SAFE : Vérifie tvEuroRate + Handler
//        mainHandler.postDelayed(() -> {
//            if (tvEuroRate != null) {
//                fetchEuroRate();
//            }
//        }, 500);  // 0.5s délai safe
    }


    private void initViews() {
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnRevenu = findViewById(R.id.btnRevenu);
        btnDepense = findViewById(R.id.btnDepense);
        btnSave = findViewById(R.id.btnSave);
        tvTotalRevenus = findViewById(R.id.tvTotalRevenus);
        tvTotalDepenses = findViewById(R.id.tvTotalDepenses);
        recyclerView = findViewById(R.id.recyclerViewTransactions);
        topAppBar = findViewById(R.id.topAppBar);
        btnExportPdf = findViewById(R.id.btnExportPdf);
//        tvEuroRate = findViewById(R.id.tvEuroRate);
        // ✅ GEMINI IA (après loadData)



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        updateDateButton(sdf.format(new Date()));
    }

    private void initToolbar() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("💰 Finances");  // ✅ AJOUTE ÇA ICI

        }
        topAppBar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }



        if (id == R.id.action_graphs) {
            showGraphs();
            return true;
        }
        if (id == R.id.action_gemini) {
            startActivity(new Intent(this, GeminiChatActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }



    private void showGraphs() {
        executor.execute(() -> {
            try {
                String userEmail = USER_EMAIL;
                Double totalRevenus = db.transactionDao().getTotalRevenusForUser(userEmail);
                Double totalDepenses = db.transactionDao().getTotalDepensesForUser(userEmail);

                // Données mensuelles pour BarChart
                List<Transaction> allTransactions = db.transactionDao().getTransactionsForUser(userEmail);

                mainHandler.post(() -> {
                    Intent intent = new Intent(FinanceActivity.this, GraphsActivity.class);
                    intent.putExtra("TOTAL_REVENUS", totalRevenus != null ? totalRevenus : 0);
                    intent.putExtra("TOTAL_DEPENSES", totalDepenses != null ? totalDepenses : 0);
                    startActivity(intent);
                });
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "❌ Erreur graphs", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String getPercentage(Double value1, double totalRevenus, double totalDepenses) {
        double total = totalRevenus + totalDepenses;
        if (total == 0) return "0%";
        double percent = (value1 != null ? value1 : 0) / total * 100;
        return String.format("%.1f%%", percent);
    }

    private void initDatabase() {
        db = AgriTrackRoomDatabase.getInstance(this);
    }

    private void setupTypeButtons() {
        btnRevenu.setOnClickListener(v -> {
            typeTransaction = "Revenu";
            btnRevenu.setStrokeColor(getColorStateList(android.R.color.holo_green_dark));
            btnDepense.setStrokeColor(getColorStateList(android.R.color.darker_gray));
            Toast.makeText(this, "💰 Mode Revenus", Toast.LENGTH_SHORT).show();
        });

        btnDepense.setOnClickListener(v -> {
            typeTransaction = "Dépense";
            btnDepense.setStrokeColor(getColorStateList(android.R.color.holo_red_dark));
            btnRevenu.setStrokeColor(getColorStateList(android.R.color.darker_gray));
            Toast.makeText(this, "❤️ Mode Dépenses", Toast.LENGTH_SHORT).show();
        });

        btnRevenu.performClick();
    }

    private void setupDatePicker() {
        btnDatePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            String currentDateStr = btnDatePicker.getText().toString().equals("Sélectionner date") ?
                    sdf.format(new Date()) : btnDatePicker.getText().toString();
            Date currentDate = sdf.parse(currentDateStr);
            cal.setTime(currentDate);
        } catch (Exception e) {
            cal.setTime(new Date());
        }

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    if (isValidDate(selectedDate)) {
                        updateDateButton(selectedDate);
                    } else {
                        Toast.makeText(this, "Date ne peut pas être dans le futur", Toast.LENGTH_SHORT).show();
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void updateDateButton(String date) {
        btnDatePicker.setText(date);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(transactions, new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onDeleteClick(Transaction transaction) {
                deleteTransaction(transaction);
            }
            @Override
            public void onEditClick(Transaction transaction) {
                editTransaction(transaction);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupRealTimeValidation() {
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String amountStr = s.toString();
                if (!amountStr.isEmpty() && !isValidAmount(amountStr)) {
                    etAmount.setError("Montant doit être positif");
                } else {
                    etAmount.setError(null);
                }
            }
        });
    }

    private boolean isValidDate(String dateStr) {
        if (dateStr.length() != 10) return false;
        if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateStr);
            return date != null && !date.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return amount >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveTransaction());
        btnExportPdf.setOnClickListener(v -> exportToPdf());
    }

    private void saveTransaction() {
        String desc = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String date = btnDatePicker.getText().toString().trim();

        if (desc.isEmpty()) {
            etDescription.setError("Description obligatoire");
            return;
        }
        if (!isValidAmount(amountStr)) {
            etAmount.setError("Montant doit être positif");
            return;
        }
        if (date.equals("Sélectionner date") || !isValidDate(date)) {
            Toast.makeText(this, "Sélectionnez une date valide", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                String userEmail = USER_EMAIL;

                if (editTransactionId != -1) {
                    Transaction existing = db.transactionDao().getTransactionById(editTransactionId);
                    if (existing != null) {
                        existing.setDescription(desc);
                        existing.setAmount(Double.parseDouble(amountStr));
                        existing.setDate(date);
                        existing.setCategory(typeTransaction);
                        existing.setType(typeTransaction);
                        existing.setUserEmail(userEmail);
                        db.transactionDao().update(existing);

                        mainHandler.post(() -> {
                            Toast.makeText(this, "✅ Transaction modifiée", Toast.LENGTH_SHORT).show();
                            clearForm();
                            loadData();
                        });
                    }
                    editTransactionId = -1;
                } else {
                    double amount = Double.parseDouble(amountStr);
                    Transaction transaction = new Transaction(
                            userEmail, desc, amount, date, typeTransaction, typeTransaction
                    );
                    db.transactionDao().insert(transaction);

                    mainHandler.post(() -> {
                        Toast.makeText(this, "✅ Transaction ajoutée", Toast.LENGTH_SHORT).show();
                        clearForm();
                        loadData();
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void editTransaction(Transaction transaction) {
        editTransactionId = transaction.getId();
        etDescription.setText(transaction.getDescription());
        etAmount.setText(String.valueOf(transaction.getAmount()));
        updateDateButton(transaction.getDate());
        typeTransaction = transaction.getType();
        setupTypeButtons();
        btnSave.setText("Modifier");
        etDescription.requestFocus();
        etDescription.setSelection(etDescription.getText().length());
    }

    private void loadData() {
        if (isLoading) return;
        isLoading = true;

        executor.execute(() -> {
            try {
                String userEmail = USER_EMAIL;
                List<Transaction> myTransactions = db.transactionDao().getTransactionsForUser(userEmail);
                Double totalRevenus = db.transactionDao().getTotalRevenusForUser(userEmail);
                Double totalDepenses = db.transactionDao().getTotalDepensesForUser(userEmail);

                mainHandler.post(() -> {
                    transactions.clear();
                    transactions.addAll(myTransactions);
                    adapter.notifyDataSetChanged();

                    tvTotalRevenus.setText("💰 Revenus: " +
                            (totalRevenus != null ? String.format("%.2f", totalRevenus) : "0.00") + " Dinar");
                    tvTotalDepenses.setText("❤️ Dépenses: " +
                            (totalDepenses != null ? String.format("%.2f", totalDepenses) : "0.00") + " Dinar");

                    isLoading = false;
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "❌ LOAD ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    isLoading = false;
                });
            }
        });
    }

    private void deleteTransaction(Transaction transaction) {
        executor.execute(() -> {
            try {
                db.transactionDao().delete(transaction);
                mainHandler.post(this::loadData);
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "❌ Suppression échouée", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void clearForm() {
        etDescription.setText("");
        etAmount.setText("");
        etDescription.setError(null);
        etAmount.setError(null);
        btnSave.setText("➕ AJOUTER TRANSACTION");
        editTransactionId = -1;
        typeTransaction = "Revenu";
        setupTypeButtons();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        updateDateButton(sdf.format(new Date()));
        etDescription.requestFocus();
    }

    private void exportToPdf() {
        executor.execute(() -> {
            try {
                String userEmail = USER_EMAIL;
                List<Transaction> transactions = db.transactionDao().getTransactionsForUser(userEmail);
                Double totalRevenus = db.transactionDao().getTotalRevenusForUser(userEmail);
                Double totalDepenses = db.transactionDao().getTotalDepensesForUser(userEmail);

                mainHandler.post(() -> createPdfReport(transactions, totalRevenus, totalDepenses));
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "❌ Erreur PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void createPdfReport(List<Transaction> transactions, Double totalRevenus, Double totalDepenses) {
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            paint.setTextSize(36);
            paint.setFakeBoldText(true);
            paint.setColor(Color.BLACK);
            canvas.drawText("📊 RAPPORT FINANCES AGRITRACK", 50, 80, paint);

            paint.setTextSize(24);
            canvas.drawText("💰 Revenus: " + (totalRevenus != null ? String.format("%.2f", totalRevenus) : "0") + " Dinar", 50, 150, paint);
            canvas.drawText("❤️ Dépenses: " + (totalDepenses != null ? String.format("%.2f", totalDepenses) : "0") + " Dinar", 50, 190, paint);

            paint.setTextSize(18);
            canvas.drawText("📋 Historique Transactions:", 50, 250, paint);

            int yPos = 280;
            for (Transaction t : transactions) {
                String line = String.format("• %s - %.2f Dinar (%s) - %s",
                        t.getDescription(), t.getAmount(), t.getType(), t.getDate());
                canvas.drawText(line, 70, yPos, paint);
                yPos += 30;
                if (yPos > 750) break;
            }

            document.finishPage(page);

            String fileName = "AgriTrack_Report_" + System.currentTimeMillis() + ".pdf";
            File file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName);

            FileOutputStream out = new FileOutputStream(file);
            document.writeTo(out);
            document.close();
            out.close();

            Toast.makeText(this, "✅ PDF sauvé ! Downloads/" + fileName, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "❌ Erreur création PDF", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }


}
