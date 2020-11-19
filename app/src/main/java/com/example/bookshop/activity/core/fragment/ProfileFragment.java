package com.example.bookshop.activity.core.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.example.bookshop.activity.entry.LoginActivity;
import com.example.bookshop.adapter.CitiesAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

public class ProfileFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private ImageView photoIV, editNameIV, editPhoneIV;
    private TextView nameTV, phoneTV, emailTV, signOutTV, errorTV;
    private EditText nameET, phoneET;
    private SearchView searchCitySV;
    private RecyclerView citiesRV;
    private CitiesAdapter citiesAdapter;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String name, phone, city, email;
    private boolean nameETShown, phoneETShown, isCitiesRVSet;
    private byte[] profilePhotoData;
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout shadeLL, citiesLL;
    public static boolean citiesListShown;
    public static TextView cityTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        context = Objects.requireNonNull(getActivity()).getApplicationContext();
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
        getInstances();
        findViewsByIds();
        setUserData();
        getPhoto();
        return fragmentView;
    }
    private void getInstances(){
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
        editor = sharedPreferences.edit();
    }
    private void findViewsByIds(){
        photoIV = fragmentView.findViewById(R.id.photoIV);
        nameTV = fragmentView.findViewById(R.id.nameTV);
        nameET = fragmentView.findViewById(R.id.nameET);
        phoneTV = fragmentView.findViewById(R.id.phoneTV);
        phoneET = fragmentView.findViewById(R.id.phoneET);
        editNameIV = fragmentView.findViewById(R.id.editNameIV);
        editPhoneIV = fragmentView.findViewById(R.id.editPhoneIV);
        cityTV = fragmentView.findViewById(R.id.cityLL);
        emailTV = fragmentView.findViewById(R.id.emailTV);
        signOutTV = fragmentView.findViewById(R.id.signOutTV);
        errorTV = fragmentView.findViewById(R.id.errorTV);
        shadeLL = fragmentView.findViewById(R.id.shadeLL);
        citiesLL = fragmentView.findViewById(R.id.citiesLL);
        searchCitySV = fragmentView.findViewById(R.id.searchCitySV);
        citiesRV = fragmentView.findViewById(R.id.citiesRV);
    }
    private void setUserData(){
        name = sharedPreferences.getString(StaticClass.NAME, "no name");
        nameTV.setText(name);
        nameET.setText(name);
        phone = sharedPreferences.getString(StaticClass.PHONE, "no phone");
        phoneTV.setText(phone);
        phoneET.setText(phone);
        city = sharedPreferences.getString(StaticClass.CITY, "no city");
        cityTV.setText(city);
        emailTV.setText(email);
        setListeners();
    }
    private void setListeners(){
        photoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importImage();
            }
        });
        editNameIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editName();
            }
        });
        editPhoneIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPhone();
            }
        });
        cityTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCity();
            }
        });
        signOutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySignOutDialog();
            }
        });
    }
    private void getPhoto(){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(email + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToPhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failure at downloading profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToPhoto(byte[] bytes){
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
                Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = data.getData();
            if(uri != null){
                final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                ContentResolver resolver = context.getContentResolver();
                resolver.takePersistableUriPermission(uri, takeFlags);

                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(
                            context.getContentResolver(), uri);
                } catch (IOException e) {
                    Toast.makeText(context, "IO Exception when selecting a profile image",
                            Toast.LENGTH_LONG).show();
                }
                photoIV.setImageBitmap(imageBitmap);
                changePhoto();

            }
        }
    }
    private byte[] getProfilePhotoData(){
        Bitmap bitmap = ((BitmapDrawable) photoIV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    private void changePhoto(){
        profilePhotoData = getProfilePhotoData();
        deletePhoto();
    }
    private void deletePhoto(){
        storage.getReference().child(email+StaticClass.PROFILE_PHOTO)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                uploadPhoto();
            }
        });
    }
    private void uploadPhoto(){
        storage.getReference().child(email+StaticClass.PROFILE_PHOTO)
                .putBytes(profilePhotoData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(context, "Uploaded!", Toast.LENGTH_LONG).show();
                    }
        });
    }
    private void editName(){
        if(phoneETShown){
            Toast.makeText(context, "Pending edit", Toast.LENGTH_LONG).show();
            return;
        }
        if(nameETShown){
            String newName = nameET.getText().toString();
            if(!StaticClass.containsDigit(newName) && newName.length()>2){
                if(!name.equals(newName)){
                    writeName(newName);
                }
            }else{
                displayErrorTV(R.string.invalid_name);
            }
        }else{
            toggleName();
        }
    }
    private void toggleName(){
        nameET.setVisibility(!nameETShown ? View.VISIBLE : View.GONE);
        nameTV.setVisibility(nameETShown ? View.VISIBLE : View.GONE);
        editNameIV.setImageDrawable(nameETShown ?
                context.getDrawable(R.drawable.ic_edit) :
                context.getDrawable(R.drawable.ic_check));
        if(!nameETShown){
            nameET.requestFocus();
        }
        nameETShown = !nameETShown;
    }
    private void writeName(final String name){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("name", name);
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideKeyboard();
                        editor.putString(StaticClass.NAME, name);
                        editor.apply();
                        toggleName();
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "Name updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing name",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void editPhone(){
        if(nameETShown){
            Toast.makeText(context, "Pending edit", Toast.LENGTH_LONG).show();
            return;
        }
        if(phoneETShown){
            String newPhone = phoneET.getText().toString();
            if(newPhone.length()>8){
                if(!phone.equals(newPhone)){
                    writePhone(newPhone);
                }
            }else{
                displayErrorTV(R.string.invalid_phone);
            }
        }else{
            togglePhone();
        }
    }
    private void togglePhone(){
        phoneET.setVisibility(!phoneETShown ? View.VISIBLE : View.GONE);
        phoneTV.setVisibility(phoneETShown ? View.VISIBLE : View.GONE);
        editPhoneIV.setImageDrawable(phoneETShown ?
                context.getDrawable(R.drawable.ic_edit) :
                context.getDrawable(R.drawable.ic_check));
        if(!phoneETShown){
            phoneET.requestFocus();
        }
        phoneETShown = !phoneETShown;
    }
    private void writePhone(final String phone){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("phone", phone);
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideKeyboard();
                        editor.putString(StaticClass.PHONE, phone);
                        editor.apply();
                        togglePhone();
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "Phone updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing phone",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void editCity(){
        if(nameETShown || phoneETShown){
            Toast.makeText(context, "Pending edit", Toast.LENGTH_LONG).show();
            return;
        }
        citiesLL.setVisibility(View.VISIBLE);
        shadeLL.setVisibility(View.VISIBLE);
        citiesListShown = true;
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
        if(!isCitiesRVSet) setCitiesRV();
    }
    private void setCitiesRV(){
        citiesAdapter = new CitiesAdapter(context,
                new ArrayList<>(Arrays.asList(StaticClass.countries)), city,
                database.collection("users").document(email), editor,
                StaticClass.PROFILE_FRAGMENT);
        citiesRV.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        citiesRV.setAdapter(citiesAdapter);
        isCitiesRVSet = true;
    }
    private void displaySignOutDialog(){
        try {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Sign out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton(
                            Html.fromHtml("<font color=\"#FF0000\"> Sign out </font>")
                            , new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    signOut();
                                }
                            })
                    .setNegativeButton(
                            Html.fromHtml("<font color=\"#1976D2\"> Cancel </font>"),
                            null)
                    .show();
        }catch (NullPointerException e){
            Toast.makeText(context, "Failed at showing AlertDialog", Toast.LENGTH_SHORT).show();
            signOut();
        }
    }
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(fragmentView.getContext(), LoginActivity.class));
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
    private void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager)
                fragmentView.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}
