//package com.sgdigitalposter.app.repository;
//
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.media.MediaScannerConnection;
//import android.net.Uri;
//import android.os.Environment;
//import android.widget.Toast;
//
//import com.sgdigitalposter.app.R;
//import com.sgdigitalposter.app.editor.PosterActivity;
//import com.sgdigitalposter.app.utils.Constant;
//import com.sgdigitalposter.app.utils.Util;
//
//import java.io.File;
//import java.io.FileOutputStream;
//
//public class Name {
//    filePath = Environment.getExternalStorageDirectory() + File.separator
//                        + Environment.DIRECTORY_PICTURES + File.separator + getResources().getString(R.string.app_name)
//                        + File.separator + fileName;
//
//    boolean success = false;
//
//                if (!new File(filePath).exists()) {
//        try {
//            File file = new File(Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_PICTURES
//            ), "/" + getResources().getString(R.string.app_name));
//            if (!file.exists()) {
//                if (!file.mkdirs()) {
//                    Util.showLog("Can't create directory to save image.");
//                    Toast.makeText(getApplicationContext(),
//                            getResources().getString(R.string.don_t_create),
//                            Toast.LENGTH_LONG).show();
//                    success = false;
//                }
//            }
//            File file2 = new File(file.getAbsolutePath() + "/" + fileName);
//            if (file2.exists()) {
//                file2.delete();
//            }
//            Bitmap bitmap = Constant.bitmap;
//            try {
//                FileOutputStream fileOutputStream = new FileOutputStream(file2);
//                Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(),
//                        bitmap.getHeight(), bitmap.getConfig());
//                Canvas canvas = new Canvas(createBitmap);
//                canvas.drawColor(-1);
//                canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
//                checkMemory = createBitmap.compress(Bitmap.CompressFormat.PNG,
//                        100, fileOutputStream);
//                createBitmap.recycle();
//                fileOutputStream.flush();
//                fileOutputStream.close();
//
//                MediaScannerConnection.scanFile(PosterActivity.this, new String[]{file2.getAbsolutePath()},
//                        (String[]) null, new MediaScannerConnection.OnScanCompletedListener() {
//                            public void onScanCompleted(String str, Uri uri) {
//                                Util.showLog("ExternalStorage " + "Scanned " + str + ":");
//                                StringBuilder sb = new StringBuilder();
//                                sb.append("-> uri=");
//                                sb.append(uri);
//                                sb.append("-> FILE=");
//                                sb.append(file2.getAbsolutePath());
//                                Uri muri = Uri.fromFile(file2);
//                            }
//                        });
//                success = true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                success = false;
//            }
//}
