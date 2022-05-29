package com.swivelsoftware.mobile_app_project;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.OnImageSavedCallback;
import androidx.camera.core.ImageCapture.OutputFileOptions;
import androidx.camera.core.ImageCapture.OutputFileOptions.Builder;
import androidx.camera.core.ImageCapture.OutputFileResults;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.swivelsoftware.mobile_app_project.databinding.ActivityCameraBinding;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    ActivityCameraBinding binding;

    ImageCapture imageCapture;
    ExecutorService cameraExecutor;

    final static int REQUEST_CODE_PERMISSIONS = 1002;
    final static String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    final static String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener((() -> {
            ProcessCameraProvider cameraProvider = null;

            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

            imageCapture = new ImageCapture.Builder().build();

            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            if (cameraProvider != null) {
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            }
        }), ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cameraExecutor.shutdown();
    }

    public void takePhoto(View view) {
        if (imageCapture != null) {
            String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis());

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

            if (VERSION.SDK_INT > Build.VERSION_CODES.P) {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
            }

            OutputFileOptions outputOptions = new Builder(getContentResolver(), Media.EXTERNAL_CONTENT_URI, contentValues).build();

            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), (new OnImageSavedCallback() {
                public void onError(@NotNull ImageCaptureException exc) {
                    Toast.makeText(getBaseContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("CameraXApp", "Photo capture failed: " + exc.getMessage(), exc);
                }

                public void onImageSaved(@NotNull OutputFileResults output) {
                    Intent intent = new Intent();
                    intent.putExtra("IMAGE_URL", output.getSavedUri());
                    setResult(RESULT_OK, intent);

                    finish();
                }
            }));
        }
    }
}