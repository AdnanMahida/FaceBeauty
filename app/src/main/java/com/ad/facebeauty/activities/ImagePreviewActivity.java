package com.ad.facebeauty.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ad.facebeauty.R;
import com.ad.facebeauty.utills.ImageUtils;
import com.ad.zoomimageview.ZoomImageView;

import java.io.File;
import java.util.Objects;

public class ImagePreviewActivity extends AppCompatActivity {
    private ZoomImageView imageView;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        imageView = findViewById(R.id.preview_imageview);
        getImageUriFromIntent();
    }

    private void getImageUriFromIntent() {
        try {
            Intent intent = getIntent();
            imageUri = Uri.fromFile(new File(Objects.requireNonNull(intent.getStringExtra("imageUrl"))));
            if (imageUri == null) {
                throw new IllegalArgumentException("Image path is null");
            }
            Bitmap rotatedBitmap = ImageUtils.getCorrectlyOrientedBitmap(this, imageUri);
            imageView.setImageBitmap(rotatedBitmap);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }


    public void onClose(View view) {
        File fDelete = new File(Objects.requireNonNull(imageUri.getPath()));
        if (fDelete.exists()) {
            finish();
        }

    }

    public void onCorrect(View view) {
        Intent intent = new Intent(ImagePreviewActivity.this, DesignActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
        finish();
    }
}