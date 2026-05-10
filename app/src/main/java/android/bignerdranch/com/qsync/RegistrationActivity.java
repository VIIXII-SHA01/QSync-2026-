package android.bignerdranch.com.qsync;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Back button action
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Gender dropdown
        AutoCompleteTextView genderSpinner = findViewById(R.id.actv_gender);
        String[] genders = new String[]{"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        genderSpinner.setAdapter(adapter);

        // Terms and conditions link
        TextView tvTerms = findViewById(R.id.tv_terms);
        String termsText = getString(R.string.agree_text) + 
                "<font color='#7F7CF7'><u>" + getString(R.string.terms_service) + "</u></font>" + 
                getString(R.string.and_text) + 
                "<font color='#7F7CF7'><u>" + getString(R.string.privacy_policy) + "</u></font>";
        tvTerms.setText(Html.fromHtml(termsText, Html.FROM_HTML_MODE_COMPACT));
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }
}