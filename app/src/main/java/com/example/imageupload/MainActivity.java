package com.example.imageupload;

import android.os.Bundle;

import java.io.File;

import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends BasePhotoActivity implements HeaderListener, ImageListener {
    private RecyclerView recyclerView;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new Adapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAddImage() {
        // Добавляй картинку
        showPickImageDialog();
    }

    @Override
    public void onClickImage(int position, String image) {

    }

    @Override
    void onAddImage(File file) {
        adapter.addImage(file.getPath());
    }
}