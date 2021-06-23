package com.example.practicepushnotification.ui.adapter;

import android.app.Activity;
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

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class ContactListAdapter extends RealmRecyclerViewAdapter<Contact,ContactListAdapter.contactViewHolder>  {

    private Context mContext;
    private Realm mRealm;
    List<Contact> contactList;


    public ContactListAdapter(RealmResults<Contact> contacts, Activity context) {
        super(contacts, true,true);

        this.contactList = contacts;
        this.mContext = context;
        this.mRealm = Realm.getDefaultInstance();

    }

    @NonNull
    @Override
    public contactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new contactViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull contactViewHolder holder, int position) {

        holder.name.setText(contactList.get(position).getName());
        holder.phoneNumber.setText(contactList.get(position).getPhoneNumbers().get(0));
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
