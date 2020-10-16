package com.zwir.bigpen.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zwir.bigpen.R;
import com.zwir.bigpen.customs.BigPenProgressDialog;
import com.zwir.bigpen.customs.BigPenToast;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.fragments.ProjectsFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import static com.zwir.bigpen.util.FirebaseUtil.fireBaseUsers;

public class ProjectPageAdapter extends RecyclerView.Adapter<ProjectPageAdapter.BookPageRowHolder>{

    private ArrayList<BookPage> bookPageList;
    private int height;
    private int width;
    private Context mContext;
    public ProjectPageAdapter(Context mContext, ArrayList<BookPage>  bookPageList ) {
        this.mContext=mContext;
        this.bookPageList = bookPageList;
        width= mContext.getResources().getDisplayMetrics().widthPixels;
        height= mContext.getResources().getDisplayMetrics().heightPixels;
    }

    @NotNull
    @Override
    public BookPageRowHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_page_view,parent,false);
        return new BookPageRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull final BookPageRowHolder holder, final int position) {

        Glide.with(mContext)
                .load(bookPageList.get(position).getPageUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.projectPage);
    }

    @Override
    public int getItemCount() {
        return (null != bookPageList ? bookPageList.size() : 0);
    }
    class BookPageRowHolder extends RecyclerView.ViewHolder {
        ImageView projectPage;
        RelativeLayout projectCardPage;
        ImageButton deleteProject;
        BookPageRowHolder(View itemView) {
            super(itemView);
            this.projectCardPage =itemView.findViewById(R.id.card_project_page);
            this.projectPage =itemView.findViewById(R.id.image_project_page);
            this.deleteProject=itemView.findViewById(R.id.delete_project);
            this.projectCardPage.getLayoutParams().width=width*3/4;
            this.projectCardPage.getLayoutParams().height=height*3/4;
            projectPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Intent returnIntent = new Intent();
                        String pageUrl=bookPageList.get(getAdapterPosition()).getPageUrl();
                        returnIntent.putExtra(Constants.pageUrl, pageUrl);
                        ((AppCompatActivity)mContext).setResult(AppCompatActivity.RESULT_OK, returnIntent);
                        ((AppCompatActivity)mContext).finish();

                }
            });
            deleteProject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDeleteOneProject(bookPageList.get(getAdapterPosition()));
                }
            });
        }
    }
    private void confirmDeleteOneProject(final BookPage bookPageToDelete){
        String message=(mContext).getString(R.string.deleting_progress_dialog);
        final BigPenProgressDialog bigPenProgressDialog=new BigPenProgressDialog(mContext,message);
        AlertDialog.Builder builder =
                new AlertDialog.Builder(mContext, R.style.AppDialogStyle);
        // set the AlertDialog's message
        View customTitle = ((AppCompatActivity)mContext).getLayoutInflater().inflate(
                R.layout.custom_dialog_title, null);
        customTitle.findViewById(R.id.dialog_icon).setBackgroundResource(R.drawable.ic_delete);
        ((TextView)customTitle.findViewById(R.id.dialog_title)).setText(R.string.title_delete_image_dialog);
        builder.setCustomTitle(customTitle);
        builder.setMessage(R.string.message_confirm_delete_page);
        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bigPenProgressDialog.showMe();
                StorageReference fileRef= FirebaseStorage.getInstance()
                        .getReferenceFromUrl(bookPageToDelete.getPageUrl());
                fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateUsedStorage(bookPageToDelete.getPageSize());
                        Query myRef= fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(Constants.fireBaseUsersProject)
                                .orderByChild(Constants.pageUrl).equalTo(bookPageToDelete.getPageUrl());
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot bookPageSnapshot: dataSnapshot.getChildren()) {
                                    if (bookPageSnapshot.getValue(BookPage.class).getPageUrl().equals(bookPageToDelete.getPageUrl())){
                                        bookPageSnapshot.getRef().removeValue();
                                        bookPageList.remove(bookPageToDelete);
                                        notifyDataSetChanged();
                                    }
                                }
                                bigPenProgressDialog.dismissMe();
                                new BigPenToast(mContext,R.drawable.ic_delete,R.string.message_delete_page_success);
                                if (bookPageList.size()==0){
                                    Fragment fragment =((AppCompatActivity)mContext).getSupportFragmentManager().findFragmentByTag(Constants.pageFragmentTag);
                                    if(fragment != null)
                                        ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                bigPenProgressDialog.dismissMe();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bigPenProgressDialog.dismissMe();
                        new BigPenToast(mContext,R.drawable.ic_error,R.string.message_delete_page_failed);
                    }
                });

            }
        });
        // add cancel Button
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.create().show();
    }
    private void updateUsedStorage(int Byte){
        ProjectsFragment projectsFragment = (ProjectsFragment)(((AppCompatActivity)mContext).getSupportFragmentManager().findFragmentByTag("projects_fragment"));
        if (projectsFragment != null && projectsFragment.isVisible())
            projectsFragment.decreaseUsedStorage(Byte);
    }
}
