package android.bignerdranch.com.qsync;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class SessionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);

        // Setup Logo
        TextView tvLogo = view.findViewById(R.id.tv_logo);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        
        SpannableString qs = new SpannableString("QS");
        qs.setSpan(new ForegroundColorSpan(requireContext().getColor(R.color.brand_purple)), 0, qs.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        SpannableString ync = new SpannableString("ync");
        ync.setSpan(new ForegroundColorSpan(requireContext().getColor(R.color.brand_teal)), 0, ync.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        builder.append(qs).append(ync);
        tvLogo.setText(builder);

        // Buttons for "New Session"
        TextView tvTopNewSession = view.findViewById(R.id.tv_top_new_session);
        MaterialButton btnCenterNewSession = view.findViewById(R.id.btn_center_new_session);

        View.OnClickListener goToFindKiosk = v -> {
            startActivity(new Intent(getActivity(), FindKioskActivity.class));
        };

        tvTopNewSession.setOnClickListener(goToFindKiosk);
        btnCenterNewSession.setOnClickListener(goToFindKiosk);

        return view;
    }
}
