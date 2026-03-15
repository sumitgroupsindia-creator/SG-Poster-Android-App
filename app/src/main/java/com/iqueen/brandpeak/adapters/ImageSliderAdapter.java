package com.sgdigitalposter.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.items.DynamicFrameItem;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private final List<DynamicFrameItem> frameList;
    private final Context context;

    public ImageSliderAdapter(Context context, List<DynamicFrameItem> imageList) {
        this.frameList = imageList;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(frameList.get(position).thumbnail,context);
    }

    @Override
    public int getItemCount() {
        return frameList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_frame);
        }

        public void bind(String imageRes, Context context) {
            Glide.with(context).load(imageRes).into(imageView);
        }
    }
}