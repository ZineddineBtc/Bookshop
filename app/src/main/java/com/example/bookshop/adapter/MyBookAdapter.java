package com.example.bookshop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.example.bookshop.activity.core.EditBookActivity;
import com.example.bookshop.activity.core.fragment.ProfileFragment;
import com.example.bookshop.model.Book;
import com.example.bookshop.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyBookAdapter extends RecyclerView.Adapter<MyBookAdapter.ViewHolder> {

    private List<Book> booksList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private Bitmap profilePhotoBitmap;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String name, phone, city;

    public MyBookAdapter(Context context, List<Book> data) {
        this.mInflater = LayoutInflater.from(context);
        this.booksList = data;
        this.context = context;
        this.profilePhotoBitmap = ProfileFragment.profilePhotoBitmap;
        this.storage = FirebaseStorage.getInstance();
        this.database = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        this.name = sharedPreferences.getString(StaticClass.NAME, "no name");
        this.phone = sharedPreferences.getString(StaticClass.PHONE, "no phone");
        this.city = sharedPreferences.getString(StaticClass.CITY, "no city");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.my_book_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Book book = booksList.get(position);
        getBookPhoto(holder, book);
        holder.priceTV.setText(book.getPrice());
        holder.timeTV.setText(castTime(book.getTime()));
        holder.titleTV.setText(book.getTitle());
        holder.descriptionTV.setText(book.getDescription());
        holder.toggleIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(holder);
            }
        });
        holder.editTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, EditBookActivity.class)
                .putExtra(StaticClass.BOOK_ID, book.getId()));
            }
        });
        holder.deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { delete(book.getId(), position);
            }
        });
    }
    private void getBookPhoto(final ViewHolder holder, Book book){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(book.getId())
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.bookIV.setImageBitmap(Bitmap.createScaledBitmap(bmp,
                                holder.bookIV.getWidth(), holder.bookIV.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failed at getting book photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void toggle(ViewHolder holder){
        if(holder.toggledLL.getVisibility()==View.GONE){
            holder.toggleIV.setImageResource(R.drawable.ic_keyboard_arrow_right_dark_grey);
            holder.toggledLL.setVisibility(View.VISIBLE);
        }else{
            holder.toggledLL.setVisibility(View.GONE);
            holder.toggleIV.setImageResource(R.drawable.ic_keyboard_arrow_left_dark_grey);
        }
    }
    private void delete(final String bookID, final int index){
        new AlertDialog.Builder(context)
                .setTitle("Delete offer")
                .setMessage("Are you sure you want to delete this offer?")
                .setPositiveButton(
                        Html.fromHtml("<font color=\"#AA0000\"> Delete </font>")
                        , new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                database.collection("books")
                                        .document(bookID)
                                        .delete();
                                storage.getReference(bookID)
                                        .delete();
                                booksList.remove(index);
                                notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(
                        Html.fromHtml("<font color=\"#1976D2\"> Cancel </font>"),
                        null)
                .show();
    }
    @SuppressLint("SimpleDateFormat")
    private String castTime(long time){
        return new SimpleDateFormat("dd MMM. yyyy HH:mm").format(new Date(time));
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView photoIV, bookIV, toggleIV;
        private TextView nameTV, phoneTV, cityTV, priceTV, timeTV,
                         titleTV, descriptionTV, editTV, deleteTV;
        private LinearLayout toggledLL;
        private View itemView;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();
            setStaticViews();
            itemView.setOnClickListener(this);
        }
        private void findViewsByIds(){
            photoIV = itemView.findViewById(R.id.photoIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            phoneTV = itemView.findViewById(R.id.phoneTV);
            cityTV = itemView.findViewById(R.id.cityTV);
            editTV = itemView.findViewById(R.id.editTV);
            deleteTV = itemView.findViewById(R.id.deleteTV);
            toggledLL = itemView.findViewById(R.id.toggledLL);
            toggleIV = itemView.findViewById(R.id.toggleIV);
            priceTV = itemView.findViewById(R.id.priceTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            titleTV = itemView.findViewById(R.id.titleTV);
            descriptionTV = itemView.findViewById(R.id.descriptionTV);
            bookIV = itemView.findViewById(R.id.bookIV);
        }
        private void setStaticViews(){
            photoIV.setImageBitmap(profilePhotoBitmap);
            nameTV.setText(name);
            phoneTV.setText(phone);
            cityTV.setText(city);
        }
        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    Book getItem(int id) {
        return booksList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
