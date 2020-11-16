package com.ad.facebeauty.Utills;

import android.Manifest;

public class PermissionUtils {
    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final String[] REQUIRED_PERMISSION = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
}
