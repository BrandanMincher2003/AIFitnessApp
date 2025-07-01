package com.example.finalyearproject.Workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.finalyearproject.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<String> imageUrls;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private final OnImageSelectedListener listener;

    public interface OnImageSelectedListener {
        void onImageSelected(String imageUrl);
    }

    public ImageAdapter(List<String> imageUrls, OnImageSelectedListener listener) {
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_card, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Load image using Glide with rounded corners
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .transform(new RoundedCorners(32)) // Adjust radius as needed
                .into(holder.imageView);

        // Highlight selected image with alpha effect
        holder.itemView.setAlpha(selectedPosition == position ? 1f : 0.5f);

        // Click to select
        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previous);
            notifyItemChanged(position);
            listener.onImageSelected(imageUrl);
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_preview);
        }
    }
}
