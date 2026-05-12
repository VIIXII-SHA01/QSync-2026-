package android.bignerdranch.com.qsync;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class EntertainClientActivity extends AppCompatActivity {

    private TextView tvInitialCircle, tvClientName, tvQueueNumber;
    private CheckBox cbTransactionClosed;
    private MaterialButton btnCall, btnMessage, btnUpdateStatus;
    private FirebaseFirestore db;
    private String sessionId, userId, userName, phone;
    private long queueNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entertain_client);

        db = FirebaseFirestore.getInstance();

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Get data from intent
        sessionId = getIntent().getStringExtra("session_id");
        userId = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");
        queueNumber = getIntent().getLongExtra("queue_number", 0);

        // Initialize Views
        ImageView ivBack = findViewById(R.id.iv_back);
        tvInitialCircle = findViewById(R.id.tv_initial_circle);
        tvClientName = findViewById(R.id.tv_client_name);
        tvQueueNumber = findViewById(R.id.tv_queue_number);
        cbTransactionClosed = findViewById(R.id.cb_transaction_closed);
        btnCall = findViewById(R.id.btn_call);
        btnMessage = findViewById(R.id.btn_message);
        btnUpdateStatus = findViewById(R.id.btn_update_status);

        if (ivBack != null) ivBack.setOnClickListener(v -> finish());

        // Set initial data
        if (tvClientName != null) tvClientName.setText(userName != null ? userName : "Unknown Client");
        if (tvQueueNumber != null) tvQueueNumber.setText(getString(R.string.queue_number_label, queueNumber));
        if (tvInitialCircle != null && userName != null && !userName.isEmpty()) {
            tvInitialCircle.setText(String.valueOf(userName.charAt(0)).toUpperCase());
        }

        fetchUserDetails();

        if (btnCall != null) btnCall.setOnClickListener(v -> makeCall());
        if (btnMessage != null) btnMessage.setOnClickListener(v -> sendMessage());
        if (btnUpdateStatus != null) btnUpdateStatus.setOnClickListener(v -> updateStatus());
    }

    private void fetchUserDetails() {
        if (userId == null) return;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        phone = documentSnapshot.getString("phone");
                    }
                });
    }

    private void makeCall() {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void sendMessage() {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phone));
        intent.putExtra("sms_body", "Hello " + userName + ", it's your turn at the kiosk. Please proceed to the counter.");
        startActivity(intent);
    }

    private void updateStatus() {
        if (sessionId == null) return;

        if (cbTransactionClosed != null && cbTransactionClosed.isChecked()) {
            db.collection("sessions").document(sessionId)
                    .update("status", "Closed")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Transaction closed", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please mark transaction as closed to update", Toast.LENGTH_SHORT).show();
        }
    }
}
