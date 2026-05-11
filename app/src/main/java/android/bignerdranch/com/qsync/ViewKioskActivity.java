package android.bignerdranch.com.qsync;

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

public class ViewKioskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_kiosk);

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

        // Back Button
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // Fall in Line Button
        MaterialButton btnFallInLine = findViewById(R.id.btn_fall_in_line);
        btnFallInLine.setOnClickListener(v -> {
            Toast.makeText(this, "Joined the queue!", Toast.LENGTH_SHORT).show();
        });
    }
}
