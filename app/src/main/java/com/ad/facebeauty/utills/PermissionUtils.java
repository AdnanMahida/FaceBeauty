package com.ad.facebeauty.utills;

import android.Manifest;

import java.util.Map;

public class PermissionUtils {
    public static final String[] REQUIRED_PERMISSION = new String[]{Manifest.permission.CAMERA};

    public static boolean allPermissionsGranted(Map<String, Boolean> grantResults) {
        for (Boolean granted : grantResults.values()) {
            if (!granted) return false;
        }
        return true;
    }

}
