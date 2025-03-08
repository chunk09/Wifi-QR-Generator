package kr.dlrkdbs.wifiqrgenerator.func;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SMSManager {
    public void sendSMS(Context c, String message, Bitmap bitmap) {
        Uri imageUri = saveImageToExternalStorage(c, bitmap);

        if (imageUri != null) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            sendIntent.putExtra("sms_body", message);

            if (sendIntent.resolveActivity(c.getPackageManager()) != null) {
                c.startActivity(sendIntent);
            }
        }
    }

    // Bitmap을 외부 저장소에 저장하고 Uri를 반환하는 메서드
    private Uri saveImageToExternalStorage(Context c, Bitmap bitmap) {
        File imagePath = new File(c.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image.png");
        try {
            FileOutputStream fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            // FileProvider를 통해 안전한 Uri를 생성합니다.
            return FileProvider.getUriForFile(c, c.getApplicationContext().getPackageName() + ".provider", imagePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}