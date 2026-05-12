package android.bignerdranch.com.qsync;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionFragment extends Fragment {

    private static final String TAG = "SessionFragment";
    private RecyclerView rvSessions;
    private LinearLayout emptyStateContainer;
    private SessionAdapter adapter;
    private List<Session> sessionList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration sessionListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup Logo
        TextView tvLogo = view.findViewById(R.id.tv_logo);
        setupLogo(tvLogo);

        // UI Components
        rvSessions = view.findViewById(R.id.rv_sessions);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        TextView tvTopNewSession = view.findViewById(R.id.tv_top_new_session);
        MaterialButton btnCenterNewSession = view.findViewById(R.id.btn_center_new_session);

        // Setup RecyclerView
        adapter = new SessionAdapter(new ArrayList<>());
        rvSessions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSessions.setAdapter(adapter);

        adapter.setOnItemClickListener(session -> {
            Intent intent = new Intent(getActivity(), ViewKioskActivity.class);
            intent.putExtra("kiosk_id", session.getKioskId());
            intent.putExtra("kiosk_name", session.getKioskName());
            startActivity(intent);
        });

        adapter.setOnDeleteClickListener((session, position) -> {
            showLeaveLineConfirmation(session);
        });

        View.OnClickListener goToFindKiosk = v -> {
            startActivity(new Intent(getActivity(), FindKioskActivity.class));
        };

        tvTopNewSession.setOnClickListener(goToFindKiosk);
        btnCenterNewSession.setOnClickListener(goToFindKiosk);

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

    private void showLeaveLineConfirmation(Session session) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Leave Line")
                .setMessage("Are you sure you want to leave the line for " + session.getKioskName() + "?")
                .setPositiveButton("Leave", (dialog, which) -> leaveLine(session))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void leaveLine(Session session) {
        if (session.getId() == null) return;
        
        db.collection("sessions").document(session.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "You have left the line", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to leave line: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningForSessions();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (sessionListener != null) {
            sessionListener.remove();
            sessionListener = null;
        }
    }

    private void startListeningForSessions() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        if (sessionListener != null) sessionListener.remove();

        sessionListener = db.collection("sessions")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage());
                        if (getContext() != null && mAuth.getCurrentUser() != null && 
                            error.getCode() != FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    if (value != null) {
                        List<Session> updatedList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Session session = doc.toObject(Session.class);
                            session.setId(doc.getId());
                            updatedList.add(session);
                        }
                        
                        // Sort in memory by timestamp descending
                        Collections.sort(updatedList, (s1, s2) -> {
                            if (s1.getTimestamp() == null || s2.getTimestamp() == null) return 0;
                            return s2.getTimestamp().compareTo(s1.getTimestamp());
                        });

                        this.sessionList = updatedList;
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (sessionList.isEmpty()) {
            rvSessions.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            rvSessions.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.setSessionList(sessionList);
        }
    }
}
