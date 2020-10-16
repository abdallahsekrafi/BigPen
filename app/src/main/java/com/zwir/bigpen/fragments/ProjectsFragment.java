package com.zwir.bigpen.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zwir.bigpen.R;
import com.zwir.bigpen.adapters.ProjectBookAdapter;
import com.zwir.bigpen.util.OnBackPressed;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;

import java.util.ArrayList;

import static com.zwir.bigpen.util.FirebaseUtil.fireBaseUsers;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectsFragment extends Fragment implements OnBackPressed {
    private DatabaseReference mRef,usedStorageRef;
    private ProjectBookAdapter projectBookAdapter;
    private LinearLayout emptyViewLayout;
    private TextView emptyData;
    private ProgressBar progressBS;
    private ArrayList<BookPage> bookPages;
    private RelativeLayout deleteLayout;
    private TextView totalDelete;
    private boolean isFirstLoad=true;
    private int usedStorage;
    public ProjectsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_projects, container, false);
        mRef = fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.fireBaseUsersProject);
        usedStorageRef = fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.fireBaseUsedStorage);
        emptyViewLayout = view.findViewById(R.id.empty_view_book_project);
        RecyclerView gridViewBookStore=view.findViewById(R.id.recyclerView_book_project);
        // set a GridLayoutManager with default vertical orientation and 2 number of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        gridViewBookStore.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
        emptyData = view.findViewById(R.id.empty_book_project);
        progressBS = view.findViewById(R.id.progress_b_p);
        deleteLayout=view.findViewById(R.id.delete_layout);
        totalDelete=view.findViewById(R.id.total_delete);
        view.findViewById(R.id.delete_btn).setOnClickListener(deleteListener);
        bookPages = new ArrayList<>();
        projectBookAdapter = new ProjectBookAdapter(this,getContext(), bookPages);
        gridViewBookStore.setAdapter(projectBookAdapter);
        mRef.addChildEventListener(ChildValueEventListener);
        mRef.addValueEventListener(valueEventListener);
        usedStorageRef.addValueEventListener(usedStorageEventListener);
        return view;
    }
    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(isFirstLoad) {
                if (dataSnapshot.exists()) {
                    emptyViewLayout.setVisibility(View.GONE);
                    for (DataSnapshot bookPagesSnapshot : dataSnapshot.getChildren()) {
                        bookPages.add(bookPagesSnapshot.getValue(BookPage.class));
                    }
                    projectBookAdapter.notifyDataSetChanged();
                } else {
                    emptyViewLayout.setVisibility(View.VISIBLE);
                    progressBS.setVisibility(View.GONE);
                    emptyData.setText(R.string.message_empty_project);
                }
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            emptyViewLayout.setVisibility(View.VISIBLE);
            progressBS.setVisibility(View.GONE);
            emptyData.setText(R.string.message_loading_books_error);
        }
    };
    private View.OnClickListener deleteListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            projectBookAdapter.confirmDeleteMultipleProject();
        }
    };

    private ChildEventListener ChildValueEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            isFirstLoad=false;
            projectBookAdapter.notifyDataSetChanged();
            verificationData();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private ValueEventListener usedStorageEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            usedStorage=Integer.parseInt(dataSnapshot.getValue().toString()) ;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    public void decreaseUsedStorage(int Byte){
        int newUsedStorage=usedStorage-Byte;
        usedStorageRef.setValue(newUsedStorage);
    }
    private void verificationData(){
        if (bookPages.size()==0){
            emptyViewLayout.setVisibility(View.VISIBLE);
            progressBS.setVisibility(View.GONE);
            emptyData.setText(R.string.message_empty_project);
        }
    }
    @Override
    public void onDestroy() {
        mRef.removeEventListener(ChildValueEventListener);
        mRef.removeEventListener(valueEventListener);
        usedStorageRef.removeEventListener(usedStorageEventListener);
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (projectBookAdapter.getMultipleSelection()){
            projectBookAdapter.disableMultipleSelection();
            return true;
        }
        else
        return false;
    }
    public void setLayoutVisibility(int size){
        deleteLayout.setVisibility((size==0)?View.GONE:View.VISIBLE);
        totalDelete.setText(String.valueOf(size));
    }
}
