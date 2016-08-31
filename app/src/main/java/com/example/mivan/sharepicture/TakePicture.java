package com.example.mivan.sharepicture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class TakePicture extends Activity {
    private static final String LOG_TAG = "error";
    ImageView picture;
    static final int CAM_REQUEST = 1;
    Date curentTime;
    String root, fname;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = Environment.getExternalStorageDirectory().toString();

        verifyStoragePermissions(this);

        picture = (ImageView)findViewById(R.id.imageView2);
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getFile();
        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(camera_intent,CAM_REQUEST);

    }

    private File getFile()
    {
        curentTime = new Date();
        File folder = new File(root + "/Absolut");//Ruta imagen original
        if (!folder.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }

        File image_file = new File(folder,"original "+curentTime+".jpg");//Nombre imagen original
        return image_file;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent     data) {
        setAndSaveImageWithOverlay(getBitmapOfSnappedImage());
    }

    public Bitmap getBitmapOfSnappedImage(){
        String path = root + "/Absolut"+"/original "+curentTime+".jpg";//Ruta de la imagen original para ser editada

        File image = new File(path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap =     BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        return bitmap;
    }

    public void setAndSaveImageWithOverlay(Bitmap snappedImage){
        Bitmap b = Bitmap.createBitmap(snappedImage.getWidth(),     snappedImage.getHeight(), Bitmap.Config.ARGB_8888);
        //the overlay png file from drawable folder
        Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.frame);
        overlay =     Bitmap.createScaledBitmap(overlay,snappedImage.getWidth(),snappedImage.getHeight    (),false);

        //create canvas with a clean bitmap
        Canvas canvas = new Canvas(b);
        //draw the snappedImage on the canvas
        canvas.drawBitmap(snappedImage, 0, 0, new Paint());
        //draw the overlay on the canvas
        canvas.drawBitmap(overlay, 0, 0, new Paint());

        picture.setImageBitmap(b);

        SaveImage(b);

    }
    private void SaveImage(Bitmap finalBitmap) {
        File myDir = new File(root + "/Absolut");//Ruta de foto editada
        myDir.mkdirs();
        fname = curentTime+".jpg"; //Nombre foto editada
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    public void compartir(View v) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Evento Absolut"); //set your subject
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Checa esta foto, esta increíble."); //set your message

        File imageFileToShare = new File(root + "/Absolut/" + fname);

        Uri uri = Uri.fromFile(imageFileToShare);

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(shareIntent, "Share Image"));
        //Reinicia app
        /*Intent j = new Intent(this, MainActivity.class);
        startActivity(j);
        finish();*/
    }

}