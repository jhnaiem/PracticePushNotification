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

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.contactViewHolder> {

    public List<Contact> contactList;

    private Context mContext;

    public RecyclerAdapter(List<Contact> contactList, Context mContext) {
        this.contactList = contactList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public contactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_list, parent, false);
        return new contactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull contactViewHolder holder, int position) {
        holder.name.setText(contactList.get(position).getName());
        int sizePhone = contactList.get(position).getPhoneNumbers().size();
        StringBuilder stringBuilder = new StringBuilder();
        while (sizePhone !=0){

            stringBuilder.append(contactList.get(position).getPhoneNumbers().get(sizePhone-1));

            sizePhone--;
            if (sizePhone>0){
                stringBuilder.append("\n");
            }
        }
        holder.phoneNumber.setText(stringBuilder);

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    public class contactViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView phoneNumber;

        public contactViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textName);
            phoneNumber = itemView.findViewById(R.id.textNumber);


        }
    }
}
