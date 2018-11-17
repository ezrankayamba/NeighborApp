package tz.co.nezatech.neighborapp.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static android.app.Activity.RESULT_OK;

public class CameraUtil {
    public static final int RC_CAMERA_IMAGE_CAPTURE = 500;
    public static final int RC_GALLERY_PICK_PHOTO = 501;
    private static final int RC_CROP_PHOTO = 502;

    /**
     * this function does the crop operation.
     */
    private static void performCrop(Uri picUri, Activity a) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("return-data", true);
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File output = new File(dir, "CameraContentDemo.jpeg");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
            a.startActivityForResult(cropIntent, CameraUtil.RC_CROP_PHOTO);
        } catch (ActivityNotFoundException anfe) {
            Toast toast = Toast.makeText(a, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    private static Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    public static void cameraResults(int requestCode, int resultCode, Intent imageReturnedIntent, Activity a, ImageListener listener) {
        switch (requestCode) {
            case RC_CAMERA_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    performCrop(getImageUri(a, imageBitmap), a);
                }
                break;
            case RC_GALLERY_PICK_PHOTO:
                if (resultCode == RESULT_OK) {
                    performCrop(imageReturnedIntent.getData(), a);
                }
                break;
            case RC_CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    listener.ready();
                }
                break;
        }
    }

    public interface ImageListener {
        public void ready();
    }
}
