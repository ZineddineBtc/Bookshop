package com.example.bookshop.activity.entry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookshop.activity.TermsActivity;
import com.example.bookshop.activity.core.CoreActivity;
import com.example.bookshop.adapter.CitiesAdapter;
import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetProfileActivity extends AppCompatActivity {

    private ImageView photoIV;
    private TextView errorTV;
    private EditText nameET, phoneET;
    private RecyclerView citiesRV;
    private CitiesAdapter citiesAdapter;
    private ProgressDialog progressDialog;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String name, email, city, phone;
    private boolean imagePicked;
    public static LinearLayout shadeLL, citiesLL;
    public static SearchView searchCitySV;
    public static TextView cityTV;
    public static boolean citiesLLShown, cityPicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();
        initializeInstances();
        findViewsByIds();
        checkBuildVersion();
    }
    private void initializeInstances(){
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
    }
    public void checkBuildVersion(){
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyHavePermission()) {
                requestForSpecificPermission();
            }
        }
    }
    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET},
                101);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // not granted
                moveTaskToBack(true);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void findViewsByIds(){
        errorTV = findViewById(R.id.errorTV);
        photoIV = findViewById(R.id.photoIV);
        nameET = findViewById(R.id.nameET);
        phoneET = findViewById(R.id.phoneET);
        cityTV = findViewById(R.id.cityTV);
        cityTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCity();
            }
        });
        Button finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishRegister();
            }
        });
        shadeLL = findViewById(R.id.shadeLL);
        citiesLL = findViewById(R.id.citiesLL);
        searchCitySV = findViewById(R.id.searchCitySV);
        searchCitySV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                citiesAdapter.filter(newText);
                return false;
            }
        });
        citiesRV = findViewById(R.id.citiesRV);
        setCitiesRV();
    }
    private void setCitiesRV(){
        citiesAdapter = new CitiesAdapter(getApplicationContext(),
                new ArrayList<>(Arrays.asList(StaticClass.countries)), "",
                database.collection("users").document(email), editor,
                StaticClass.SET_PROFILE_ACTIVITY);
        citiesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        citiesRV.setAdapter(citiesAdapter);
    }
    public void importImage(View view){
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
                photoIV.setImageBitmap(imageBitmap);
                imagePicked = true;
            }
        }
    }
    private void finishRegister(){
        if(!imagePicked){
            displayErrorTV(R.string.no_photo_selected);
            return;
        }
        name = nameET.getText().toString().trim();
        if(StaticClass.containsDigit(name) || name.length()<=1){
            displayErrorTV(R.string.invalid_name);
            return;
        }
        phone = phoneET.getText().toString().trim();
        if(phone.length()<8){
            displayErrorTV(R.string.invalid_phone);
            return;
        }
        city = cityTV.getText().toString().trim();
        if(!cityPicked){
            displayErrorTV(R.string.unspecified_city);
            return;
        }
        progressDialog.show();
        progressDialog.setMessage("Setting up profile...");
        uploadPhoto();
    }
    private byte[] getPhotoData(){
        Bitmap bitmap = ((BitmapDrawable) photoIV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    private void uploadPhoto(){
        byte[] data = getPhotoData();
        storage.getReference().child(email+StaticClass.PROFILE_PHOTO)
                .putBytes(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Uploading photo failed", Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                writeSharedPreferences();
            }
        });
    }
    private void setCity(){
        shadeLL.setVisibility(View.VISIBLE);
        citiesLL.setVisibility(View.VISIBLE);
        citiesLLShown = true;
    }
    private void writeSharedPreferences(){
        editor = sharedPreferences.edit();
        editor.putString(StaticClass.EMAIL, email);
        editor.putString(StaticClass.NAME, name);
        editor.putString(StaticClass.CITY, city);
        editor.putString(StaticClass.PHONE, phone);
        editor.apply();
        writeOnlineDatabase();
    }
    private void writeOnlineDatabase(){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("name", name);
        userReference.put("city", city);
        userReference.put("phone", phone);
        database.collection("users")
                .document(email)
                .set(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(getApplicationContext(), CoreActivity.class));
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Error writing user",
                                Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }
    public void toTermsAndConditions(View view) {
        startActivity(new Intent(getApplicationContext(), TermsActivity.class));
    }
    public void displayErrorTV(int resourceID) {
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
    @Override
    public void onBackPressed() {
        if(citiesLLShown){
            shadeLL.setVisibility(View.GONE);
            citiesLL.setVisibility(View.GONE);
            citiesLLShown = false;
            return;
        }
        moveTaskToBack(true);
    }
}
