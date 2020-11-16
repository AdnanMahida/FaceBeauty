package com.ad.facebeauty.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ad.facebeauty.Adapter.MainToolAdapter;
import com.ad.facebeauty.R;
import com.ad.facebeauty.Utills.ColorPickerSeekbar;
import com.ad.facebeauty.Utills.FaceGlow;
import com.ad.facebeauty.Utills.LipDraw;
import com.ad.facebeauty.Utills.SaveImageFile;
import com.ad.facebeauty.Utills.ToolType;
import com.ad.zoomimageview.ZoomImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DesignActivity extends AppCompatActivity implements MainToolAdapter.OnToolItemSelected {
    public static final int NEW_IMAGE_REQUEST = 2;
    private static int tempColor = Color.RED, tempAlpha = 80, tempColorProcess = 0;
    private Uri imageUri;
    private Bitmap newTempBitmap;
    private List<Bitmap> bitmapList = new ArrayList<>();
    private InputImage inputImage;
    private ZoomImageView mainImageView;
    private FloatingActionButton btnFaceDetect;
    private RecyclerView mainToolView;
    private List<Face> mainFaceList = new ArrayList<>();
    private View scannerView;
    private Animation animation;
    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design);
        init();
        initMainTool();
        getImageUriFromIntent();
        initScannig();

        btnFaceDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputImage != null) {
                    detectFaces(inputImage);
                    scannerView.setVisibility(View.VISIBLE);
                    scannerView.startAnimation(animation);
                }
            }
        });
    }

    private void initMainTool() {
        MainToolAdapter mainToolAdapter = new MainToolAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mainToolView.setLayoutManager(layoutManager);
        mainToolView.setAdapter(mainToolAdapter);
    }

    private void initScannig() {
        animation = AnimationUtils.loadAnimation(DesignActivity.this, R.anim.scannig);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                scannerView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


    private void init() {
        mainImageView = findViewById(R.id.design_imageview);
        mainToolView = findViewById(R.id.design_recycle);
        btnFaceDetect = findViewById(R.id.design_btn_facedetect);
        scannerView = findViewById(R.id.design_scanner);
        rootLayout = findViewById(R.id.design_rootlayout);
    }

    private void getImageUriFromIntent() {
        try {
            Intent intent = getIntent();
            imageUri = Uri.parse(Objects.requireNonNull(intent.getExtras()).getString("imageUri"));
            final InputStream stream = getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
            newTempBitmap = bitmap;
            bitmapList.add(newTempBitmap);
            mainImageView.setImageBitmap(bitmap);
            inputImage = InputImage.fromFilePath(DesignActivity.this, imageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void detectFaces(InputImage image) {
        try {
            FaceDetectorOptions options =
                    new FaceDetectorOptions.Builder()
                            .setClassificationMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                            .setMinFaceSize(0.15f)
                            .enableTracking()
                            .build();

            FaceDetector detector = FaceDetection.getClient(options);

            Task<List<Face>> result =
                    detector.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<Face>>() {
                                        @Override
                                        public void onSuccess(List<Face> faces) {
                                            scannerView.clearAnimation();
                                            if (!faces.isEmpty()) {
                                                if (mainFaceList != null) {
                                                    mainFaceList.clear();
                                                }
                                                mainFaceList = faces;
                                                mainToolView.setVisibility(View.VISIBLE);
                                                btnFaceDetect.setVisibility(View.GONE);
                                            } else {
                                                noFaceAvailableDialog();
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            scannerView.clearAnimation();
                                            noFaceAvailableDialog();
                                        }
                                    });
        } catch (Exception e) {
            e.printStackTrace();
            noFaceAvailableDialog();
        }

    }

    private void noFaceAvailableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DesignActivity.this);
        builder.setTitle("No Face Available").setMessage("Sorry,can't detect face\nso beauty option is disable");
        builder.setPositiveButton("Ok", null).create().show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.design_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.designmenu_camera: {
                Intent i = new Intent(DesignActivity.this, MainActivity.class);
                startActivity(i);
                finishAffinity();
                break;
            }
            case R.id.designmenu_save: {
                if (newTempBitmap != null) {
                    SaveImageFile imageFile = new SaveImageFile(DesignActivity.this);
                    imageFile.SaveImage(newTempBitmap, rootLayout);
                }
                break;
            }
            case R.id.designmenu_newimage: {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "Select Picture"), NEW_IMAGE_REQUEST);
                break;
            }
            case R.id.designmenu_share: {
                shareImageUri(imageUri);
                break;
            }
            case R.id.designmenu_undo: {
                onUndoPress();
                break;
            }
            case R.id.designmenu_redo: {
                onRedoPress();
                break;
            }

//            case R.id.designmenu_helpfeedback:
//                startActivity(new Intent(DesignActivity.this, HelpFeedBackActivity.class));
//                break;
//            case R.id.designmenu_settings:
//                startActivity(new Intent(DesignActivity.this, SettingsActivity.class));
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onRedoPress() {
        try {
            if (!(bitmapList.size() >= 0)) {
                Toast.makeText(this, "No Changes detected", Toast.LENGTH_SHORT).show();
            } else {
                newTempBitmap = bitmapList.get(bitmapList.indexOf(newTempBitmap) + 1);
            }
            mainImageView.setImageDrawable(new BitmapDrawable(getResources(), newTempBitmap));
        } catch (Exception e) {
            Toast.makeText(this, "No Changes detected", Toast.LENGTH_SHORT).show();
        }
    }

    private void onUndoPress() {
        try {
            if (!(bitmapList.size() > 1)) {
                Toast.makeText(this, "No Changes detected", Toast.LENGTH_SHORT).show();
                newTempBitmap = bitmapList.get(0);
            } else {
                newTempBitmap = bitmapList.get(bitmapList.indexOf(newTempBitmap) - 1);
            }
            mainImageView.setImageDrawable(new BitmapDrawable(getResources(), newTempBitmap));
        } catch (Exception e) {
            Toast.makeText(this, "No Changes detected", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImageUri(Uri uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == NEW_IMAGE_REQUEST) {
                if (data != null) {
                    try {
                        imageUri = data.getData();
                        final InputStream stream = getContentResolver().openInputStream(imageUri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inMutable = true;
                        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
                        if (newTempBitmap != null && !newTempBitmap.isRecycled()) {
                            newTempBitmap.recycle();
                            newTempBitmap = null;
                        }
                        bitmapList.clear();
                        newTempBitmap = bitmap;
                        bitmapList.add(newTempBitmap);
                        btnFaceDetect.setVisibility(View.VISIBLE);
                        mainToolView.setVisibility(View.GONE);
                        mainImageView.setImageBitmap(bitmap);
                        inputImage = InputImage.fromFilePath(DesignActivity.this, imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Snackbar.make(rootLayout, "You haven't picked Image", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DesignActivity.this);
        builder.setTitle("Confirmation").setMessage("Are you sure you want to exit without saving image")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DesignActivity.this.finish();
                    }
                });
        builder.setNegativeButton("Cancel", null).setNeutralButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DesignActivity.this.finish();
            }
        }).create().show();
    }


    @Override
    public void OnToolItemSelected(ToolType toolType) {
        showLipsBeautyDialog(toolType);
    }


    @SuppressLint("SetTextI18n")
    public void showLipsBeautyDialog(ToolType toolType) {
        final BottomSheetDialog dialog = new BottomSheetDialog(DesignActivity.this);
        dialog.setContentView(R.layout.dialog_lips_prompt);
        dialog.setCancelable(false);
        ImageView correctBtn = dialog.findViewById(R.id.lips_dialog_img_correct);
        ImageView inCorrectBtn = dialog.findViewById(R.id.lips_dialog_img_incorrect);
        ColorPickerSeekbar colorPickerSeekbar = dialog.findViewById(R.id.lips_dialog_seekbar_color);
        SeekBar alphaSeekBar = dialog.findViewById(R.id.lips_dialog_seekbar_alpha);
        TextView txtApha = dialog.findViewById(R.id.lips_dialog_txt_alpha);
        TextView txtColor = dialog.findViewById(R.id.lips_dialog_txt_color);

        if (toolType == ToolType.FACE_GLOW) {
            colorPickerSeekbar.setVisibility(View.GONE);
            txtColor.setVisibility(View.GONE);
            tempColor = Color.WHITE;
            alphaSeekBar.setProgress(15);
            tempAlpha =15;
        }else if (toolType == ToolType.LIPS_BEAUTY){
            alphaSeekBar.setProgress(80);
            tempAlpha =80;
        }
        colorPickerSeekbar.init();
        txtColor.setText("Selected Color");
        txtColor.setTextColor(tempColor);
        colorPickerSeekbar.setProgress(tempColorProcess);
        colorPickerSeekbar.setOnColorSeekbarChangeListener(new ColorPickerSeekbar.OnColorSeekBarChangeListener() {
            @Override
            public void onColorChanged(SeekBar seekBar, int color, boolean b) {
                tempColorProcess = seekBar.getProgress();
                txtColor.setTextColor(color);
                txtColor.setText("Selected Color");
                tempColor = color;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        alphaSeekBar.setMax(150);
        alphaSeekBar.setProgress(tempAlpha);
        txtApha.setText("Alpha :-" + alphaSeekBar.getProgress());
        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtApha.setText("Alpha :-" + progress);
                tempAlpha = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Objects.requireNonNull(inCorrectBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Objects.requireNonNull(correctBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (toolType) {
                    case LIPS_BEAUTY:
                        LipDraw draw = new LipDraw();
                        newTempBitmap = draw.drawFace(newTempBitmap, mainFaceList.get(0), tempColor, tempAlpha);
                        bitmapList.add(newTempBitmap);
                        break;
                    case FACE_GLOW:
                        FaceGlow glow = new FaceGlow();
                        newTempBitmap = glow.drawFace(newTempBitmap, mainFaceList.get(0), tempColor, tempAlpha);
                        bitmapList.add(newTempBitmap);
                        break;
                }
                mainImageView.setImageDrawable(new BitmapDrawable(getResources(), newTempBitmap));
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}