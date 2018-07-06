package com.oops.mensa;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oops.mensa.database.Review;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RateActivity extends AppCompatActivity {

    private final int FROM_CAMERA = 0;
    private final int FROM_GALLERY = 1;
    private final int REQUEST_WRITE_STORAGE = 2;

    private String mealName;
    private String text;

    private DatabaseReference db;
    private StorageReference storage;

    private String reviewID;

    private Button cameraButton;
    private Button galleryButton;
    private ImageView imageView;
    private EditText editText;

    private String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        Button navButton = findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mealName = getIntent().getStringExtra("meal");

        db = FirebaseDatabase.getInstance().getReference().child("reviews").child(mealName);
        storage = FirebaseStorage.getInstance().getReference().child("images").child(mealName);

        galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RateActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RateActivity.this,
                            new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_WRITE_STORAGE);
                } else {
                    openGallery();
                }
            }
        });

        cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        imageView = findViewById(R.id.image_view);

        editText = findViewById(R.id.edit_text);

        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = editText.getText().toString();

                if (text.equals("")) {
                    Toast.makeText(RateActivity.this, "You must enter something!", Toast.LENGTH_SHORT).show();
                } else {
                    uploadReview(text);
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Permission denied :(", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    getImagePath(data);
                    displayPic();
                }
                break;
            case FROM_CAMERA:
                if (resultCode == RESULT_OK) {
                    displayPic();
                }
                break;
        }
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , FROM_GALLERY);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(RateActivity.this,
                        "com.oops.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, FROM_CAMERA);
            }
        } else {
            Toast.makeText(RateActivity.this, "No camera detected.", Toast.LENGTH_LONG).show();
        }
    }

    private void displayPic() {
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        if (bitmap.getWidth() > imageView.getWidth()) {
            int outWidth = imageView.getWidth();
            double width = bitmap.getWidth();
            double height = bitmap.getHeight();
            int outHeight = (int) (outWidth * (height / width));
            bitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true);
        }

        imageView.setImageBitmap(bitmap);
        imageView.setPadding(0, 20, 0, 20);
    }

    private void getImagePath(Intent data) {
        Uri imageUri = data.getData();
        Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                currentPhotoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
    }


    private void uploadReview(final String text) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Uploading");
        progress.setMessage("Wait while uploading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        if (reviewID == null) {
            reviewID = db.push().getKey();
        }

        //first upload image, then store review
        if (currentPhotoPath != null) {
            Uri imageFile = Uri.fromFile(new File(currentPhotoPath));
            UploadTask uploadTask = storage.child(reviewID).putFile(imageFile);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(RateActivity.this, "Upload failed, please retry.", Toast.LENGTH_SHORT);
                    progress.dismiss();
                }
            });

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storage.child(reviewID).getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String downloadUri = task.getResult().toString();
                        Review review = new Review(text, downloadUri);
                        db.child(reviewID).setValue(review);
                        finish();
                    }
                }
            });
        } else {
            Review review = new Review(text, null);
            db.child(reviewID).setValue(review);
            progress.dismiss();
            finish();
        }
    }
}
