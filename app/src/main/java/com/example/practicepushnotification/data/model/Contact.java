package com.example.practicepushnotification.data.model;

import java.util.Comparator;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Contact extends RealmObject {



    @PrimaryKey
    private String id;
    private String name;
    private RealmList<String> phoneNumbers = new RealmList<>();

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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List <String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumber) {
        this.phoneNumbers.add(phoneNumber.get(0));
    }

    public static Comparator<Contact> ConNameComparator = new Comparator<Contact>() {
        @Override
        public int compare(Contact o1, Contact o2) {

            String ConName1 = o1.getName().toUpperCase();
            String ConName2 = o2.getName().toUpperCase();

            return ConName1.compareTo(ConName2);
        }
    };


    public void addPhoneNumber(String phoneNumber) {
        this.phoneNumbers.add(phoneNumber);
    }
}
