package com.example.finalyearproject.Account;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;

import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private final List<AchievementItem> achievementList;
    private final Context context;

    public AchievementAdapter(Context context, List<AchievementItem> achievementList) {
        this.context = context;
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement_card, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        AchievementItem item = achievementList.get(position);
        holder.name.setText(item.getName());
        holder.description.setText(item.getDescription());

        // Load image
        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.ic_achievement)
                .into(holder.image);

        if (!item.isHasAchieved()) {
            // Apply greyscale filter
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);  // desaturate
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.image.setColorFilter(filter);

            holder.name.setTextColor(ContextCompat.getColor(context, R.color.grey_700));
            holder.description.setTextColor(ContextCompat.getColor(context, R.color.grey_500));
            holder.itemView.setAlpha(1f); // No transparency
        } else {
            holder.image.clearColorFilter();
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.description.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, description;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.achievement_image);
            name = itemView.findViewById(R.id.achievement_title);
            description = itemView.findViewById(R.id.achievement_description);
        }
    }
}
