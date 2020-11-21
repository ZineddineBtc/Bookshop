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
import com.example.bookshop.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {

    private List<User> usersList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private FirebaseStorage storage;

    public SearchUserAdapter(Context context, List<User> data) {
        this.mInflater = LayoutInflater.from(context);
        this.usersList = data;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.profile_search_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final User user = usersList.get(position);
        setUserPhoto(holder, user.getId());
        holder.nameTV.setText(user.getName());
        holder.cityTV.setText(user.getCity());
        holder.phoneTV.setText(user.getPhone());
        holder.userLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ProfileActivity.class)
                        .putExtra(StaticClass.PROFILE_ID, user.getId()));
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
    @Override
    public int getItemCount() {
        return usersList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout userLL;
        private ImageView photoIV;
        private TextView nameTV, phoneTV, cityTV;
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
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    User getItem(int id) {
        return usersList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
