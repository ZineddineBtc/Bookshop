package com.example.bookshop.activity.core.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookshop.R;
import com.example.bookshop.activity.core.AddBookActivity;
import com.example.bookshop.adapter.ProfileBookAdapter;
import com.example.bookshop.model.Book;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;


public class SearchFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private FloatingActionButton addBookFAB;
    private EditText searchET;
    private TextView noResultsTV;
    private RecyclerView booksRV;
    private ProfileBookAdapter adapter;
    private ArrayList<Book> booksList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_search, container, false);
        context = fragmentView.getContext();
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).hide();
        findViewsByIds();

        return fragmentView;
    }
    private void findViewsByIds(){
        searchET = fragmentView.findViewById(R.id.searchET);
        noResultsTV = fragmentView.findViewById(R.id.noResultsTV);
        booksRV = fragmentView.findViewById(R.id.booksRV);
        addBookFAB = fragmentView.findViewById(R.id.addBookFAB);
        addBookFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, AddBookActivity.class));
            }
        });
    }

}
