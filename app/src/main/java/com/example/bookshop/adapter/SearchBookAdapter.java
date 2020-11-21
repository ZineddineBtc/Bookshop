package com.example.bookshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.example.bookshop.activity.core.ProfileActivity;
import com.example.bookshop.model.Book;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.ViewHolder> {

    private List<Book> booksList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private FirebaseStorage storage;

    public SearchBookAdapter(Context context, List<Book> data) {
        this.mInflater = LayoutInflater.from(context);
        this.booksList = data;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.profile_book_or_search_book_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Book book = booksList.get(position);
        setBookPhoto(holder, book);
        setUserPhoto(holder, book.getUser().getId());
        holder.nameTV.setText(book.getUser().getName());
        holder.cityTV.setText(book.getUser().getCity());
        holder.phoneTV.setText(book.getUser().getPhone());
        holder.priceTV.setText(book.getPrice());
        holder.timeTV.setText(castTime(book.getTime()));
        holder.titleTV.setText(book.getTitle());
        holder.descriptionTV.setText(book.getDescription());
        holder.userLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ProfileActivity.class)
                .putExtra(StaticClass.PROFILE_ID, book.getUser().getId()));
            }
        });
    }
    private void setUserPhoto(final ViewHolder holder, String userID){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(userID + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap btm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.photoIV.setImageBitmap(Bitmap.createScaledBitmap(btm,
                        holder.photoIV.getWidth(), holder.photoIV.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failure at downloading profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBookPhoto(final ViewHolder holder, Book book){
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
    private String castTime(long time){
        return new SimpleDateFormat("dd MMM. yyyy HH:mm").format(new Date(time));
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout userLL;
        private ImageView photoIV, bookIV;
        private TextView nameTV, phoneTV, cityTV, priceTV, timeTV, titleTV, descriptionTV;
        private View itemView;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        private void findViewsByIds(){
            userLL = itemView.findViewById(R.id.userLL);
            photoIV = itemView.findViewById(R.id.photoIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            phoneTV = itemView.findViewById(R.id.phoneTV);
            cityTV = itemView.findViewById(R.id.cityTV);
            priceTV = itemView.findViewById(R.id.priceTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            titleTV = itemView.findViewById(R.id.titleTV);
            descriptionTV = itemView.findViewById(R.id.descriptionTV);
            bookIV = itemView.findViewById(R.id.bookIV);
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
