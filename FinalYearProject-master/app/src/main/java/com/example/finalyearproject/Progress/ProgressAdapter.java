package com.example.finalyearproject.Progress;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ProgressModel model);
    }

    private final List<ProgressModel> progressList;
    private final Context context;
    private final OnItemClickListener listener;

    public ProgressAdapter(Context context, List<ProgressModel> progressList, OnItemClickListener listener) {
        this.context = context;
        this.progressList = progressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_progress_card, parent, false);
        return new ProgressViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        ProgressModel goal = progressList.get(position);

        holder.title.setText(goal.getName());
        holder.subtitle.setText("Target: " + goal.getTargetValue() + goal.getUnit());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(goal));

        String graphUrl = goal.getGraphUrl();
        Glide.with(context)
                .load(graphUrl)
                .placeholder(R.drawable.white_placeholder)
                .error(R.drawable.white_placeholder)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return progressList.size();
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView image;
        TextView title, subtitle;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.progressImage);
            title = itemView.findViewById(R.id.progressTitle);
            subtitle = itemView.findViewById(R.id.progressSubtitle);
        }
    }
}
