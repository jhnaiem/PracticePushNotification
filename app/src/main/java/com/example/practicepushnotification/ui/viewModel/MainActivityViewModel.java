package com.example.practicepushnotification.ui.viewModel;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.practicepushnotification.data.model.Contact;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;

public class MainActivityViewModel {

    private static final String TAG = MainActivityViewModel.class.getName();
    private static final String NAME_KEY = "Name";
    private static final String PHONE_KEY = "Phone";
    private Context mContext;

    private FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();
    //private DocumentReference contactRef = firebaseDatabase.document("phonebook/Contacts");


    public MainActivityViewModel(Context mContext) {
        this.mContext = mContext;
    }

    List<Contact> storeFetchedContacts = new ArrayList<>();

    Map<String, Object> newContact = new HashMap<>();

    private Realm mRealm = Realm.getDefaultInstance();

    public List<Contact> getContacts() {

        try {

            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm mRealm) {
                    Contact realmContact = new Contact();
                    Cursor contactsCursor = mContext.getContentResolver()
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

                    while (contactsCursor.moveToNext()) {
                        String id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        String name = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        realmContact = new Contact();
                        realmContact.setId(id);
                        realmContact.setName(name);
                        realmContact.setPhoneNumber(phoneNumber);
                        realmContact.setBeingSaved(true);
                        Log.d("===>", " ContactFetched: " + realmContact.getName());
                        storeFetchedContacts.add(realmContact);
                        mRealm.insertOrUpdate(realmContact);
                    }

                    mRealm.where(Contact.class)
                            .equalTo("isBeingSaved", false)
                            .findAll()
                            .deleteAllFromRealm();
                    for (Contact contact : mRealm.where(Contact.class).findAll()) {
                        contact.setBeingSaved(false);
                    }


                }
            });
        } catch (RealmMigrationNeededException e) {
            Log.d("==>", "RealmExMigration" + e);

            //            mRealm = Realm.getInstance(config);
        }


        return storeFetchedContacts;
    }


    public void writeinFirebase() {

        Log.d(TAG, "===> mRealm: " + mRealm);
        //Contact realmContact = new Contact();

        List<Contact> retrieveRealm = mRealm.copyFromRealm(mRealm.where(Contact.class).findAll());

        int i = 0;
        for (Contact itrContact : retrieveRealm) {

            newContact.put(NAME_KEY, itrContact.getName());
            newContact.put(PHONE_KEY, itrContact.getPhoneNumber());

            i++;

            Log.d("==>", "Retrived:" + itrContact.getName());

            firebaseDatabase.collection("phonebook")
                    .document(String.valueOf(itrContact.getId()))
                    .set(newContact).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Log.d("==>", "Saved in frestore");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Failed", e.toString());


                }
            });
        }


        //newContact.put(NAME_KEY,)


    }

}
