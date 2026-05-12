package android.bignerdranch.com.qsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class KioskInfoFragment extends Fragment {

    private TextView tvDescription, tvLimit, tvStatus, tvTransaction;
    private ViewKioskViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kiosk_info, container, false);
        tvDescription = view.findViewById(R.id.tv_info_description);
        tvLimit = view.findViewById(R.id.tv_info_limit);
        tvStatus = view.findViewById(R.id.tv_info_status);
        tvTransaction = view.findViewById(R.id.tv_info_transaction);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ViewKioskViewModel.class);
        viewModel.getKiosk().observe(getViewLifecycleOwner(), this::updateInfo);
    }

    private void updateInfo(Kiosk kiosk) {
        if (kiosk == null) return;
        if (tvDescription != null) tvDescription.setText(kiosk.getDescription());
        if (tvLimit != null) tvLimit.setText(String.valueOf(kiosk.getDailyLimit()));
        if (tvTransaction != null) tvTransaction.setText(String.valueOf(kiosk.getCurrentTransaction()));
        
        if (tvStatus != null) {
            tvStatus.setText(kiosk.getStatus());
            if (getContext() != null) {
                if ("Open".equalsIgnoreCase(kiosk.getStatus())) {
                    tvStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_teal));
                } else if ("Ongoing".equalsIgnoreCase(kiosk.getStatus())) {
                    tvStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_purple));
                } else {
                    tvStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.text_grey));
                }
            }
        }
    }
}
