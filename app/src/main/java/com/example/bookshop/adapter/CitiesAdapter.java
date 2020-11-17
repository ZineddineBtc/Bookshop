package com.example.bookshop.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.example.bookshop.activity.SetProfileActivity;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.ViewHolder> {

    private List<String> citiesList, copyList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private String city;
    private DocumentReference document;
    private SharedPreferences.Editor editor;
    private String initializedIn;

    public CitiesAdapter(Context context, List<String> data, String city,
                         DocumentReference document, SharedPreferences.Editor editor,
                         String initializedIn) {
        this.mInflater = LayoutInflater.from(context);
        this.citiesList = data;
        copyList = new ArrayList<>(citiesList);
        this.context = context;
        this.city = city;
        this.document = document;
        this.editor = editor;
        this.initializedIn = initializedIn;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.city_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.cityTV.setText(citiesList.get(position));
        if(initializedIn.equals(StaticClass.PROFILE_FRAGMENT)) {
            if (citiesList.get(position).equals(city)) {
                holder.cityTV.setTextColor(context.getColor(R.color.special));
                holder.locationIV.setVisibility(View.VISIBLE);
            }else{
                holder.cityTV.setTextColor(context.getColor(R.color.black));
                holder.locationIV.setVisibility(View.GONE);
            }
            /*
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileFragment.shadeLL.setVisibility(View.GONE);
                    ProfileFragment.countriesLL.setVisibility(View.GONE);
                    ProfileFragment.countriesListShown = false;
                    String newCountry = countriesList.get(position);
                    ProfileFragment.countryTV.setText(newCountry);
                    document.update("country", newCountry);
                    editor.putString(StaticClass.COUNTRY, newCountry);
                    editor.apply();
                    Toast.makeText(context,
                            "Country updated",
                            Toast.LENGTH_SHORT).show();
                }
            });*/
        }else if(initializedIn.equals(StaticClass.SET_PROFILE_ACTIVITY)){
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetProfileActivity.shadeLL.setVisibility(View.GONE);
                    SetProfileActivity.citiesLL.setVisibility(View.GONE);
                    SetProfileActivity.cityTV.setText(citiesList.get(position));
                    SetProfileActivity.citiesLLShown = false;
                    SetProfileActivity.cityPicked = true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return citiesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RelativeLayout parentLayout;
        private ImageView locationIV;
        private TextView cityTV;

        ViewHolder(final View itemView) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parentLayout);
            locationIV = itemView.findViewById(R.id.locationIV);
            cityTV = itemView.findViewById(R.id.cityTV);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    String getItem(int id) {
        return citiesList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void filter(String queryText) {
        citiesList.clear();
        if(queryText.isEmpty()) {
            citiesList.addAll(copyList);
        }else{
            for(String text: copyList) {
                if(text.toLowerCase().contains(queryText.toLowerCase())) {
                    citiesList.add(text);
                }
            }
        }
        notifyDataSetChanged();
    }
}