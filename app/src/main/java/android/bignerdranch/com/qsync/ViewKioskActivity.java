package android.bignerdranch.com.qsync;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewKioskActivity extends AppCompatActivity {

    private static final String TAG = "ViewKioskActivity";
    private TextView tabHost, tabClients, tabInfo;
    private View indicatorHost, indicatorClients, indicatorInfo;
    private ViewPager2 viewPager;
    private ViewKioskPagerAdapter pagerAdapter;

    // Header Views
    private TextView tvKioskName, tvKioskDate, tvInitialCircle;

    // Bottom Buttons
    private MaterialButton btnAddHost, btnFallInLine, btnEditKiosk;

    private FirebaseFirestore db;
    private String currentUserId;
    private boolean isHost = false;
    private Kiosk currentKiosk;
    private String kioskId;
    private ViewKioskViewModel viewModel;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private ListenerRegistration clientsListener;
    private ListenerRegistration kioskListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_kiosk);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        viewModel = new ViewModelProvider(this).get(ViewKioskViewModel.class);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Setup Logo
        TextView tvLogo = findViewById(R.id.tv_logo);
        setupLogo(tvLogo);

        // Back Button
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // Initialize Tab Navigation Views
        tabHost = findViewById(R.id.tab_host);
        tabClients = findViewById(R.id.tab_clients);
        tabInfo = findViewById(R.id.tab_info);

        indicatorHost = findViewById(R.id.indicator_host);
        indicatorClients = findViewById(R.id.indicator_clients);
        indicatorInfo = findViewById(R.id.indicator_info);

        // Initialize ViewPager2
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new ViewKioskPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Initialize Data Views
        tvKioskName = findViewById(R.id.tv_kiosk_name);
        tvKioskDate = findViewById(R.id.tv_date);
        tvInitialCircle = findViewById(R.id.tv_initial_circle);

        // Initialize Bottom Buttons
        btnAddHost = findViewById(R.id.btn_add_host);
        btnFallInLine = findViewById(R.id.btn_fall_in_line);
        btnEditKiosk = findViewById(R.id.btn_edit_kiosk);
        
        // Initial state
        btnFallInLine.setEnabled(false);

        // Load Kiosk Data
        loadKioskData();

        // Observe clients to update Fall in Line button status
        viewModel.getClients().observe(this, clients -> updateFallInLineButtonStatus());

        // Tab click listeners
        tabHost.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabClients.setOnClickListener(v -> viewPager.setCurrentItem(1));
        tabInfo.setOnClickListener(v -> viewPager.setCurrentItem(2));

        // Sync tabs with ViewPager2 swipe
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateTabUI(position);
            }
        });

        // Button Listeners
        btnAddHost.setOnClickListener(v -> showAddHostDialog());
        btnFallInLine.setOnClickListener(v -> joinQueue());
        btnEditKiosk.setOnClickListener(v -> {
            if (currentKiosk != null) showEditKioskDialog();
        });

        // Default tab
        updateTabUI(0);
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

    private void loadKioskData() {
        kioskId = getIntent().getStringExtra("kiosk_id");
        if (kioskId == null) kioskId = getIntent().getStringExtra("KIOSK_ID");
        
        String intentName = getIntent().getStringExtra("kiosk_name");
        if (intentName == null) intentName = getIntent().getStringExtra("KIOSK_NAME");
        
        String intentDate = getIntent().getStringExtra("kiosk_date");
        if (intentDate == null) intentDate = getIntent().getStringExtra("KIOSK_DATE");

        if (intentName != null) {
            tvKioskName.setText(intentName);
            tvInitialCircle.setText(String.valueOf(intentName.charAt(0)).toUpperCase());
        }
        if (intentDate != null) {
            tvKioskDate.setText(intentDate);
        }

        if (kioskId != null) {
            if (kioskListener != null) kioskListener.remove();
            kioskListener = db.collection("kiosks").document(kioskId).addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    if (e.getCode() != FirebaseFirestoreException.Code.PERMISSION_DENIED && FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Toast.makeText(this, "Error fetching kiosk details", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    currentKiosk = documentSnapshot.toObject(Kiosk.class);
                    if (currentKiosk != null) {
                        currentKiosk.setId(documentSnapshot.getId());
                        updateUIWithKiosk(currentKiosk);
                    }
                } else if (documentSnapshot != null && !documentSnapshot.exists()) {
                    Toast.makeText(this, "This kiosk has been deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            loadClientsData();
        }
    }

    private void loadClientsData() {
        if (clientsListener != null) clientsListener.remove();

        clientsListener = db.collection("sessions")
                .whereEqualTo("kioskId", kioskId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (error.getCode() != FirebaseFirestoreException.Code.PERMISSION_DENIED && FirebaseAuth.getInstance().getCurrentUser() != null) {
                            Toast.makeText(this, "Error fetching clients: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        return;
                    }
                    if (value != null) {
                        List<Session> clientsList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Session session = doc.toObject(Session.class);
                            session.setId(doc.getId());
                            clientsList.add(session);
                        }
                        Collections.sort(clientsList, (s1, s2) -> Long.compare(s1.getNumber(), s2.getNumber()));
                        viewModel.setClients(clientsList);
                    }
                });
    }

    private void updateUIWithKiosk(Kiosk kiosk) {
        tvKioskName.setText(kiosk.getName());
        tvKioskDate.setText(kiosk.getDate());
        tvInitialCircle.setText(String.valueOf(kiosk.getName().charAt(0)).toUpperCase());

        viewModel.setKiosk(kiosk);

        isHost = currentUserId != null && (currentUserId.equals(kiosk.getCreatorId()) || (kiosk.getHostIds() != null && kiosk.getHostIds().contains(currentUserId)));
        updateTabUI(viewPager.getCurrentItem());

        loadHostsData(kiosk);
    }

    private void loadHostsData(Kiosk kiosk) {
        List<String> allHostIds = new ArrayList<>();
        if (kiosk.getCreatorId() != null) allHostIds.add(kiosk.getCreatorId());
        if (kiosk.getHostIds() != null) {
            for (String id : kiosk.getHostIds()) {
                if (!allHostIds.contains(id)) allHostIds.add(id);
            }
        }

        if (allHostIds.isEmpty()) {
            viewModel.setHosts(new ArrayList<>());
            return;
        }

        db.collection("users")
                .whereIn(FieldPath.documentId(), allHostIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, User> userMap = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            userMap.put(doc.getId(), user);
                        }
                    }

                    List<User> newList = new ArrayList<>();
                    for (String id : allHostIds) {
                        User user = userMap.get(id);
                        if (user != null) {
                            User displayUser = new User(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
                            if (id.equals(kiosk.getCreatorId())) {
                                displayUser.setLastName(displayUser.getLastName() + " (Creator)");
                            }
                            newList.add(displayUser);
                        }
                    }
                    viewModel.setHosts(newList);
                });
    }

    private void updateTabUI(int position) {
        int activeColor = ContextCompat.getColor(this, R.color.brand_purple);
        int inactiveColor = ContextCompat.getColor(this, R.color.app_text_secondary);
        int transparent = ContextCompat.getColor(this, android.R.color.transparent);

        tabHost.setTextColor(inactiveColor);
        tabClients.setTextColor(inactiveColor);
        tabInfo.setTextColor(inactiveColor);
        tabHost.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabClients.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabInfo.setTypeface(null, android.graphics.Typeface.NORMAL);

        indicatorHost.setBackgroundColor(transparent);
        indicatorClients.setBackgroundColor(transparent);
        indicatorInfo.setBackgroundColor(transparent);

        btnAddHost.setVisibility(View.GONE);
        btnFallInLine.setVisibility(View.GONE);
        btnEditKiosk.setVisibility(View.GONE);

        switch (position) {
            case 0:
                tabHost.setTextColor(activeColor);
                tabHost.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorHost.setBackgroundColor(activeColor);
                if (isHost) btnAddHost.setVisibility(View.VISIBLE);
                break;
            case 1:
                tabClients.setTextColor(activeColor);
                tabClients.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorClients.setBackgroundColor(activeColor);
                btnFallInLine.setVisibility(View.VISIBLE);
                updateFallInLineButtonStatus();
                break;
            case 2:
                tabInfo.setTextColor(activeColor);
                tabInfo.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorInfo.setBackgroundColor(activeColor);
                if (isHost) {
                    btnEditKiosk.setVisibility(View.VISIBLE);
                } else {
                    btnFallInLine.setVisibility(View.VISIBLE);
                    updateFallInLineButtonStatus();
                }
                break;
        }
    }

    private void updateFallInLineButtonStatus() {
        if (currentKiosk != null) {
            boolean isOpen = "Open".equalsIgnoreCase(currentKiosk.getStatus());
            boolean alreadyInLine = false;
            List<Session> clients = viewModel.getClients().getValue();
            if (clients != null && currentUserId != null) {
                for (Session session : clients) {
                    if (currentUserId.equals(session.getUserId()) && "Waiting".equals(session.getStatus())) {
                        alreadyInLine = true;
                        break;
                    }
                }
            }

            boolean canJoin = isOpen && !alreadyInLine && !isHost;
            btnFallInLine.setEnabled(canJoin);
            btnFallInLine.setAlpha(canJoin ? 1.0f : 0.5f);
            
            if (isHost) {
                btnFallInLine.setText("Host Cannot Join");
            } else if (alreadyInLine) {
                btnFallInLine.setText("Already in Line");
            } else {
                btnFallInLine.setText(R.string.fall_in_line);
            }
        }
    }

    private void joinQueue() {
        if (currentUserId == null || currentKiosk == null || isHost) return;
        btnFallInLine.setEnabled(false);

        db.collection("sessions")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("kioskId", kioskId)
                .whereEqualTo("status", "Waiting")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        updateFallInLineButtonStatus();
                        return;
                    }

                    db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
                        if (!userDoc.exists()) return;
                        String userName = userDoc.getString("firstName") + " " + userDoc.getString("lastName");

                        db.runTransaction(transaction -> {
                            DocumentSnapshot kioskSnap = transaction.get(db.collection("kiosks").document(kioskId));
                            Long availableNumber = kioskSnap.getLong("availableNumber");
                            Long dailyLimit = kioskSnap.getLong("dailyLimit");

                            if (availableNumber == null || dailyLimit == null || availableNumber > dailyLimit) {
                                throw new FirebaseFirestoreException("Kiosk error", FirebaseFirestoreException.Code.ABORTED);
                            }

                            Map<String, Object> session = new HashMap<>();
                            session.put("userId", currentUserId);
                            session.put("userName", userName);
                            session.put("kioskId", kioskId);
                            session.put("kioskName", kioskSnap.getString("name"));
                            session.put("number", availableNumber);
                            session.put("status", "Waiting");
                            session.put("timestamp", FieldValue.serverTimestamp());

                            transaction.set(db.collection("sessions").document(), session);
                            transaction.update(db.collection("kiosks").document(kioskId), "availableNumber", availableNumber + 1);
                            return availableNumber;
                        }).addOnSuccessListener(number -> {
                            Toast.makeText(this, "Joined! #" + number, Toast.LENGTH_SHORT).show();
                            updateFallInLineButtonStatus();
                        });
                    });
                });
    }

    private void showEditKioskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_kiosk, null);
        builder.setView(dialogView);

        Spinner spinnerStatus = dialogView.findViewById(R.id.spinner_status);
        EditText etLimit = dialogView.findViewById(R.id.et_edit_limit);
        EditText etTransaction = dialogView.findViewById(R.id.et_edit_transaction);
        EditText etDescription = dialogView.findViewById(R.id.et_edit_description);
        
        String[] statuses = {"Open", "Ongoing", "Closed"};
        spinnerStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses));

        if (currentKiosk != null) {
            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i].equalsIgnoreCase(currentKiosk.getStatus())) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
            etLimit.setText(String.valueOf(currentKiosk.getDailyLimit()));
            etTransaction.setText(String.valueOf(currentKiosk.getCurrentTransaction()));
            etDescription.setText(currentKiosk.getDescription());
        }

        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_save_changes).setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", spinnerStatus.getSelectedItem().toString());
            updates.put("dailyLimit", Integer.parseInt(etLimit.getText().toString()));
            updates.put("currentTransaction", Integer.parseInt(etTransaction.getText().toString()));
            updates.put("description", etDescription.getText().toString());

            db.collection("kiosks").document(kioskId).update(updates).addOnSuccessListener(aVoid -> dialog.dismiss());
        });
        dialog.show();
    }

    private void showAddHostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_host, null);
        builder.setView(dialogView);

        EditText etSearchUser = dialogView.findViewById(R.id.et_search_user);
        RecyclerView rvUserResults = dialogView.findViewById(R.id.rv_user_results);
        AlertDialog dialog = builder.create();

        List<User> searchResults = new ArrayList<>();
        UserAdapter userAdapter = new UserAdapter(searchResults, user -> {
            db.collection("kiosks").document(kioskId).update("hostIds", FieldValue.arrayUnion(user.getId()));
            dialog.dismiss();
        });
        rvUserResults.setLayoutManager(new LinearLayoutManager(this));
        rvUserResults.setAdapter(userAdapter);

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                performUserSearch(s.toString().trim(), searchResults, userAdapter);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        dialogView.findViewById(R.id.btn_close_search).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void performUserSearch(String query, List<User> results, UserAdapter adapter) {
        if (query.isEmpty()) { results.clear(); adapter.notifyDataSetChanged(); return; }
        db.collection("users").orderBy("email").startAt(query).endAt(query + "\uf8ff").limit(10).get()
                .addOnSuccessListener(snapshots -> {
                    results.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());
                        if (!user.getId().equals(currentUserId)) results.add(user);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientsListener != null) clientsListener.remove();
        if (kioskListener != null) kioskListener.remove();
    }
}
