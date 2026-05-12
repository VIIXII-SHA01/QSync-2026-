package android.bignerdranch.com.qsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private List<Session> sessionList;
    private OnItemClickListener onItemClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(Session session);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Session session, int position);
    }

    public SessionAdapter(List<Session> sessionList) {
        this.sessionList = sessionList;
    }

    public void setSessionList(List<Session> sessionList) {
        this.sessionList = sessionList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessionList.get(position);
        holder.bind(session, onItemClickListener, onDeleteClickListener, position);
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvKioskInitial;
        TextView tvKioskName;
        TextView tvStatus;
        TextView tvQueueNumber;
        ImageView ivLeaveLine;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKioskInitial = itemView.findViewById(R.id.tv_kiosk_initial);
            tvKioskName = itemView.findViewById(R.id.tv_kiosk_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvQueueNumber = itemView.findViewById(R.id.tv_queue_number);
            ivLeaveLine = itemView.findViewById(R.id.iv_leave_line);
        }

        public void bind(Session session, OnItemClickListener itemListener, OnDeleteClickListener deleteListener, int position) {
            tvKioskName.setText(session.getKioskName());
            tvStatus.setText(session.getStatus());
            tvQueueNumber.setText("#" + session.getNumber());

            if (session.getKioskName() != null && !session.getKioskName().isEmpty()) {
                tvKioskInitial.setText(session.getKioskName().substring(0, 1).toUpperCase());
            }

            itemView.setOnClickListener(v -> {
                if (itemListener != null) {
                    itemListener.onItemClick(session);
                }
            });

            if (ivLeaveLine != null) {
                ivLeaveLine.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteClick(session, position);
                    }
                });
            }
        }
    }
}
