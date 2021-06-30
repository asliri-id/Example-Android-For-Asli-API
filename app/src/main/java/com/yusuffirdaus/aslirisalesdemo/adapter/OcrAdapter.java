package com.yusuffirdaus.aslirisalesdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yusuffirdaus.aslirisalesdemo.model.DataCode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import  com.yusuffirdaus.aslirisalesdemo.R;

public class OcrAdapter extends RecyclerView.Adapter<com.yusuffirdaus.aslirisalesdemo.adapter.OcrAdapter.Holderdata> {
    private Context ctx;
    private List<DataCode> listContact;


    public OcrAdapter(Context ctx, List<DataCode> listKeluarga) {
        this.ctx = ctx;
        this.listContact = listKeluarga;

    }

    @NonNull
    @Override
    public Holderdata onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_ocr, parent, false);
        Holderdata holder = new Holderdata(layout);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holderdata holder, int position) {


        DataCode dm = listContact.get(position);
        String inisial = (dm.getCode()).substring(0, 1).toUpperCase() + (dm.getCode()).substring(1).toLowerCase();
        holder.tInisial.setText(inisial);
        holder.tValue.setText(String.valueOf(dm.getName()));
    }

    @Override
    public int getItemCount() {

        int ukuran = 0;
        if (listContact != null) {
            ukuran = listContact.size();
        } else {
            ukuran = 0;
        }
        return ukuran;
    }

    public class Holderdata extends RecyclerView.ViewHolder {
        TextView tInisial, tValue;


        public Holderdata(@NonNull View itemView) {
            super(itemView);


            tInisial = itemView.findViewById(R.id.tInisial);
            tValue = itemView.findViewById(R.id.tValue);

        }
    }


}
