package com.example.bookshop.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.example.bookshop.model.Book;
import com.example.bookshop.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class ProfileBookAdapter extends RecyclerView.Adapter<ProfileBookAdapter.ViewHolder> {

    private List<Book> booksList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private Bitmap profilePhotoBitmap;
    private User user;
    private FirebaseStorage storage;

    public ProfileBookAdapter(Context context, List<Book> data,
                              Bitmap profilePhotoBitmap, User user) {
        this.mInflater = LayoutInflater.from(context);
        this.booksList = data;
        this.context = context;
        this.profilePhotoBitmap = profilePhotoBitmap;
        this.user = user;
        this.storage = FirebaseStorage.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.profile_book_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Book book = booksList.get(position);
        getBookPhoto(holder, book);
        holder.phoneTV.setText(book.getPrice());
        holder.titleTV.setText(book.getTitle());
        holder.descriptionTV.setText(book.getDescription());
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

    @Override
    public int getItemCount() {
        return booksList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView photoIV, bookIV;
        private TextView nameTV, phoneTV, cityTV, priceTV, titleTV, descriptionTV;
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
            priceTV = itemView.findViewById(R.id.priceTV);
            titleTV = itemView.findViewById(R.id.titleTV);
            descriptionTV = itemView.findViewById(R.id.descriptionTV);
            bookIV = itemView.findViewById(R.id.bookIV);
        }
        private void setStaticViews(){
            photoIV.setImageBitmap(profilePhotoBitmap);
            nameTV.setText(user.getName());
            phoneTV.setText(user.getPhone());
            cityTV.setText(user.getCity());
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
