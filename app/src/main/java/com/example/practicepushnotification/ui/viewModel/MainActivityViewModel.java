package com.example.practicepushnotification.ui.viewModel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.practicepushnotification.data.model.Contact;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.realm.Realm;
import io.realm.exceptions.RealmMigrationNeededException;

public class MainActivityViewModel {

    private static final String TAG = MainActivityViewModel.class.getName();
    private static final String NAME_KEY = "Name";
    private static final String PHONE_KEY = "Phone";
    private static final int REQUEST_READ_PHONE_STATE = 1 ;
    private Context mContext;

    // private FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();
    //private DocumentReference contactRef = firebaseDatabase.document("phonebook/Contacts");


    public MainActivityViewModel(Context mContext) {
        this.mContext = mContext;
    }

    Cursor contactsCursor;

    List<Contact> storeFetchedContacts = new ArrayList<>();

    Map<String, Object> newContact = new HashMap<>();

    private Realm mRealm = Realm.getDefaultInstance();

    public Realm getmRealm() {
        return mRealm;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Contact> getContacts() {

        Contact realmContact;
        contactsCursor = mContext.getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);


        while (contactsCursor.moveToNext()) {
            String id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            String name = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));


            //Check if contacts with same id is present or not
            Optional<Contact> optionalContact = getContactByID(storeFetchedContacts, id);
            if (optionalContact.isPresent()) {
                realmContact = optionalContact.get();
                realmContact.addPhoneNumber(phoneNumber);

            } else {
                List<String> phoneNumbers = new ArrayList<>();
                phoneNumbers.add(phoneNumber);

                realmContact = new Contact();
                realmContact.setId(id);
                realmContact.setName(name);
                realmContact.setPhoneNumbers(phoneNumbers);
                realmContact.setBeingSaved(true);
                Log.d("===>", " ContactFetched: " + realmContact.getName());
                storeFetchedContacts.add(realmContact);
            }
        }

        storeinRealm(storeFetchedContacts);


        return storeFetchedContacts;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int countDeviceContacts() {

        int countContacts = 0;

        HashSet toStoreCount = new HashSet();
        Cursor contactsCursor = mContext.getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (contactsCursor.getCount() > 0) {
            while (contactsCursor.moveToNext()) {
                String id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

                toStoreCount.add(id);


            }
        }

        return toStoreCount.size();


    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private Optional<Contact> getContactByID(List<Contact> storeFetchedContacts, String id) {

        return storeFetchedContacts.stream().filter(o -> o.getId().equals(id)).findFirst();
    }


    public void storeinRealm(List<Contact> storeFetchedContacts) {

        try {

            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm mRealm) {

                    mRealm.insertOrUpdate(storeFetchedContacts);


//                    mRealm.where(Contact.class)
//                            .equalTo("isBeingSaved", false)
//                            .findAll()
//                            .deleteAllFromRealm();
//                    for (Contact contact : mRealm.where(Contact.class).findAll()) {
//                        contact.setBeingSaved(false);
//                    }


                }
            });
        } catch (RealmMigrationNeededException e) {
            Log.d("==>", "RealmExMigration" + e);

            //            mRealm = Realm.getInstance(config);
        }
    }


    public void writeinFirebase(FirebaseFirestore firebaseDatabase, String IMEINumber) {



        Log.d(TAG, "===> mRealm: " + mRealm);
        //Contact realmContact = new Contact();

        List<Contact> retrieveRealm = mRealm.copyFromRealm(mRealm.where(Contact.class).findAll());


        for (Contact itrContact : retrieveRealm) {

            newContact.put(NAME_KEY, itrContact.getName());
            newContact.put(PHONE_KEY, itrContact.getPhoneNumbers());


            Log.d("==>", "Retrived:" + itrContact.getName());

            firebaseDatabase.collection("phonebook").document(IMEINumber)
                    .collection("OnlyContacts").document(String.valueOf(itrContact.getId()))
                    .set(newContact).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Log.d("==>", "Saved in firestore");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Failed", e.toString());


                }
            });
        }


    }

    @SuppressLint("HardwareIds")
    public String getDeviceId() {

        String deviceId;


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

        } else {

            final TelephonyManager mTelephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);


            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId();
            } else {
                deviceId = Settings.Secure.getString(
                        mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
        }

        return deviceId;

    }
}
