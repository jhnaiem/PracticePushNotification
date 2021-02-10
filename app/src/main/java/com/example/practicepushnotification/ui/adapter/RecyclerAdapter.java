package com.example.practicepushnotification.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicepushnotification.R;
import com.example.practicepushnotification.data.model.Contact;

import java.util.List;
import java.util.zip.Inflater;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.contactViewHolder> {


    List<Contact> contactList;

    private Context mContext;

    public RecyclerAdapter(List<Contact> contactList, Context mContext) {
        this.contactList = contactList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public contactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_list,parent,false);
        return new contactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull contactViewHolder holder, int position) {

        holder.name.setText(contactList.get(position).getName());
        holder.name.setText(contactList.get(position).getPhoneNumber());

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    public class contactViewHolder extends RecyclerView.ViewHolder {

        TextView name ,phonenumber;

        public contactViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textName);
            phonenumber = itemView.findViewById(R.id.textNumber);


        }
    }
}
