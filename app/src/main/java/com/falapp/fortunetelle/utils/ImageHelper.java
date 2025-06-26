package com.falapp.fortunetelle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private Context context;

    public ImageHelper(Context context) {
        this.context = context;
    }

    /**
     * Geçici resim dosyası oluştur
     */
    public File createImageFile() throws IOException {
        // Dosya adı için zaman damgası oluştur
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Depolama dizini
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Dosya oluştur
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Uri'den Bitmap oluştur
     */
    public Bitmap getBitmapFromUri(Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();
        return bitmap;
    }

    /**
     * Bitmap'i dosyaya kaydet
     */
    public String saveBitmapToFile(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;

        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Bitmap kayıt hatası: " + e.getMessage());
        }
        return null;
    }

    /**
     * Bitmap'i belirli bir boyuta yeniden boyutlandır
     */
    public Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;

        // En küçük ölçek faktörünü kullan (oranı koru)
        float scaleFactor = Math.min(scaleWidth, scaleHeight);

        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    /**
     * Resmin yönlendirmesini düzelt (EXIF verileri)
     */
    public Bitmap fixImageOrientation(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotatedBitmap;
        } catch (IOException e) {
            Log.e(TAG, "Resim yönlendirme düzeltme hatası: " + e.getMessage());
        }
        return bitmap;
    }

    /**
     * Uri'den gerçek dosya yolunu al
     */
    public String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return uri.getPath();
        }

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();

        return path;
    }

    /**
     * Resmi döndür
     */
    public Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Bitmap'i belirlenen kalitede sıkıştır
     */
    public Bitmap compressBitmap(Bitmap bitmap, int quality) {
        byte[] bytes;
        try {
            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            bytes = stream.toByteArray();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Bitmap sıkıştırma hatası: " + e.getMessage());
        }
        return bitmap;
    }
}