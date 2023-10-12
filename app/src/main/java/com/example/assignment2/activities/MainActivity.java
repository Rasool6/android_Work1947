package com.example.assignment2.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment2.R;
import com.example.assignment2.adapters.ImageAdapter;
import com.example.assignment2.models.FilesModel;
import com.example.assignment2.paintUtils.DrawView;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import petrov.kristiyan.colorpicker.ColorPicker;


public class MainActivity extends AppCompatActivity  {
     private DrawView paint;

     // creating objects of type button
     private ImageView save, color, stroke, undo,btnClear;

     // creating a RangeSlider object, which will
     // help in selecting the width of the Stroke
     private RangeSlider rangeSlider;

    private RecyclerView imageRecyclerView;
    private ProgressBar progress;
    private TextView notTxt;
    private ImageAdapter imageAdapter;
    ArrayList<FilesModel> fileList ;
    private List<String> listPermissionsNeeded;
    private View snackBarContainer;

    private View getSnackBarContainer() {
        return getWindow().getDecorView().getRootView();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         fileList=new ArrayList<>();
         // getting the reference of the views from their ids
         paint = (DrawView) findViewById(R.id.draw_view);
         rangeSlider = (RangeSlider) findViewById(R.id.rangebar);
         btnClear = (ImageView) findViewById(R.id.btn_clear);
         undo = (ImageView) findViewById(R.id.btn_undo);
         save = (ImageView) findViewById(R.id.btn_save);
         color = (ImageView) findViewById(R.id.btn_color);
         stroke = (ImageView) findViewById(R.id.btn_stroke);
         imageRecyclerView =   findViewById(R.id.imageRecyclerView);
        progress =   findViewById(R.id.progress);
        notTxt =   findViewById(R.id.notTxt);


//        getSaveFIles
        if (hasPermissions()) {
            getFromGallery();
        }

         btnClear.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 paint.clearPaint();
             }
         });
         // the undo button will remove the most
         // recent stroke from the canvas
         undo.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 paint.undo();
             }
         });

         // the save button will save the current
         // canvas which is actually a bitmap
         // in form of PNG, in the storage
         save.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 if (hasPermissions()) {


                      saveImgToGaleery();
                     getFromGallery();
                 }

             }
         });
         // the color button will allow the user
         // to select the color of his brush
         color.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 final ColorPicker colorPicker = new ColorPicker(MainActivity.this);
                 colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                             @Override
                             public void setOnFastChooseColorListener(int position, int color) {
                                 // get the integer value of color
                                 // selected from the dialog box and
                                 // set it as the stroke color
                                 paint.setColor(color);
                             }
                             @Override
                             public void onCancel() {
                                 colorPicker.dismissDialog();
                             }
                         })
                         // set the number of color columns
                         // you want  to show in dialog.
                         .setColumns(5)
                         // set a default color selected
                         // in the dialog
                         .setDefaultColorButton(Color.parseColor("#000000"))
                         .show();
             }
         });
         // the button will toggle the visibility of the RangeBar/RangeSlider
         stroke.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (rangeSlider.getVisibility() == View.VISIBLE)
                     rangeSlider.setVisibility(View.GONE);
                 else
                     rangeSlider.setVisibility(View.VISIBLE);
             }
         });

         // set the range of the RangeSlider
         rangeSlider.setValueFrom(0.0f);
         rangeSlider.setValueTo(100.0f);

         // adding a OnChangeListener which will
         // change the stroke width
         // as soon as the user slides the slider
         rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
             @Override
             public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                 paint.setStrokeWidth((int) value);
             }
         });

         // pass the height and width of the custom view
         // to the init method of the DrawView object
         ViewTreeObserver vto = paint.getViewTreeObserver();
         vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 paint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                 int width = paint.getMeasuredWidth();
                 int height = paint.getMeasuredHeight();
                 paint.init(height, width);
             }
         });
     }

    private void saveImgToGaleery() {
        Bitmap bitmap = paint.save();
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        String folderName = "/PaintImages/";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + folderName);

        ContentResolver contentResolver = getContentResolver();
        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try {
                OutputStream outputStream = contentResolver.openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                    MediaScannerConnection.scanFile(
                            this,
                            new String[]{uri.toString()},
                            new String[]{"image/jpeg"},
                            null
                    );

                    Toast.makeText(
                            this,
                            "Image saved successfully.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
        }
    }
//    private void saveImgToGaleery() {
//        // getting the bitmap from DrawView class
//        Bitmap bmp = paint.save();
//
//        // opening a OutputStream to write into the file
//        OutputStream imageOutStream = null;
//
//        ContentValues cv = new ContentValues();
//
//        Random random=new Random(10000);
//       int ran= random.nextInt();
//       String fileName=ran+"_drawing.png";
//        // name of the file
//        cv.put(MediaStore.Images.Media.DISPLAY_NAME, "_drawing.png");
//
//        // type of the file
//        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
//
//        // location of the file to be saved
//        cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/PaintImages/");
//
//        // get the Uri of the file which is to be created in the storage
//        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
//        try {
//            // open the output stream with the above uri
//            assert uri != null;
//            imageOutStream = getContentResolver().openOutputStream(uri);
//
//            // this method writes the files in storage
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
//
//            // close the output stream after use
//            imageOutStream.close();
//            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
//        }
//    }


    private boolean checkAndRequestPermissions() {
        listPermissionsNeeded = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             listPermissionsNeeded.add(android.Manifest.permission.READ_MEDIA_IMAGES);
            ActivityCompat.requestPermissions(
                    this,
                    listPermissionsNeeded.toArray(new String[0]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS_TIRAMISU
            );
            return false;
        } else {
            int readPermission = ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            );
            int writePermission = ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            );

            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(
                        this,
                        listPermissionsNeeded.toArray(new String[0]),
                        REQUEST_ID_MULTIPLE_PERMISSIONS
                );
                return false;
            } else {
                System.out.println("listPermissionNeeded: Empty");
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) || ActivityCompat.shouldShowRequestPermissionRationale(
                                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )) {
                            showSnackBar(
                                    getString(R.string.permissions_denied),
                                    getString(R.string.action_grant),
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            hasPermissions();
                                        }
                                    },
                                    Snackbar.LENGTH_SHORT);


                        } else {
                            showSnackBar(
                                    getString(R.string.permissions_denied),
                                    getString(R.string.action_settings),
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts(
                                                    "package",
                                                  getPackageName(),
                                                    null
                                            );
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    }
                                    ,
                                    Snackbar.LENGTH_INDEFINITE
                            );
                        }
                        return;
                    } else {
                        // fetchRecent();
                        System.out.println("permissions: are granted");
                    }
                }
                break;

            case REQUEST_ID_MULTIPLE_PERMISSIONS_TIRAMISU:
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this, android.Manifest.permission.READ_MEDIA_IMAGES
                        ) ) {
                            showSnackBar(
                                    getString(R.string.permissions_denied),
                                    getString(R.string.action_grant),
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            hasPermissions();
                                        }
                                    },
                                    Snackbar.LENGTH_SHORT
                            );
                        } else {
                            showSnackBar(
                                    getString(R.string.permissions_denied),
                                    getString(R.string.action_settings),
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts(
                                                    "package",
                                                     getPackageName(),
                                                    null
                                            );
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    },
                                    Snackbar.LENGTH_INDEFINITE
                            );
                        }
                        return;
                    } else {
                        //  fetchRecent();
                        System.out.println("permissions: are granted");
                    }
                }
                break;

            default:
                System.out.println("permissions: Request code don't match");
        }
    }


    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 7;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS_TIRAMISU = 8;

    private void showSnackBar(String message, String action, View.OnClickListener actionListener, int duration) {
        Snackbar snackBar = Snackbar.make(snackBarContainer, message, duration);
        snackBar.setBackgroundTint(Color.parseColor("#CC000000"));
        snackBar.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) snackBar.getView().getLayoutParams();
        layoutParams.gravity = Gravity.TOP;
        snackBar.getView().setLayoutParams(layoutParams);

        if (action != null && actionListener != null) {
            snackBar.setAction(action, actionListener);
            snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.color1));
        }
        snackBar.show();
    }
    private boolean hasPermissions() {
        checkAndRequestPermissions();
        for (String permission : listPermissionsNeeded) {
            if (ActivityCompat.checkSelfPermission(
                    this, permission
            ) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
     public void getFromGallery() {
        String selection = MediaStore.Images.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{"%DCIM/PaintImages%"};
        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_MODIFIED};

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        );

        if (cursor != null) {
            ArrayList<FilesModel> fileList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                fileList.add(new FilesModel(fileName, path, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))));
            }
            cursor.close();

            // Process the fileList and set up your RecyclerView here
            if (!fileList.isEmpty()) {
                // Set up RecyclerView and adapter
                imageAdapter = new ImageAdapter(this, fileList);
                imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                imageRecyclerView.setAdapter(imageAdapter);
                Log.d("varMsg", "getFromGallery: " + fileList.size());
                imageRecyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            } else {
                // Handle the case when no image files are found.
                Log.d("varMsg", "No image files found");
                imageRecyclerView.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                notTxt.setVisibility(View.VISIBLE);
            }
        } else {
            Log.d("varMsg", "Cursor is null");
        }
    }







}
