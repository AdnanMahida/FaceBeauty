package com.ad.facebeauty.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ad.facebeauty.R;
import com.ad.zoomimageview.ZoomImageView;

import java.io.File;
import java.io.InputStream;
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
            final InputStream stream = getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClose(View view) {
        File fDelete = new File(Objects.requireNonNull(imageUri.getPath()));
        if (fDelete.exists()) {
            if (fDelete.delete()) {
                finish();
            } else {
                finish();
            }
        }

    }

    public void onCorrect(View view) {
        Intent intent = new Intent(ImagePreviewActivity.this, DesignActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
        finish();
    }
}