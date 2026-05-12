package android.bignerdranch.com.qsync;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FindKioskActivity extends AppCompatActivity {

    private RecyclerView rvFindKiosks;
    private KioskAdapter adapter;
    private FirebaseFirestore db;
    private EditText etSearch;
    private ListenerRegistration kioskListener;
    private List<Kiosk> allKiosks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_kiosk);

        db = FirebaseFirestore.getInstance();

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

        // Setup RecyclerView
        rvFindKiosks = findViewById(R.id.rv_find_kiosks);
        adapter = new KioskAdapter(new ArrayList<>());
        adapter.setShowDeleteButton(false);
        rvFindKiosks.setLayoutManager(new LinearLayoutManager(this));
        rvFindKiosks.setAdapter(adapter);

        // Item Click Listener
        adapter.setOnItemClickListener(kiosk -> {
            Intent intent = new Intent(FindKioskActivity.this, JoinLineActivity.class);
            intent.putExtra("kiosk_id", kiosk.getId());
            startActivity(intent);
        });

        // Search Functionality - Direct filtering for immediate feedback
        etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterKiosks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load all kiosks with real-time updates
        startKioskListener();
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

    private void startKioskListener() {
        if (kioskListener != null) kioskListener.remove();
        
        // Listen to all kiosks to allow local real-time filtering
        kioskListener = db.collection("kiosks")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    if (value != null) {
                        List<Kiosk> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Kiosk kiosk = doc.toObject(Kiosk.class);
                                kiosk.setId(doc.getId());
                                newList.add(kiosk);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        allKiosks = newList;
                        // Refresh the list with the current search query
                        filterKiosks(etSearch.getText().toString());
                    }
                });
    }

    private void filterKiosks(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        List<Kiosk> filteredResults = new ArrayList<>();

        if (lowerCaseQuery.isEmpty()) {
            filteredResults.addAll(allKiosks);
        } else {
            for (Kiosk kiosk : allKiosks) {
                // Check if the name contains the search query (case-insensitive)
                if (kiosk.getName() != null && kiosk.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredResults.add(kiosk);
                }
            }
        }
        
        // Update the adapter with the filtered list
        adapter.setKioskList(filteredResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kioskListener != null) kioskListener.remove();
    }
}
