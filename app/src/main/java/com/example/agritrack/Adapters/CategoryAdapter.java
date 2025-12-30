package com.example.agritrack.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.R;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private String[] categories;
    private String[] descriptions;
    private int[] images;
    private OnCategoryClickListener listener;

    // 🔹 Interface click
    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    // 🔹 Constructeur
    public CategoryAdapter(String[] categories,
                           String[] descriptions,
                           int[] images,
                           OnCategoryClickListener listener) {
        this.categories = categories;
        this.descriptions = descriptions;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {

        String category = categories[position];

        holder.tvCategoryName.setText(category);
        holder.tvCategoryInfo.setText(descriptions[position]);
        holder.ivCategoryIcon.setImageResource(images[position]);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.length;
    }

    // 🔹 ViewHolder aligné avec item_category.xml
    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCategoryIcon;
        TextView tvCategoryName, tvCategoryInfo;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryInfo = itemView.findViewById(R.id.tvCategoryInfo);
        }
    }
}
