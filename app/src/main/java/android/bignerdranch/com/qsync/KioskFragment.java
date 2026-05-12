package android.bignerdranch.com.qsync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class KioskFragment extends Fragment {

    private static final String TAG = "KioskFragment";
    private static final String PREFS_NAME = "KioskPrefs";
    private static final String PREF_SKIP_DELETE_CONFIRM = "skipDeleteConfirm";

    private RecyclerView rvKiosks;
    private LinearLayout emptyStateContainer;
    private KioskAdapter adapter;
    private List<Kiosk> kioskList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration kioskListener;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kiosk, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Setup Logo
        TextView tvLogo = view.findViewById(R.id.tv_logo);
        setupLogo(tvLogo);

        // UI Components
        rvKiosks = view.findViewById(R.id.rv_kiosks);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        TextView tvTopCreateKiosk = view.findViewById(R.id.tv_top_create_kiosk);
        MaterialButton btnCenterCreateKiosk = view.findViewById(R.id.btn_center_create_kiosk);

        // Setup RecyclerView
        adapter = new KioskAdapter(new ArrayList<>());
        rvKiosks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKiosks.setAdapter(adapter);

        // Set Delete Click Listener
        adapter.setOnDeleteClickListener((kiosk, position) -> {
            if (sharedPreferences.getBoolean(PREF_SKIP_DELETE_CONFIRM, false)) {
                deleteKiosk(kiosk);
            } else {
                showDeleteConfirmationDialog(kiosk);
            }
        });

        // Set Item Click Listener
        adapter.setOnItemClickListener(kiosk -> {
            Intent intent = new Intent(getActivity(), ViewKioskActivity.class);
            intent.putExtra("kiosk_id", kiosk.getId());
            intent.putExtra("kiosk_name", kiosk.getName());
            intent.putExtra("kiosk_date", kiosk.getDate());
            startActivity(intent);
        });

        View.OnClickListener onCreateKiosk = v -> {
            startActivity(new Intent(getActivity(), AddKioskActivity.class));
        };

        tvTopCreateKiosk.setOnClickListener(onCreateKiosk);
        btnCenterCreateKiosk.setOnClickListener(onCreateKiosk);

        return view;
    }

    private void setupLogo(TextView tvLogo) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString qs = new SpannableString("QS");
        qs.setSpan(new ForegroundColorSpan(requireContext().getColor(R.color.brand_purple)), 0, qs.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString ync = new SpannableString("ync");
        ync.setSpan(new ForegroundColorSpan(requireContext().getColor(R.color.brand_teal)), 0, ync.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(qs).append(ync);
        tvLogo.setText(builder);
    }

    private void showDeleteConfirmationDialog(Kiosk kiosk) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_kiosk, null);
        CheckBox cbDontShowAgain = dialogView.findViewById(R.id.cb_dont_show_again);

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Kiosk")
                .setMessage("Are you sure you want to delete this kiosk? This will also remove all people currently in line.")
                .setView(dialogView)
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (cbDontShowAgain.isChecked()) {
                        sharedPreferences.edit().putBoolean(PREF_SKIP_DELETE_CONFIRM, true).apply();
                    }
                    deleteKiosk(kiosk);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteKiosk(Kiosk kiosk) {
        if (kiosk.getId() == null) return;

        // First, find all sessions related to this kiosk
        db.collection("sessions")
                .whereEqualTo("kioskId", kiosk.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    
                    // Add each session deletion to the batch
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    
                    // Add the kiosk deletion to the batch
                    batch.delete(db.collection("kiosks").document(kiosk.getId()));
                    
                    // Commit the batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Kiosk and all related sessions deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error finding sessions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningForKiosks();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (kioskListener != null) {
            kioskListener.remove();
            kioskListener = null;
        }
    }

    private void startListeningForKiosks() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        if (kioskListener != null) kioskListener.remove();

        kioskListener = db.collection("kiosks")
                .whereEqualTo("creatorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage());
                        // Only show toast if user is still logged in and it's not a permission error (common on logout)
                        if (getContext() != null && mAuth.getCurrentUser() != null && 
                            error.getCode() != FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Toast.makeText(getContext(), "Sync Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    if (value != null) {
                        List<Kiosk> updatedList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Kiosk kiosk = doc.toObject(Kiosk.class);
                            kiosk.setId(doc.getId());
                            updatedList.add(kiosk);
                        }
                        this.kioskList = updatedList;
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (kioskList.isEmpty()) {
            rvKiosks.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            rvKiosks.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.setKioskList(kioskList);
        }
    }
}
