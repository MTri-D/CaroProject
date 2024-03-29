package com.example.caroproject.Adapter;

import android.content.ContentResolver;
import android.net.Uri;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.example.caroproject.Data.Message;
import com.example.caroproject.Data.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseStorage storage;


    /**
     * @param collection table name
     * @param data data object that need to up to database
     */
    public void addDataToDatabase(String collection, Object data) {
        firestore.collection(collection).add(data);
    }

    /**
     * @param collection table name
     * @param id element id
     * @param data data object that need to up to database
     */
    public void addDataToDatabase(String collection, String id, Object data) {
        firestore.collection(collection).document(id).set(data);
    }

    /**
     * @param collection table name
     * @param id element id
     * @param type data type
     * @param listener the interface that return the object
     */
    public<T> void retrieveDataFromDatabase(String collection, String id, Class<T> type, OnCompleteRetrieveDataListener listener) {
        firestore.collection(collection).document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()) {
                        List<T> result = new ArrayList<>();
                        result.add(documentSnapshot.toObject(type));
                        listener.onComplete(result);
                    } else {
                        listener.onComplete(null);
                    }
                }
            }
        });
    }

    /**
     * @param collection table name
     * @param listener the interface that return the object
     */
    public<T> void retrieveDataFromDatabase(String collection, Class<T> type, OnCompleteRetrieveDataListener listener) {
        firestore.collection(collection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()) {
                        List<T> result = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot :
                                querySnapshot) {
                            result.add(documentSnapshot.toObject(type));
                        }
                        listener.onComplete(result);

                    }else {
                        listener.onComplete(null);
                    }
                }

            }
        });
    }

    public void logOut() {
        auth.signOut();
    }

    public void getUserIdByUsername(String username, OnGetUserIdListener listener) {
        CollectionReference usersCollection = firestore.collection("UserInfo");

        Query query = usersCollection.whereEqualTo("username", username);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Document found, pass the document ID to the listener
                    String documentId = document.getId();
                    listener.onSuccess(documentId);
                    return; // Stop processing after finding the first match
                }
                // If no matching document is found
                listener.onSuccess(null);
            } else {
                // Pass the exception to the listener
                listener.onFailure(task.getException());
            }
        });
    }

    public interface OnGetUserByIdListener {
        void onSuccess(UserInfo userInfo);
        void onFailure(Exception e);
    }

    public void getUserById(String userId, OnGetUserByIdListener listener) {
        DocumentReference userDocument = firestore.collection("UserInfo").document(userId);

        userDocument.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Convert the document snapshot to a UserInfo object
                    UserInfo userInfo = document.toObject(UserInfo.class);
                    listener.onSuccess(userInfo);
                } else {
                    // Document not found
                    listener.onSuccess(null);
                }
            } else {
                // Pass the exception to the listener
                listener.onFailure(task.getException());
            }
        });
    }


    public interface OnGetUserIdListener {
        void onSuccess(String userId);
        void onFailure(Exception e);
    }


    public void changePassword(String password) {
        FirebaseUser user = auth.getCurrentUser();
        user.updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //DO NOTHING
                    }
                });
    }

    public void reAuthenticate(String password, OnResultListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            listener.onResult(true);
                        } else {
                            listener.onResult(false);
                        }
                    }
                });
    }

    public void uploadUserAvatar(String userId, Uri imageUri, String imageType, OnResultUploadAvatarListener listener) {
        String folder = "Avatar";
        StorageReference storageRef = storage.getReference(folder).child(userId + "." + imageType);
        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        listener.onResult(uri);
                                    }
                                });
                    }
                });
    }


    public interface OnCompleteRetrieveDataListener {
        <T>void onComplete(List<T> list);
    }


    public interface OnResultListener{
        void onResult(boolean result);
    }

    public interface OnResultUploadAvatarListener {
        void onResult(Uri Uri);
    }

    public static DocumentReference getChatRoomRef(String chatroomID){
        return FirebaseFirestore.getInstance().collection("ChatRoom").document(chatroomID);
    }
    public static CollectionReference getMsgRef(String chatroomID){
        return getChatRoomRef(chatroomID).collection("chats");
    }

    public static String getChatRoomID(String userID1,String userID2){
        if(userID1.hashCode()<userID2.hashCode()){
            return userID1 + "_" + userID2;
        }else {
            return userID2 + "_" + userID1;
        }
    }



    private static FirebaseHelper instance;
    private FirebaseHelper() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }
    public static FirebaseHelper getInstance() {
        if(instance != null) {
            return  instance;
        } else {
            return new FirebaseHelper();
        }
    }

    public void SetTatus(boolean status){
        String uid  = auth.getCurrentUser().getUid();
        Map<String,Object> updates = new HashMap<>();
        updates.put("online",status);
        firestore.collection("UserInfo").document(uid).update(updates);
    }
}
