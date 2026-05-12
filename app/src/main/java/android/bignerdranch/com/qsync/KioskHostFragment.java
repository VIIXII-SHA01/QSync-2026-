package android.bignerdranch.com.qsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class KioskHostFragment extends Fragment {

    private RecyclerView rvHosts;
    private HostAdapter hostAdapter;
    private List<User> hostList = new ArrayList<>();
    private ViewKioskViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kiosk_host, container, false);
        rvHosts = view.findViewById(R.id.rv_hosts);
        rvHosts.setLayoutManager(new LinearLayoutManager(getContext()));
        hostAdapter = new HostAdapter(hostList);
        rvHosts.setAdapter(hostAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ViewKioskViewModel.class);
        viewModel.getHosts().observe(getViewLifecycleOwner(), hosts -> {
            if (hosts != null) {
                hostList.clear();
                hostList.addAll(hosts);
                hostAdapter.notifyDataSetChanged();
            }
        });
    }

    // Deprecated: Using ViewModel now
    public void updateHosts(List<User> hosts) {
        // Handled by observer
    }
}
