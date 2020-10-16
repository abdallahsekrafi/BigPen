package com.zwir.bigpen.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    public static FirebaseDatabase firebaseDatabase =FirebaseDatabase.getInstance();
    public static DatabaseReference bookRepoRef=firebaseDatabase.getReference(Constants.fireBaseBooksRef);
    public static DatabaseReference fireBaseUsers=firebaseDatabase.getReference(Constants.fireBaseUsers);
}
