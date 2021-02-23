package com.example.practicepushnotification.ui.view;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.practicepushnotification.R;
import com.example.practicepushnotification.data.model.Contact;
import com.example.practicepushnotification.ui.adapter.RecyclerAdapter;
import com.example.practicepushnotification.ui.viewModel.MainActivityViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private RecyclerView recyclerView;
    private List<Contact> contactList;

    private RecyclerAdapter contactAdapter;
    private MainActivityViewModel mainActivityViewModel;

    final AtomicBoolean isFirstListener = new AtomicBoolean(true);

    private FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();
    private CollectionReference phoneBookRef = firebaseDatabase.collection("phonebook");
    //    private DocumentReference documentReference = firebaseDatabase.document("phonebook");
    private Realm mRealm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration
                .Builder()
                .name("contacts.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);


        contactList = new ArrayList<>();
        contactAdapter = new RecyclerAdapter(contactList, this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(contactAdapter);

        mainActivityViewModel = new MainActivityViewModel(this);
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(new PermissionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        if (permissionGrantedResponse.getPermissionName().equals(Manifest.permission.READ_CONTACTS)) {
                            if (mainActivityViewModel.getmRealm().isEmpty()) {
                                contactList.addAll(mainActivityViewModel.getContacts());
                                Log.d("===>", " ContactPassed: " + contactList.size());
                                Collections.sort(contactList, Contact.ConNameComparator);
                            } else {
                                mRealm = mainActivityViewModel.getmRealm();
                                List<Contact> retrieveRealm = mRealm.copyFromRealm(mRealm.where(Contact.class).findAll());
                                contactList.addAll(retrieveRealm);
                            }

                            contactAdapter.notifyDataSetChanged();

                            phoneBookRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (queryDocumentSnapshots.isEmpty()){
                                        mainActivityViewModel.writeinFirebase(firebaseDatabase);
                                        Toast.makeText(MainActivity.this,"Let's populate firestore",Toast.LENGTH_LONG).show();

                                    }
                                }
                            });



                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        Toast.makeText(MainActivity.this, "Permission should be granted", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError dexterError) {
                dexterError.name();
            }
        }).check();


    }

    @Override
    protected void onStart() {
        super.onStart();



        phoneBookRef.orderBy("Name").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                List<DocumentSnapshot> docCount = value.getDocuments();

                if (isFirstListener.get()) {
                    isFirstListener.set(false);

                    return;
                }
                if (error != null) {
                    return;
                }
                Contact storeContact;

                for (DocumentChange changeItr : value.getDocumentChanges()) {


                    // Log.d(TAG, "onEvent: " + changeItr.);
                    DocumentSnapshot documentSnapshot = changeItr.getDocument();

                    String id = documentSnapshot.getId();
                    int oldIndex = changeItr.getOldIndex();
                    int newIndex = changeItr.getNewIndex();
                    switch (changeItr.getType()) {

                        case ADDED:
                            if(mRealm.where(Contact.class).findAll().size()<docCount.size()) {
                                storeContact = new Contact();
                                storeContact.setId(id);
                                Log.d("Change", "Name :" + documentSnapshot.getString("Name"));
                                storeContact.setName(documentSnapshot.getString("Name"));
                                storeContact.setName(documentSnapshot.getString("Number"));

                                contactList.add(newIndex, storeContact);
                                try {
                                    Collections.sort(contactList, Contact.ConNameComparator);

                                } catch (Exception e) {
                                    Log.d(TAG, "Error in comparator");
                                }
                                contactAdapter.notifyDataSetChanged();
                            }
                            break;

                        case MODIFIED:
                            storeContact = new Contact();
                            storeContact.setId(id);
                            storeContact.setName(documentSnapshot.getString("Name"));

                            Object phone = documentSnapshot.get("Phone");
                            Log.d(TAG, "phone: " + phone.getClass());
                            storeContact.setPhoneNumbers((List<String>) documentSnapshot.get("Phone"));

                            Log.d("==>", "Phone: " + documentSnapshot.get("Phone").toString());

                            updateInRealm(storeContact);
                            contactList.set(newIndex + 1, storeContact);
                            // Collections.sort(contactList, Contact.ConNameComparator);
                            contactAdapter.notifyItemChanged(newIndex + 1);
                            break;

                        case REMOVED:

                            break;
                    }
                }

            }

            private void initRealm(){
                if(mRealm == null || mRealm.isClosed()){

                    RealmConfiguration realmConfiguration = new RealmConfiguration
                            .Builder()
                            .name("contacts.realm")
                            .deleteRealmIfMigrationNeeded()
                            .build();
                    mRealm = Realm.getInstance(realmConfiguration);
                }
            }

            private void updateInRealm(Contact storeContact) {

                try {
                    initRealm();
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            Contact contact = mRealm.where(Contact.class).equalTo("id", storeContact.getId()).findFirst();
                            if (contact != null){

                                contact.setBeingSaved(true);
                                contact.setName(storeContact.getName());
                                Log.d(TAG,"Real Number: "+ storeContact.getPhoneNumbers() );
                                contact.setPhoneNumbers(storeContact.getPhoneNumbers());
                            }


                        }
                        List<Contact> retrieveRealm = mRealm.copyFromRealm(mRealm.where(Contact.class).findAll());

                    });

                } catch (Exception e) {
                    Log.d("==>", "Update Exception" + e);


                }


            }
        });



    }
}