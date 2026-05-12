package android.bignerdranch.com.qsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HostAdapter extends RecyclerView.Adapter<HostAdapter.HostViewHolder> {

    private List<User> hostList;

    public HostAdapter(List<User> hostList) {
        this.hostList = hostList;
    }

    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_host, parent, false);
        return new HostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {
        User user = hostList.get(position);
        holder.tvName.setText(user.getFullName());
        
        // Ensure visibility in light/dark mode
        holder.tvName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.app_text_primary));
        
        String initials = "";
        if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
            initials += String.valueOf(user.getFirstName().charAt(0)).toUpperCase();
        }
        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
            initials += String.valueOf(user.getLastName().charAt(0)).toUpperCase();
        }
        holder.tvInitial.setText(initials);
        // Initial text color is already dark grey which works on light grey circle
    }

    @Override
    public int getItemCount() {
        return hostList.size();
    }

    public static class HostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInitial;

        public HostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_host_name);
            tvInitial = itemView.findViewById(R.id.tv_host_initial);
        }
    }
}
