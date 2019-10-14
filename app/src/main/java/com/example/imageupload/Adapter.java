package com.example.imageupload;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> list = new ArrayList<>();
    private HeaderListener headerListener;
    private ImageListener imageListener;

    public Adapter(HeaderListener headerListener, ImageListener imageListener) {
        this.headerListener = headerListener;
        this.imageListener = imageListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case HEADER:
                View headerView = inflater.inflate(R.layout.item_header, parent, false);
                return new HeaderViewHolder(headerView, headerListener);
            default:
                View bodyView = inflater.inflate(R.layout.item_body, parent, false);
                return new BodyViewHolder(bodyView, imageListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER;
        }
        return BODY;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
            viewHolder.bind();
        } else {
            BodyViewHolder viewHolder = (BodyViewHolder) holder;
            int actualPosition = position - 1; // позиция без HEADER
            viewHolder.bind(list.get(actualPosition));
        }
    }

    public void addImage(String imageUrl) {
        list.add(imageUrl);
        notifyItemInserted(0);
    }

    // Header + list.size()
    @Override
    public int getItemCount() {
        return list.size() + 1;
    }

    private static final int HEADER = 0;
    private static final int BODY = 0;
}
