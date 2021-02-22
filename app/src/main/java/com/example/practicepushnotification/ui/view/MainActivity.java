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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private RecyclerView recyclerView;
    private List<Contact> contactList;

    private RecyclerAdapter contactAdapter;
    private MainActivityViewModel mainActivityViewModel;

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
                            
                            contactList.addAll(mainActivityViewModel.getContacts());
                            Log.d("===>", " ContactPassed: " + contactList.size());
                            Collections.sort(contactList, Contact.ConNameComparator);
                            contactAdapter.notifyDataSetChanged();
                            mainActivityViewModel.writeinFirebase(firebaseDatabase);


                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        Toast.makeText(MainActivity.this, "Permission should be granted", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();


    }

    @Override
    protected void onStart() {
        super.onStart();

        final AtomicBoolean isFirstListener = new AtomicBoolean(true);


        phoneBookRef.orderBy("Name").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

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
                            storeContact = new Contact();
                            storeContact.setId(id);
                            Log.d("Change", "Name :" + documentSnapshot.getString("Name"));
                            storeContact.setName(documentSnapshot.getString("Name"));
                            storeContact.setName(documentSnapshot.getString("Number"));

                            contactList.add(newIndex, storeContact);
                            Collections.sort(contactList, Contact.ConNameComparator);
                            contactAdapter.notifyDataSetChanged();
                            break;

                        case MODIFIED:
                            storeContact = new Contact();
                            storeContact.setId(id);
                            storeContact.setName(documentSnapshot.getString("Name"));
                            storeContact.setPhoneNumber(documentSnapshot.getString("Phone"));

                            contactList.set(newIndex + 1, storeContact);
                            // Collections.sort(contactList, Contact.ConNameComparator);
                            contactAdapter.notifyItemChanged(newIndex + 1);
                            break;

                        case REMOVED:

                            break;
                    }
                }

            }
        });


    }
}