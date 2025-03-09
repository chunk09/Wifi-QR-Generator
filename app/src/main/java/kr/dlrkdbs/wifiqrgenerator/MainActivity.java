package kr.dlrkdbs.wifiqrgenerator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.dlrkdbs.wifiqrgenerator.func.Define;
import kr.dlrkdbs.wifiqrgenerator.func.SMSManager;
import kr.dlrkdbs.wifiqrgenerator.func.WifiInfo;

// 아이콘 적용하기
// apk 뽑기 (올리는용도)
// 올릴때 필요한 이미지 뽑기
// 앱 소개 글 작성
// 올리기

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    EditText etPassword;
    ImageView imgQr;
    Button btnGenerate, btnDownload, btnShare;
    Bitmap qrCodeBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        btnGenerate = findViewById(R.id.generate);
        btnDownload = findViewById(R.id.download);
        btnShare = findViewById(R.id.share);
        imgQr = findViewById(R.id.qr);
        etPassword = findViewById(R.id.et_password);

        requestPermission();

        btnGenerate.setOnClickListener((view) -> {
            // Wi-Fi 권한이 있는지 확인
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                            != PackageManager.PERMISSION_GRANTED) {
                // 권한 요청
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE},
                        PERMISSION_REQUEST_CODE);
            } else {
                WifiInfo wifiInfo = new WifiInfo(this);
                String ssid = wifiInfo.getSSID();
                String password = String.valueOf(etPassword.getText());

                String wifiConfig;

                if (password.isEmpty()) {
                    wifiConfig = "WIFI:S:" + ssid + ";T:WPA;";
                } else {
                    wifiConfig = "WIFI:S:" + ssid + ";T:WPA;P:" + password + ";;";
                }

                Log.i("Wifi", "onCreate: ssid: " + ssid + " and password: " + password);

                qrCodeBitmap = generateQRCode(wifiConfig, 500, 500);
                Define.getInstance().qr = qrCodeBitmap;
                imgQr.setImageBitmap(qrCodeBitmap);

                btnDownload.setVisibility(View.VISIBLE);
                btnShare.setVisibility(View.VISIBLE);

                imgQr.setPadding(10, 10, 10, 10);
            }
        });

        btnDownload.setOnClickListener(view -> saveImageToGallery(this, qrCodeBitmap));
        btnShare.setOnClickListener(view -> {
            SMSManager manager = new SMSManager();
            manager.sendSMS(this, "QR 코드 공유", qrCodeBitmap);
        });
    }

    // 문자열을 받아 QR 코드를 생성하는 메서드
    private Bitmap generateQRCode(String data, int width, int height) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** 안드로이드 10 이상에는 작동 안됨
    private void saveImageToGallery(Context context, Bitmap bitmap) {
        // 저장할 디렉토리 및 파일명 설정
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String fileName = "Wifi-QR.png";

        // 이미지 파일 생성
        File file = new File(directory, fileName);

        // 파일 저장 작업
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            // 갤러리 스캔하여 새로운 파일을 인식하도록 함
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
            Toast.makeText(context, "이미지가 갤러리에 다운로드 되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "이미지를 다운 받는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    */

    private void saveImageToGallery(Context context, Bitmap bitmap) {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "Wifi-QR.png");
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WifiQR");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri == null) {
                    throw new IOException("Failed to create new MediaStore record.");
                }
                fos = resolver.openOutputStream(imageUri);
            } else {
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WifiQR");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                File file = new File(directory, "Wifi-QR.png");
                fos = new FileOutputStream(file);
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(context, "이미지가 갤러리에 다운로드 되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "이미지를 다운 받는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }




    private void requestPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                Log.d("Permission", "onPermissionGranted: Permission Granted");
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Permission", "onPermissionGranted: Permission Denied");
            }
        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.SEND_SMS
                )
                .check();

        // 권한 요청
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE},
                PERMISSION_REQUEST_CODE);
    }

}
