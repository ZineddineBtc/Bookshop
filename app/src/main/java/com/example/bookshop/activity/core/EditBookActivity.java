package com.example.bookshop.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class EditBookActivity extends AppCompatActivity {
    private ImageView photoIV, bookIV;
    private TextView nameTV, phoneTV, cityTV, errorTV;
    private EditText titleET, descriptionET, priceET;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private String email, bookID, price, title, description,
                   editedPrice, editedTitle, editedDescription;
    private boolean imageEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);
        setActionBarTitle("Edit Post");
        getInstances();
        findViewsByIds();
        getBookPhoto();
        getUserData();
    }
    private void getInstances(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        bookID = getIntent().getStringExtra(StaticClass.BOOK_ID);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        bookIV = findViewById(R.id.bookIV);
        bookIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importImage();
            }
        });
        nameTV = findViewById(R.id.nameTV);
        phoneTV = findViewById(R.id.phoneTV);
        cityTV = findViewById(R.id.cityTV);
        priceET = findViewById(R.id.priceET);
        titleET = findViewById(R.id.titleET);
        descriptionET = findViewById(R.id.descriptionET);
        errorTV = findViewById(R.id.errorTV);
    }
    private void getUserData(){
        getProfilePhoto();
        nameTV.setText(sharedPreferences.getString(StaticClass.NAME, "no name"));
        phoneTV.setText(sharedPreferences.getString(StaticClass.PHONE, "no phone"));
        cityTV.setText(sharedPreferences.getString(StaticClass.CITY, "no city"));
    }
    private void getBookPhoto(){
        if(bookID == null){
            displayErrorTV(R.string.error);
            startActivity(new Intent(getApplicationContext(), MyBooksActivity.class));
            return;
        }
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(bookID)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToBookPhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failure at downloading profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToBookPhoto(byte[] bytes){
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bookIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, bookIV.getWidth(),
                bookIV.getHeight(), false));
        getOfferData();
    }
    private void getOfferData(){
        database.collection("books")
                .document(bookID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        price = String.valueOf(document.get("price"));
                        priceET.setText(price);
                        title = String.valueOf(document.get("title"));
                        titleET.setText(title);
                        description = String.valueOf(document.get("description"));
                        descriptionET.setText(description);
                        progressDialog.dismiss();
                    }
                });
    }
    private void getProfilePhoto(){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(email + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToProfilePhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failure at downloading profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToProfilePhoto(byte[] bytes){
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        photoIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, photoIV.getWidth(),
                photoIV.getHeight(), false));
    }
    private void importImage(){
        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select Images"),
                StaticClass.PICK_SINGLE_IMAGE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StaticClass.PICK_SINGLE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = data.getData();
            if(uri != null){
                final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                ContentResolver resolver = getApplicationContext().getContentResolver();
                resolver.takePersistableUriPermission(uri, takeFlags);

                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(
                            getApplicationContext().getContentResolver(), uri);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "IO Exception when selecting a profile image",
                            Toast.LENGTH_LONG).show();
                }
                bookIV.setScaleType(ImageView.ScaleType.FIT_CENTER);
                bookIV.setImageBitmap(imageBitmap);
                imageEdited = true;
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_book_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.post){
            post();
        }
        return super.onOptionsItemSelected(item);
    }
    private void post(){
        if(!validData())
            return;
        DocumentReference bookReference = database.collection("books")
                .document(bookID);
        bookReference.update(bookMap());
        if(imageEdited) uploadBookPhoto(bookReference.getId());
        startActivity(new Intent(getApplicationContext(), MyBooksActivity.class));
    }
    private boolean validData(){
        editedPrice = priceET.getText().toString();
        if(price.isEmpty()){
            displayErrorTV(R.string.unspecified_price);
            return false;
        }
        editedTitle = titleET.getText().toString();
        if(title.isEmpty()){
            displayErrorTV(R.string.unspecified_title);
            return false;
        }
        editedDescription = descriptionET.getText().toString();
        if(description.isEmpty()){
            displayErrorTV(R.string.unspecified_description);
            return false;
        }
        return true;
    }
    private HashMap<String, Object> bookMap(){
        HashMap<String, Object> map = new HashMap<>();
        if(!price.equals(editedPrice)) map.put("price", editedPrice);
        if(!title.equals(editedTitle)) map.put("title", editedTitle);
        if(!description.equals(editedDescription)) map.put("description", editedDescription);
        return map;
    }
    private byte[] getPhotoData(){
        Bitmap bitmap = ((BitmapDrawable) bookIV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    private void uploadBookPhoto(String bookID){
        byte[] data = getPhotoData();
        storage.getReference().child(bookID)
                .putBytes(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), "Uploading photo failed", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void displayErrorTV(int resourceID) {
        errorTV.setText(resourceID);
        errorTV.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                errorTV.setVisibility(View.GONE);
            }
        }, 1500);
    }
    public void setActionBarTitle(String title){
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(
                Html.fromHtml("<font color=\"#ffffff\"> "+title+" </font>")
        );
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(EditBookActivity.this)
                .setTitle("Discard the Edit")
                .setMessage("Are you sure you want to discard this edit?")
                .setPositiveButton(
                        Html.fromHtml("<font color=\"#AA0000\"> Discard </font>")
                        , new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getApplicationContext(), MyBooksActivity.class));
                            }
                        })
                .setNegativeButton(
                        Html.fromHtml("<font color=\"#1976D2\"> Cancel </font>"),
                        null)
                .show();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
