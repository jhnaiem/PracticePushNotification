package com.example.practicepushnotification.data.model;

import androidx.annotation.NonNull;

import java.util.Comparator;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Contact extends RealmObject {



    private String id;
    private String name;
    @PrimaryKey
    private String phoneNumber;

    @Index
    private boolean isBeingSaved;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBeingSaved(boolean beingSaved) {
        isBeingSaved = beingSaved;
    }
//    public Contact(String name, String phoneNumber) {
//        this.name = name;
//        this.phoneNumber = phoneNumber;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static Comparator<Contact> ConNameComparator = new Comparator<Contact>() {
        @Override
        public int compare(Contact o1, Contact o2) {

            String ConName1 = o1.getName().toUpperCase();
            String ConName2 = o2.getName().toUpperCase();

            return ConName1.compareTo(ConName2);
        }
    };


}
