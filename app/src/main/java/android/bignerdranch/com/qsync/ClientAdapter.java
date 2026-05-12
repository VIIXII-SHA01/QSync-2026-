package android.bignerdranch.com.qsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Session> clients;
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onClientClick(Session session);
    }

    public ClientAdapter(List<Session> clients) {
        this.clients = clients;
    }

    public void setClients(List<Session> clients) {
        this.clients = clients;
        notifyDataSetChanged();
    }

    public void setOnClientClickListener(OnClientClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Session session = clients.get(position);
        holder.bind(session, listener);
    }

    @Override
    public int getItemCount() {
        return clients != null ? clients.size() : 0;
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName, tvNumber, tvStatus;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tv_client_initial);
            tvName = itemView.findViewById(R.id.tv_client_name);
            tvNumber = itemView.findViewById(R.id.tv_client_number);
            tvStatus = itemView.findViewById(R.id.tv_client_status);
        }

        public void bind(Session session, OnClientClickListener listener) {
            String name = session.getUserName();
            long number = session.getNumber();

            tvName.setText(name != null ? name : "Unknown Client");
            tvNumber.setText(String.valueOf(number));
            
            if (name != null && !name.isEmpty()) {
                tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
            } else {
                tvInitial.setText("?");
            }

            // Display "Done" if status is Closed
            if ("Closed".equalsIgnoreCase(session.getStatus())) {
                tvStatus.setVisibility(View.VISIBLE);
            } else {
                tvStatus.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClientClick(session);
                }
            });
        }
    }
}
