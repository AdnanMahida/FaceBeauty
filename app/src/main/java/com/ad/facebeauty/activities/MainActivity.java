package com.ad.facebeauty.activities;

import static com.ad.facebeauty.utills.PermissionUtils.REQUIRED_PERMISSION;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.ad.facebeauty.R;
import com.ad.facebeauty.utills.PermissionUtils;
import com.ad.facebeauty.utills.SaveImageFile;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CardView captureBtn, pickImageBtn, swipCamera;
    private PreviewView cameraPreviewView;
    private RelativeLayout rootLayout;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setUpClickListeners();
        requestPermission();
    }

    private void init() {
        rootLayout = findViewById(R.id.main_relativeLayout);
        captureBtn = findViewById(R.id.main_btn_capture);
        pickImageBtn = findViewById(R.id.main_btn_pickimage);
        swipCamera = findViewById(R.id.main_btn_swipcamera);
        cameraPreviewView = findViewById(R.id.main_camera_view_finder);
    }

    private void setUpClickListeners() {
        pickImageBtn.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        swipCamera.setOnClickListener(v -> {
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                lensFacing = CameraSelector.LENS_FACING_FRONT;
            } else {
                lensFacing = CameraSelector.LENS_FACING_BACK;
            }
            startCamera();
        });
    }

    private void requestPermission() {
        ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (PermissionUtils.allPermissionsGranted(result)) {
                        startCamera();
                    } else {
                        displayNeverAskAgainDialog();
                    }
                }
        );

        if (ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSION[0]) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(REQUIRED_PERMISSION);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                bindPreview(cameraProvider);
            } catch (Exception e) {
                Log.e("Exception", e.toString());
            }
        }, ContextCompat.getMainExecutor(this));
    }


    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
//        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
//        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
        // Enable the extension if available.
//            hdrImageCaptureExtender.enableExtension(cameraSelector);
//        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);


        captureBtn.setOnClickListener(v -> {
            SaveImageFile imageFile = new SaveImageFile(MainActivity.this);

            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            File file = new File(imageFile.getBatchDirectoryName(), imageFile.getAppName() + mDateFormat.format(new Date()) + ".jpg");

            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(MainActivity.this, ImagePreviewActivity.class);
                        intent.putExtra("imageUrl", file.getPath());
                        startActivity(intent);
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
        });
    }

    ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Intent intent = new Intent(MainActivity.this, DesignActivity.class);
                    intent.putExtra("imageUri", uri.toString());
                    startActivity(intent);
                } else {
                    Snackbar.make(rootLayout, "You haven't picked Image", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
    );

    public void displayNeverAskAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("We need to capture & save Image for performing necessary task. Please permit the permission through "
                + "Settings screen.\n\nSelect Permissions -> Enable permission");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.show();
    }

}