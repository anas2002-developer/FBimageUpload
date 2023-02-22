package com.anas.firebaseimageupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    ImageView imgCamera;
    Button btnCamera, btnGallery, btnAdd;
    Uri uri;    //image url
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgCamera = findViewById(R.id.imgCamera);
        btnCamera=findViewById(R.id.btnCamera);
        btnGallery=findViewById(R.id.btnGallery);
        btnAdd=findViewById(R.id.btnAdd);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(iCamera, 100);
            }
        });

        btnGallery.setOnClickListener(v -> {


            //for Manifest file import in this use import android.Manifest;
            Dexter.withActivity(MainActivity.this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            Intent iGallery = new Intent(Intent.ACTION_PICK);
//                            iGallery.setType("image/*");
//                            startActivityForResult(Intent.createChooser(iGallery,"Select a image"),200);
                            iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(iGallery,200);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            //on reopen of app , again asks for permission
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();




        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setTitle("File Uploader");
                dialog.show();

                FirebaseStorage fstorage = FirebaseStorage.getInstance();
                StorageReference uploader = fstorage.getReference().child("image1");
                uploader.putFile(uri)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                                float percent = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                                dialog.setMessage("Uploaded : "+(int)percent+"%");

                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(MainActivity.this, "File Uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK){
            if (requestCode== 100){
                Bitmap camera_img = (Bitmap) data.getExtras().get("data");
                imgCamera.setImageBitmap(camera_img);

            }
            else if (requestCode==200){
                uri=data.getData();


//                try {
//                    InputStream inputStream = getContentResolver().openInputStream(uri);
//                    bitmap = BitmapFactory.decodeStream(inputStream);
//                    imgCamera.setImageBitmap(bitmap);
                imgCamera.setImageURI(uri);
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                }

            }
        }
        else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }
}