package com.example.imageupload;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BodyViewHolder extends RecyclerView.ViewHolder {
    private ImageListener imageListener;

    public BodyViewHolder(@NonNull View itemView, ImageListener imageListener) {
        super(itemView);
        this.imageListener = imageListener;
    }

    public void bind(final String image) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageListener.onClickImage(getAdapterPosition(), image);
            }
        });
    }
}
