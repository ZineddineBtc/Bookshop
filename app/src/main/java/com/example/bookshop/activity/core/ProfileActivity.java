package com.example.bookshop.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.example.bookshop.adapter.ProfileBookAdapter;
import com.example.bookshop.model.Book;
import com.example.bookshop.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ImageView photoIV;
    private TextView nameTV, cityTV, phoneTV;
    private RecyclerView booksRV;
    private ProfileBookAdapter adapter;
    private ArrayList<Book> booksList = new ArrayList<>();
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String profileID, name, city, phone;
    private Bitmap profilePhotoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getInstances();
        findViewsByIds();
        getProfilePhoto();
        setProfileData();
        setBooksRV();
        getBooks();
    }
    private void getInstances(){
        profileID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        nameTV = findViewById(R.id.nameTV);
        cityTV = findViewById(R.id.cityTV);
        cityTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCityInMap();
            }
        });
        phoneTV = findViewById(R.id.phoneTV);
        phoneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhoneNumber();
            }
        });
        booksRV = findViewById(R.id.booksRV);
    }
    private void getProfilePhoto(){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(profileID + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToProfilePhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failed at getting profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToProfilePhoto(byte[] bytes){
        profilePhotoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        photoIV.setImageBitmap(Bitmap.createScaledBitmap(profilePhotoBitmap, photoIV.getWidth(),
                photoIV.getHeight(), false));
    }
    private void setProfileData(){
        database.collection("users")
                .document(profileID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            name = String.valueOf(document.get("name"));
                            nameTV.setText(name);
                            setActionBarTitle(name);
                            phone = String.valueOf(document.get("phone"));
                            phoneTV.setText(phone);
                            city = String.valueOf(document.get("city"));
                            cityTV.setText(city);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at setting profile date", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void setBooksRV(){
        adapter = new ProfileBookAdapter(getApplicationContext(), booksList,
                profilePhotoBitmap,
                new User(name, phone, city));
        booksRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        booksRV.setAdapter(adapter);
    }
    private void getBooks(){
        database.collection("books")
                .whereEqualTo("user", profileID)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot document: queryDocumentSnapshots.getDocuments()){
                            addBookToList(document);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at getting profile books", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void addBookToList(DocumentSnapshot document){
        Book book = new Book();
        book.setId(document.getId());
        book.setPrice(String.valueOf(document.get("price")));
        book.setTitle(String.valueOf(document.get("title")));
        book.setDescription(String.valueOf(document.get("description")));
        book.setTime((Long) document.get("time"));
        booksList.add(book);
        adapter.notifyDataSetChanged();
    }
    private void callPhoneNumber(){
        String phone_no = phoneTV.getText().toString()
                .replaceAll("-", "");
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phone_no));
        if (checkSelfPermission(Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }
    private void openCityInMap(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(
                Uri.parse("https://www.google.com/maps/search/" +
                        cityTV.getText().toString()));
        startActivity(browserIntent);
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
        startActivity(new Intent(getApplicationContext(), CoreActivity.class));
    }
    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return false;
    }
}
