package com.example.imageupload;


import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.example.imageupload.BasePhotoActivityPermissionsDispatcher.pickPhotoFromGalleryWithPermissionCheck;
import static com.example.imageupload.BasePhotoActivityPermissionsDispatcher.takePhotoFromCameraWithPermissionCheck;

@RuntimePermissions
abstract class BasePhotoActivity extends AppCompatActivity {
    private String filename;

    protected void showPickImageDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_list, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        TextView gallery = (TextView) dialogView.findViewById(R.id.gallery);
        TextView camera = (TextView) dialogView.findViewById(R.id.camera);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPhotoFromGalleryWithPermissionCheck(BasePhotoActivity.this);
                alertDialog.dismiss();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhotoFromCameraWithPermissionCheck(BasePhotoActivity.this);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "images"), 1);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    Uri getCaptureImageOutputUri(Context context, String fileName) {
        Uri outputFileUri = null;
        File imageFile = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (imageFile != null) {
            outputFileUri = Uri.fromFile(new File(imageFile.getPath(), "$fileName.jpeg"));
        }
        return outputFileUri;
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    protected void takePhotoFromCamera() {
        filename = String.valueOf(System.nanoTime());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = getCaptureImageOutputUri(this, filename);
        if (uri != null) {
            File file = new File(uri.getPath());
            if (Build.VERSION.SDK_INT >= 24) {
                intent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(this, "${this.packageName}.provider", file)
                );
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, 2);
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void pickPhotoFromGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    Uri getPickImageResultUri(Context context, Intent data, String fileName) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE;
        }
        return (isCamera || data.getData() == null ? getCaptureImageOutputUri(context, fileName) : data.getData());
    }

    Uri getNormalizedUri(Context context, Uri uri) {
        if (uri != null && uri.toString().contains("content:"))
            return Uri.fromFile(getPath(context, uri, MediaStore.Images.Media.DATA));
        else
            return uri;
    }

    private File getPath(Context context, Uri uri, String column) {
        String[] columns = new String[]{column};
        Cursor cursor = context.getContentResolver().query(uri, columns, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(column);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return new File(path);
    }

    private File createTemporalFile(Context context, String filePath) {
        return new File(context.getExternalCacheDir(), "$filePath.jpg"); // context needed
    }

    private File createTemporalFileFrom(Context context, InputStream inputStream) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = createTemporalFile(context, filename);
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            while (true) {
                read = inputStream.read(buffer);
                if (read == -1)
                    break;
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    String getImagePathFromInputStreamUri(Context context, Uri uri) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri); // context needed
                File photoFile = createTemporalFileFrom(context, inputStream);

                filePath = photoFile.getPath();

            } catch (FileNotFoundException e) {
                // log
            } catch (IOException e) {
                // log
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 0:
                    Uri uri = getPickImageResultUri(this, data, filename);
                    Uri uriFile = getNormalizedUri(this, uri);
                    onAddImage(new File(uriFile.getPath()));
                    break;

                case 1:
                    if (data != null && data.getData() != null) {
                        String fileName = getImagePathFromInputStreamUri(this, data.getData());
                        onAddImage(new File(fileName));
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BasePhotoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    abstract void onAddImage(File file);

    protected boolean isCameraPermissionGiven() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }
}