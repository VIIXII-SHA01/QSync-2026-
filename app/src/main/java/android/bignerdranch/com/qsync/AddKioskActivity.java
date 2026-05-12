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

import java.util.Calendar;
import java.util.Locale;

public class AddKioskActivity extends AppCompatActivity {

    private TextInputEditText etKioskName, etDate, etDailyLimit, etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_kiosk);

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
                Toast.makeText(this, "Kiosk Created Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
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
