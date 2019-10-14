package com.example.imageupload;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    public HeaderViewHolder(@NonNull View itemView, final HeaderListener headerListener) {
        super(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headerListener.onAddImage();
            }
        });
    }

    public void bind() {

    }
}
