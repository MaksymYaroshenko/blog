package com.example.blog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;


public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 100;
    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private EditText newPostDescription;
    private Button newPostButton;
    private ProgressBar newPostProgress;

    private Uri postImageUri = null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String currentUserId;
    private Bitmap compressedImageFile;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        newPostToolbar = findViewById(R.id.newPostToolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add new Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.newPostImage);
        newPostDescription = findViewById(R.id.newPostDescription);
        newPostButton = findViewById(R.id.postButton);
        newPostProgress = findViewById(R.id.newPostProgress);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewPostActivity.this);
            }
        });

        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String description = newPostDescription.getText().toString();

                if (!TextUtils.isEmpty(description) && postImageUri != null) {
                    newPostProgress.setVisibility(View.VISIBLE);
                    final String randomeName = RandomString();
                    final StorageReference filePath = storageReference.child("post_images").child(randomeName + ".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                File newImageFile = new File(postImageUri.getPath());
                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                final StorageReference filePathThumbs = storageReference.child("post_images/thumbs").child(randomeName + ".jpg");
                                final UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomeName + ".jpg").putBytes(thumbData);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                final String downloadUri = uri.toString();
                                                filePathThumbs.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String downloadthumbUri = uri.toString();
                                                        Map<String, Object> postMap = new HashMap<>();
                                                        postMap.put("image_url", downloadUri);
                                                        postMap.put("image_thumb", downloadthumbUri);
                                                        postMap.put("desc", description);
                                                        postMap.put("user_id", currentUserId);
                                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                                    startActivity(mainIntent);
                                                                    finish();

                                                                } else {
                                                                    String errorMessage = task.getException().getMessage();
                                                                    Toast.makeText(NewPostActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                                                }
                                                                newPostProgress.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });


                            } else {
                                newPostProgress.setVisibility(View.INVISIBLE);
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String RandomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}