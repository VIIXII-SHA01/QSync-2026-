package android.bignerdranch.com.qsync;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class SessionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_session);

        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Setup Logo with theme-aware colors
        TextView tvLogo = findViewById(R.id.tv_logo);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        
        SpannableString qs = new SpannableString("QS");
        qs.setSpan(new ForegroundColorSpan(getColor(R.color.brand_purple)), 0, qs.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        SpannableString ync = new SpannableString("ync");
        ync.setSpan(new ForegroundColorSpan(getColor(R.color.brand_teal)), 0, ync.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        builder.append(qs).append(ync);
        tvLogo.setText(builder);

        // Buttons for "New Session"
        TextView tvTopNewSession = findViewById(R.id.tv_top_new_session);
        MaterialButton btnCenterNewSession = findViewById(R.id.btn_center_new_session);

        tvTopNewSession.setOnClickListener(v -> {
            Toast.makeText(this, "Create New Session Clicked", Toast.LENGTH_SHORT).show();
        });

        btnCenterNewSession.setOnClickListener(v -> {
            Toast.makeText(this, "Create New Session Clicked", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setItemActiveIndicatorEnabled(true);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_sessions) {
                return true;
            } else if (id == R.id.nav_kiosk) {
                Toast.makeText(this, "Kiosk Clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
        
        bottomNav.setSelectedItemId(R.id.nav_sessions);
    }
}
