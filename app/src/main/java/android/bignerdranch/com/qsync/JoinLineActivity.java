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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinLineActivity extends AppCompatActivity {

    private TextView tvKioskName, tvDate, tvAvailableNumber, tvDailyLimit, tvCurrentTransaction, tvStatus, tvDescription, tvInitialCircle;
    private MaterialButton btnFallInLine;
    private FirebaseFirestore db;
    private String kioskId;
    private String currentUserId;
    private ListenerRegistration kioskListener;
    private ListenerRegistration sessionListener;
    private boolean isAlreadyInLine = false;
    private boolean isHost = false;
    private Kiosk currentKiosk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join_line);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Logo
        TextView tvLogo = findViewById(R.id.tv_logo);
        setupLogo(tvLogo);

        // Back Button
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // Initialize Views
        tvInitialCircle = findViewById(R.id.tv_initial_circle);
        tvKioskName = findViewById(R.id.tv_kiosk_name);
        tvDate = findViewById(R.id.tv_date);
        tvAvailableNumber = findViewById(R.id.tv_available_number);
        tvDailyLimit = findViewById(R.id.tv_daily_limit);
        tvCurrentTransaction = findViewById(R.id.tv_current_transaction);
        tvStatus = findViewById(R.id.tv_status);
        tvDescription = findViewById(R.id.tv_description);
        btnFallInLine = findViewById(R.id.btn_fall_in_line);

        kioskId = getIntent().getStringExtra("kiosk_id");
        
        if (kioskId != null) {
            loadKioskDetails();
            checkUserSession();
        }

        btnFallInLine.setOnClickListener(v -> joinQueue());
    }

    private void setupLogo(TextView tvLogo) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString qs = new SpannableString("QS");
        qs.setSpan(new ForegroundColorSpan(getColor(R.color.brand_purple)), 0, qs.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString ync = new SpannableString("ync");
        ync.setSpan(new ForegroundColorSpan(getColor(R.color.brand_teal)), 0, ync.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(qs).append(ync);
        tvLogo.setText(builder);
    }

    private void checkUserSession() {
        if (currentUserId == null || kioskId == null) return;

        if (sessionListener != null) sessionListener.remove();
        sessionListener = db.collection("sessions")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("kioskId", kioskId)
                .whereEqualTo("status", "Waiting")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    isAlreadyInLine = value != null && !value.isEmpty();
                    if (currentKiosk != null) {
                        updateUI(currentKiosk);
                    }
                });
    }

    private void loadKioskDetails() {
        if (kioskListener != null) kioskListener.remove();
        kioskListener = db.collection("kiosks").document(kioskId).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Toast.makeText(this, "Error loading kiosk details", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                currentKiosk = documentSnapshot.toObject(Kiosk.class);
                if (currentKiosk != null) {
                    currentKiosk.setId(documentSnapshot.getId());
                    updateUI(currentKiosk);
                }
            }
        });
    }

    private void updateUI(Kiosk kiosk) {
        tvKioskName.setText(kiosk.getName());
        tvInitialCircle.setText(String.valueOf(kiosk.getName().charAt(0)).toUpperCase());
        tvDate.setText(getString(R.string.label_date, kiosk.getDate()));
        tvAvailableNumber.setText(getString(R.string.label_available_number, kiosk.getAvailableNumber()));
        tvDailyLimit.setText(getString(R.string.label_daily_limit, kiosk.getDailyLimit()));
        tvCurrentTransaction.setText(getString(R.string.label_current_transaction, kiosk.getCurrentTransaction()));
        
        String status = kiosk.getStatus();
        tvStatus.setText(status.toUpperCase());
        
        isHost = currentUserId != null && (currentUserId.equals(kiosk.getCreatorId()) || (kiosk.getHostIds() != null && kiosk.getHostIds().contains(currentUserId)));
        
        boolean isOpen = "Open".equalsIgnoreCase(status);
        boolean canJoin = isOpen && !isAlreadyInLine && !isHost;

        if (isOpen) {
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.brand_teal));
        } else {
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
        }

        btnFallInLine.setEnabled(canJoin);
        btnFallInLine.setAlpha(canJoin ? 1.0f : 0.5f);
        if (isHost) {
            btnFallInLine.setText("Host Cannot Join");
        } else if (isAlreadyInLine) {
            btnFallInLine.setText("Already in Line");
        } else {
            btnFallInLine.setText(R.string.fall_in_line);
        }

        tvDescription.setText(getString(R.string.label_description, kiosk.getDescription()));
    }

    private void joinQueue() {
        if (currentUserId == null) {
            Toast.makeText(this, "Please sign in to join the line", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isHost) {
            Toast.makeText(this, "Hosts cannot join their own line", Toast.LENGTH_SHORT).show();
            return;
        }

        btnFallInLine.setEnabled(false);

        // First check if user is already in line (double check)
        db.collection("sessions")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("kioskId", kioskId)
                .whereEqualTo("status", "Waiting")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        isAlreadyInLine = true;
                        if (currentKiosk != null) updateUI(currentKiosk);
                        Toast.makeText(this, "You are already in line for this kiosk", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // First fetch user details to record their name
                    db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
                        if (!userDoc.exists()) {
                            if (currentKiosk != null) updateUI(currentKiosk);
                            Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String userName = userDoc.getString("firstName") + " " + userDoc.getString("lastName");

                        db.runTransaction(transaction -> {
                            DocumentSnapshot kioskSnap = transaction.get(db.collection("kiosks").document(kioskId));
                            
                            // Server-side validation for host
                            String creatorId = kioskSnap.getString("creatorId");
                            List<String> hostIds = (List<String>) kioskSnap.get("hostIds");
                            if (currentUserId.equals(creatorId) || (hostIds != null && hostIds.contains(currentUserId))) {
                                throw new FirebaseFirestoreException("Hosts cannot join their own line", FirebaseFirestoreException.Code.ABORTED);
                            }

                            Long availableNumber = kioskSnap.getLong("availableNumber");
                            Long dailyLimit = kioskSnap.getLong("dailyLimit");

                            if (availableNumber == null || dailyLimit == null) {
                                throw new FirebaseFirestoreException("Kiosk data error", FirebaseFirestoreException.Code.ABORTED);
                            }

                            if (availableNumber > dailyLimit) {
                                throw new FirebaseFirestoreException("Daily limit reached for this kiosk", FirebaseFirestoreException.Code.ABORTED);
                            }

                            // Record as a client to that kiosk in a 'sessions' collection
                            Map<String, Object> session = new HashMap<>();
                            session.put("userId", currentUserId);
                            session.put("userName", userName);
                            session.put("kioskId", kioskId);
                            session.put("kioskName", kioskSnap.getString("name"));
                            session.put("number", availableNumber);
                            session.put("status", "Waiting");
                            session.put("timestamp", FieldValue.serverTimestamp());

                            // Add to sessions collection
                            transaction.set(db.collection("sessions").document(), session);
                            
                            // Increment the kiosk count for the next person
                            transaction.update(db.collection("kiosks").document(kioskId), "availableNumber", availableNumber + 1);

                            return availableNumber;
                        }).addOnSuccessListener(number -> {
                            Toast.makeText(this, "Successfully joined! Your number is " + number, Toast.LENGTH_LONG).show();
                            finish();
                        }).addOnFailureListener(e -> {
                            if (currentKiosk != null) updateUI(currentKiosk);
                            Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }).addOnFailureListener(e -> {
                        if (currentKiosk != null) updateUI(currentKiosk);
                        Toast.makeText(this, "Error fetching user info", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kioskListener != null) kioskListener.remove();
        if (sessionListener != null) sessionListener.remove();
    }
}
