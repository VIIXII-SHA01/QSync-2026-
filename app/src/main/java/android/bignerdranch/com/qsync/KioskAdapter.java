package android.bignerdranch.com.qsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class KioskAdapter extends RecyclerView.Adapter<KioskAdapter.KioskViewHolder> {

    private List<Kiosk> kioskList;
    private OnDeleteClickListener onDeleteClickListener;
    private OnItemClickListener onItemClickListener;
    private boolean showDeleteButton = true;

    public interface OnDeleteClickListener {
        void onDeleteClick(Kiosk kiosk, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(Kiosk kiosk);
    }

    public KioskAdapter(List<Kiosk> kioskList) {
        this.kioskList = (kioskList != null) ? kioskList : new ArrayList<>();
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setShowDeleteButton(boolean show) {
        this.showDeleteButton = show;
        notifyDataSetChanged();
    }

    public void setKioskList(List<Kiosk> kioskList) {
        this.kioskList = (kioskList != null) ? kioskList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Kiosk getKioskAt(int position) {
        if (kioskList != null && position >= 0 && position < kioskList.size()) {
            return kioskList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public KioskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kiosk, parent, false);
        return new KioskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KioskViewHolder holder, int position) {
        if (kioskList == null || position >= kioskList.size()) return;
        
        Kiosk kiosk = kioskList.get(position);
        if (kiosk == null) return;

        holder.tvTitle.setText(kiosk.getName());
        holder.tvDate.setText(kiosk.getDate());
        
        if (kiosk.getName() != null && !kiosk.getName().isEmpty()) {
            holder.tvInitial.setText(String.valueOf(kiosk.getName().charAt(0)).toUpperCase());
        } else {
            holder.tvInitial.setText("");
        }

        holder.ivDelete.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);
        holder.ivDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(kiosk, position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(kiosk);
            }
        });
    }

    @Override
    public int getItemCount() {
        return kioskList != null ? kioskList.size() : 0;
    }

    public static class KioskViewHolder extends RecyclerView.ViewHolder {
        public TextView tvInitial, tvTitle, tvDate;
        public ImageView ivDelete;

        public KioskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tv_initial);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
