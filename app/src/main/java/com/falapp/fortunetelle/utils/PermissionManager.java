package com.falapp.fortunetelle.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private Context context;
    private Activity activity;
    private PermissionCallback callback;
    private String[] permissions;

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public PermissionManager(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    /**
     * Kamera izni kontrolü
     */
    public void requestCameraPermission(PermissionCallback callback) {
        this.callback = callback;
        this.permissions = new String[]{Manifest.permission.CAMERA};
        requestPermissions();
    }

    /**
     * Depolama izni kontrolü
     */
    public void requestStoragePermission(PermissionCallback callback) {
        this.callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.permissions = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        } else {
            this.permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
        requestPermissions();
    }

    /**
     * Bildirim izni kontrolü
     */
    public void requestNotificationPermission(PermissionCallback callback) {
        this.callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.permissions = new String[]{Manifest.permission.POST_NOTIFICATIONS};
            requestPermissions();
        } else {
            // Android 13'ten önceki sürümlerde bildirim izni gerekmez
            callback.onPermissionGranted();
        }
    }

    /**
     * Çoklu izin kontrolü
     */
    public void requestMultiplePermissions(String[] permissions, PermissionCallback callback) {
        this.callback = callback;
        this.permissions = permissions;
        requestPermissions();
    }

    /**
     * İzinleri istemek için genel metod
     */
    private void requestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (permissionsNeeded.isEmpty()) {
            // Tüm izinler zaten verilmiş
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }

        // İzin iste
        ActivityCompat.requestPermissions(
                activity,
                permissionsNeeded.toArray(new String[0]),
                PERMISSION_REQUEST_CODE
        );
    }

    /**
     * İzin sonucunu işlemek için
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                if (callback != null) {
                    callback.onPermissionGranted();
                }
            } else {
                // İzin reddedildi, açıklama diyaloğu göster
                showPermissionExplanationDialog();
            }
        }
    }

    /**
     * İzin reddedildiğinde açıklama diyaloğu göster
     */
    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(context)
                .setTitle("İzin Gerekli")
                .setMessage("Bu özelliği kullanmak için gerekli izinleri vermeniz gerekiyor. Ayarlar sayfasına giderek izinleri etkinleştirebilirsiniz.")
                .setPositiveButton("Ayarlar", (dialog, which) -> {
                    // Uygulama ayarları sayfasına yönlendir
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    context.startActivity(intent);
                })
                .setNegativeButton("İptal", (dialog, which) -> {
                    if (callback != null) {
                        callback.onPermissionDenied();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Belirli bir iznin verilip verilmediğini kontrol et
     */
    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}