package com.example.practicepushnotification.ui.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.practicepushnotification.R;
import com.example.practicepushnotification.data.model.Contact;
import com.example.practicepushnotification.ui.adapter.RecyclerAdapter;
import com.example.practicepushnotification.ui.viewModel.MainActivityViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
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
    private static final int REQUEST_CODE = 101;
    private RecyclerView recyclerView;
    private List<Contact> contactList;

    private RecyclerAdapter contactAdapter;
    private MainActivityViewModel mainActivityViewModel;

    final AtomicBoolean isFirstListener = new AtomicBoolean(true);

    private FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();
    private CollectionReference phoneBookRef = firebaseDatabase.collection("phonebook");
    //    private DocumentReference documentReference = firebaseDatabase.document("phonebook");
    private Realm mRealm = null;
    private String IMEINumber;

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

        getIDpermission();


//        FirebaseApp.initializeApp(this);
//        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(){
//
//        }
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.e("mNotification", "Refreshed token: " + token);
            }
        });


        mainActivityViewModel = new MainActivityViewModel(this);
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(new PermissionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {


                        if (permissionGrantedResponse.getPermissionName().equals(Manifest.permission.READ_CONTACTS)) {

                            Log.d(TAG, "Check" + mainActivityViewModel.getmRealm().isEmpty());

                            mRealm = mainActivityViewModel.getmRealm();
                            List<Contact> retrieveRealm = mRealm.copyFromRealm(mRealm.where(Contact.class).findAll());

                            int size = retrieveRealm.size();


                            //If realmDB is empty then create new one for the first time for a device


                            if (mainActivityViewModel.getmRealm().isEmpty()) {
                                contactList.addAll(mainActivityViewModel.getContacts());
                                Log.d("===>", " ContactPassed: " + contactList.size());
                                Collections.sort(contactList, Contact.ConNameComparator);
                                mainActivityViewModel.writeinFirebase(firebaseDatabase, IMEINumber);
                                Toast.makeText(MainActivity.this, "Let's populate firestore", Toast.LENGTH_LONG).show();

                            } else if (mainActivityViewModel.countDeviceContacts() > size) {

                                contactList.addAll(mainActivityViewModel.getContacts());
                                Collections.sort(contactList, Contact.ConNameComparator);

                                //call write to fire here
                                mainActivityViewModel.writeinFirebase(firebaseDatabase, IMEINumber);
                                Toast.makeText(MainActivity.this, "Let's populate firestore", Toast.LENGTH_LONG).show();

                            } else {

                                contactList.addAll(retrieveRealm);
                                Collections.sort(contactList, Contact.ConNameComparator);

                            }

                            contactAdapter.notifyDataSetChanged();

//                            //prevent from overwrite to firestore???????///////WRONGGGGGG
//                            phoneBookRef.document(mainActivityViewModel.getDeviceId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                                    if (task.isSuccessful()) {
//                                        DocumentSnapshot document = task.getResult();
//                                        if (document == null && !document.exists()) {
//
//                                        } else {
//                                            Log.d(TAG, "Document exists");
//                                        }
//                                    } else {
//                                        Log.d(TAG, "get failed with ", task.getException());
//                                    }
//
//                                }
//                            });
//                        addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                @Override
//                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                    if (queryDocumentSnapshots.isEmpty()) {
//                                        mainActivityViewModel.writeinFirebase(firebaseDatabase);
//                                        Toast.makeText(MainActivity.this, "Let's populate firestore", Toast.LENGTH_LONG).show();
//
//                                    }
//                                }
//                            });


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


    //Get IMEI number
    @SuppressLint("HardwareIds")
    private void getIDpermission() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
            return;
        }
        IMEINumber = telephonyManager.getDeviceId();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        phoneBookRef.document(IMEINumber).collection("OnlyContacts").orderBy("Name").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            if (mRealm.where(Contact.class).findAll().size() < docCount.size()) {
                                storeContact = new Contact();
                                storeContact.setId(id);
                                Log.d("Change", "Name :" + documentSnapshot.getString("Name"));
                                storeContact.setName(documentSnapshot.getString("Name"));
                                //storeContact.addPhoneNumber((documentSnapshot.getString("Number"));

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
                            contactList.set(newIndex, storeContact);
                            // Collections.sort(contactList, Contact.ConNameComparator);
                            contactAdapter.notifyItemChanged(newIndex);
                            break;

                        case REMOVED:

                            break;
                    }
                }

            }

            private void initRealm() {
                if (mRealm == null || mRealm.isClosed()) {

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
                            if (contact != null) {

                                contact.setBeingSaved(true);
                                contact.setName(storeContact.getName());
                                Log.d(TAG, "Real Number: " + storeContact.getPhoneNumbers());
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