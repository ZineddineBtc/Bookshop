package com.example.bookshop.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.example.bookshop.activity.core.fragment.ProfileFragment;
import com.example.bookshop.adapter.MyBookAdapter;
import com.example.bookshop.adapter.ProfileBookAdapter;
import com.example.bookshop.model.Book;
import com.example.bookshop.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class MyBooksActivity extends AppCompatActivity {

    private TextView noBooksTV;
    private RecyclerView booksRV;
    private MyBookAdapter adapter;
    private ArrayList<Book> booksList = new ArrayList<>();
    private FirebaseFirestore database;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);
        getInstances();
        setActionBarTitle("My Book Posts");
        findViewsByIds();
        setBooksRV();
        getBooks();
    }
    private void getInstances(){
        email = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE)
                .getString(StaticClass.EMAIL, "no email");
        database = FirebaseFirestore.getInstance();
    }
    private void findViewsByIds(){
        noBooksTV = findViewById(R.id.noBooksTV);
        booksRV = findViewById(R.id.booksRV);
    }
    private void setBooksRV(){
        adapter = new MyBookAdapter(MyBooksActivity.this, booksList);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        llm.setStackFromEnd(true);
        booksRV.setLayoutManager(llm);
        booksRV.setAdapter(adapter);
    }
    private void getBooks(){
        database.collection("books")
                .whereEqualTo("user", email)
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
                Log.i("INDEX",e.getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
    public void setActionBarTitle(String title){
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(
                Html.fromHtml("<font color=\"#ffffff\"> "+title+" </font>")
        );
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), CoreActivity.class)
        .putExtra(StaticClass.TO, StaticClass.PROFILE_FRAGMENT));
    }
    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return false;
    }
}
