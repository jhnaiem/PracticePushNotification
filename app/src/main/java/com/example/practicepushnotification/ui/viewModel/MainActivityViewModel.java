package com.example.practicepushnotification.ui.viewModel;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.practicepushnotification.data.model.Contact;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.exceptions.RealmMigrationNeededException;

public class MainActivityViewModel {


    private Context mContext;

    public MainActivityViewModel(Context mContext) {
        this.mContext = mContext;
    }

    List<Contact>  storeFetchedContacts = new ArrayList<>();

    private Realm mRealm = null;

    public List<Contact> getContacts() {

        try {
            Realm.init(mContext);
            mRealm = Realm.getDefaultInstance();
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm mRealm) {
                    Contact realmContact  = new Contact();
                    Cursor contactsCursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

                    while (contactsCursor.moveToNext()){

                        String id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        String name = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        realmContact = new Contact();
                        //realmContact.setId(id);
                        realmContact.setName(name);
                        realmContact.setPhoneNumber(phoneNumber);
                        realmContact.setBeingSaved(true);
                        Log.d("===>", " ContactFetched: " +realmContact.getName());
                        storeFetchedContacts.add(realmContact);
                        mRealm.insertOrUpdate(realmContact);
                    }
                    mRealm.where(Contact.class)
                            .equalTo("isBeingSaved",false)
                            .findAll()
                            .deleteAllFromRealm();
                    for (Contact contact: mRealm.where(Contact.class).findAll()){
                        realmContact.setBeingSaved(false);
                    }



                }
            });
        }catch (RealmMigrationNeededException e){

            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            mRealm = Realm.getInstance(config);
        } finally {
            if (mRealm!=null){
                mRealm.close();
            }
        }



        return storeFetchedContacts;
    }
}
