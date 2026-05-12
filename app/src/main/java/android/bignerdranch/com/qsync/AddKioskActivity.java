package android.bignerdranch.com.qsync;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddKioskActivity extends AppCompatActivity {

    private TextInputEditText etKioskName, etDate, etDailyLimit, etDescription;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_kiosk);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Logo
        TextView tvLogo = findViewById(R.id.tv_logo);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString qs = new SpannableString("QS");
        qs.setSpan(new ForegroundColorSpan(getColor(R.color.brand_purple)), 0, qs.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString ync = new SpannableString("ync");
        ync.setSpan(new ForegroundColorSpan(getColor(R.color.brand_teal)), 0, ync.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(qs).append(ync);
        tvLogo.setText(builder);

        // UI Components
        etKioskName = findViewById(R.id.et_kiosk_name);
        etDate = findViewById(R.id.et_date);
        etDailyLimit = findViewById(R.id.et_daily_limit);
        etDescription = findViewById(R.id.et_description);
        ImageView ivBack = findViewById(R.id.iv_back);
        MaterialButton btnSave = findViewById(R.id.btn_save_kiosk);

        // Date Picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Back Button
        ivBack.setOnClickListener(v -> finish());

        // Save Button
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveKioskToFirestore();
            }
        });
    }

    private void saveKioskToFirestore() {
        String userId = mAuth.getUid();
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etKioskName.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        int dailyLimit = Integer.parseInt(etDailyLimit.getText().toString().trim());
        String description = etDescription.getText().toString().trim();

        Map<String, Object> kiosk = new HashMap<>();
        kiosk.put("name", name);
        kiosk.put("nameLowercase", name.toLowerCase());
        kiosk.put("date", date);
        kiosk.put("dailyLimit", dailyLimit);
        kiosk.put("description", description);
        kiosk.put("creatorId", userId);
        kiosk.put("status", "OPEN");
        kiosk.put("currentTransaction", 0);
        kiosk.put("availableNumber", 1);
        kiosk.put("createdAt", System.currentTimeMillis());

        db.collection("kiosks")
                .add(kiosk)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddKioskActivity.this, "Kiosk Created Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddKioskActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%d/%d/%d", monthOfYear + 1, dayOfMonth, year1);
                    etDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private boolean validateInputs() {
        if (etKioskName.getText().toString().trim().isEmpty()) {
            etKioskName.setError("Kiosk name is required");
            return false;
        }
        if (etDate.getText().toString().isEmpty()) {
            etDate.setError("Date is required");
            return false;
        }
        if (etDailyLimit.getText().toString().isEmpty()) {
            etDailyLimit.setError("Daily limit is required");
            return false;
        }
        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Description is required");
            return false;
        }
        return true;
    }
}
