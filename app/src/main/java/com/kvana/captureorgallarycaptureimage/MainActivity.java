package com.kvana.captureorgallarycaptureimage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Taken picture from gallery and capture image with camera and given runtime permission.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ImageView ivCaptureOrGalleryImage;
    private Button btPicImageFromGallery;
    private Button btCaptureImage;
    private String filePath;
    private static final int REQUEST_CODE_PICK_FILE_GALLERY = 103;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        init();
        checkPermissions();
    }

    private void init() {
        ivCaptureOrGalleryImage = findViewById(R.id.iv_camera_or_gallery_image);
        btPicImageFromGallery = findViewById(R.id.bt_select_from_gal);
        btPicImageFromGallery.setOnClickListener(this);
        btCaptureImage = findViewById(R.id.bt_select_with_cam);
        btCaptureImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_select_from_gal:
                if (checkPermissions()) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, REQUEST_CODE_PICK_FILE_GALLERY);
                }
                break;
            case R.id.bt_select_with_cam:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkPermissions()) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
                break;
        }
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_CAMERA_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap;
            if (requestCode == REQUEST_CODE_PICK_FILE_GALLERY) {
                filePath = getFilePath(this, data.getData());
                bitmap = createBitMap(filePath);
                ivCaptureOrGalleryImage.setImageBitmap(bitmap);
            } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                bitmap = (Bitmap) data.getExtras().get("data");
                ivCaptureOrGalleryImage.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "grantResults[0] >> " + grantResults[0]);
        Log.e(TAG, "grantResults[1] >> " + grantResults[1]);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            // do something
        } else if (grantResults.length > 0 && grantResults[1] == REQUEST_CODE_PICK_FILE_GALLERY) {
            // do something
        }
    }

    public static Bitmap createBitMap(String capturingImageURl) {
        File file = new File(capturingImageURl);
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    public static String getFilePath(Context context, Uri contentUri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    // Find network status wheather wifi or mobile net connected
    private boolean hasNetwork() {
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifi = false;
        if (conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null) {
            isWifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        }
        boolean is3g = false;
        if (conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
            is3g = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        }

        if (isWifi) {
            Log.e(TAG, "Wifi connected " + ConnectivityManager.TYPE_WIFI);
            return true;
        } else if (is3g) {
            Log.e(TAG, "Cellular data connected " + ConnectivityManager.TYPE_MOBILE);
            return true;
        } else {
            Log.e(TAG, "connect wifi or cellular data ");
            return false;
        }
//        return (isWifi || is3g);
    }
}
