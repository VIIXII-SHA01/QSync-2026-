package android.bignerdranch.com.qsync;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class KioskClientsFragment extends Fragment {

    private static final String TAG = "KioskClientsFragment";
    private RecyclerView rvClients;
    private LinearLayout emptyState;
    private ClientAdapter adapter;
    private ViewKioskViewModel viewModel;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kiosk_clients, container, false);
        rvClients = view.findViewById(R.id.rv_clients);
        emptyState = view.findViewById(R.id.empty_state_clients);
        
        currentUserId = FirebaseAuth.getInstance().getUid();
        
        rvClients.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ClientAdapter(new ArrayList<>());
        rvClients.setAdapter(adapter);
        
        adapter.setOnClientClickListener(session -> {
            Kiosk currentKiosk = viewModel.getKiosk().getValue();
            if (currentKiosk != null && currentUserId != null) {
                boolean isHost = currentUserId.equals(currentKiosk.getCreatorId()) || 
                                (currentKiosk.getHostIds() != null && currentKiosk.getHostIds().contains(currentUserId));
                
                if (isHost) {
                    Intent intent = new Intent(getActivity(), EntertainClientActivity.class);
                    intent.putExtra("session_id", session.getId());
                    intent.putExtra("user_id", session.getUserId());
                    intent.putExtra("user_name", session.getUserName());
                    intent.putExtra("queue_number", session.getNumber());
                    startActivity(intent);
                }
            }
        });
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ViewKioskViewModel.class);
        viewModel.getClients().observe(getViewLifecycleOwner(), clients -> {
            Log.d(TAG, "Clients observed: " + (clients != null ? clients.size() : "null"));
            updateUI(clients);
        });
    }

    private void updateUI(List<Session> clients) {
        if (clients == null || clients.isEmpty()) {
            rvClients.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvClients.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            adapter.setClients(clients);
        }
    }
}
