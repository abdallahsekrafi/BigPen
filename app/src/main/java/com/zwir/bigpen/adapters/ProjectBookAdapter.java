package com.zwir.bigpen.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.zwir.bigpen.fragments.ProjectsPagesFragment;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import static com.zwir.bigpen.util.FirebaseUtil.fireBaseUsers;

public class ProjectBookAdapter extends RecyclerView.Adapter<ProjectBookAdapter.ProjectBookRowHolder>{

    private ArrayList<BookPage> bookPages;
    private Context mContext;
    private ProjectsFragment projectsFragment;
    private boolean multipleSelection = false;
    private SparseBooleanArray listSelectionState;
    private ArrayList<BookPage> pagesListToDelete;
    private int height;
    public ProjectBookAdapter(ProjectsFragment projectsFragment, Context context, ArrayList<BookPage> bookPages) {
        this.bookPages = bookPages;
        mContext = context;
        height = context.getResources().getDisplayMetrics().heightPixels;
        listSelectionState = new SparseBooleanArray();
        pagesListToDelete = new ArrayList<>();
        this.projectsFragment = projectsFragment;
    }

    @NonNull
    @Override
    public ProjectBookRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_book_view, parent, false);
        return new ProjectBookRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectBookRowHolder holder, int position) {
        Glide.with(mContext)
                .load(bookPages.get(position).getPageUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.projectBookCover);
        holder.selectionState.setVisibility(multipleSelection ? View.VISIBLE : View.GONE);
        holder.selectionState.setChecked(listSelectionState.get(position, false));
    }

    @Override
    public int getItemCount() {
        return (null != bookPages ? bookPages.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public boolean getMultipleSelection() {
        return multipleSelection;
    }

    public void disableMultipleSelection() {
        multipleSelection = false;
        notifyDataSetChanged();
        pagesListToDelete.clear();
        projectsFragment.setLayoutVisibility(pagesListToDelete.size());
    }
    public void confirmDeleteMultipleProject() {
        String message = (mContext).getString(R.string.deleting_progress_dialog);
        final BigPenProgressDialog bigPenProgressDialog = new BigPenProgressDialog(mContext, message);
        AlertDialog.Builder builder =
                new AlertDialog.Builder(mContext, R.style.AppDialogStyle);
        // set the AlertDialog's message
        View customTitle = ((AppCompatActivity) mContext).getLayoutInflater().inflate(
                R.layout.custom_dialog_title, null);
        customTitle.findViewById(R.id.dialog_icon).setBackgroundResource(R.drawable.ic_delete);
        ((TextView) customTitle.findViewById(R.id.dialog_title)).setText(R.string.title_delete_image_dialog);
        builder.setCustomTitle(customTitle);
        builder.setMessage(R.string.message_confirm_delete_page);
        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bigPenProgressDialog.showMe();
                int totalSizeToDelete = 0;
                for (final BookPage bookPageToDelete : pagesListToDelete) {
                    totalSizeToDelete += bookPageToDelete.getPageSize();
                    StorageReference fileRef = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(bookPageToDelete.getPageUrl());
                    fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Query myRef = fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(Constants.fireBaseUsersProject)
                                    .orderByChild(Constants.pageUrl).equalTo(bookPageToDelete.getPageUrl());
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot bookPageSnapshot : dataSnapshot.getChildren()) {
                                        if (bookPageSnapshot.getValue(BookPage.class).getPageUrl().equals(bookPageToDelete.getPageUrl())) {
                                            bookPageSnapshot.getRef().removeValue();
                                            bookPages.remove(bookPageToDelete);
                                            notifyDataSetChanged();
                                        }

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    });
                }
                projectsFragment.decreaseUsedStorage(totalSizeToDelete);
                bigPenProgressDialog.dismissMe();
                new BigPenToast(mContext, R.drawable.ic_delete, R.string.message_delete_page_success);
                disableMultipleSelection();
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
    class ProjectBookRowHolder extends RecyclerView.ViewHolder{
        ImageView projectBookCover;
        RelativeLayout projectBookCoverLayout;
        RadioButton selectionState;

        public ProjectBookRowHolder(@NonNull View itemView) {
            super(itemView);
            this.projectBookCoverLayout = itemView.findViewById(R.id.project_card_book);
            this.selectionState = itemView.findViewById(R.id.selection_state);
            this.projectBookCover = itemView.findViewById(R.id.project_book_cover);
            this.projectBookCoverLayout.getLayoutParams().height = height / 3;
            this.projectBookCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (multipleSelection) {
                        if (selectionState.isChecked())
                            pagesListToDelete.remove(bookPages.get(getAdapterPosition()));
                        else
                            pagesListToDelete.add(bookPages.get(getAdapterPosition()));
                        projectsFragment.setLayoutVisibility(pagesListToDelete.size());
                        listSelectionState.put(getAdapterPosition(), !selectionState.isChecked());
                        notifyDataSetChanged();
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(Constants.parcelableBookPages, bookPages);
                        bundle.putInt(Constants.pagesPosition, getAdapterPosition());
                        FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        ProjectsPagesFragment projectsPagesFragment = new ProjectsPagesFragment();
                        projectsPagesFragment.setArguments(bundle);
                        fragmentTransaction.add(R.id.fragment_layout, projectsPagesFragment, Constants.pageFragmentTag);
                        fragmentTransaction.addToBackStack(Constants.pageFragmentTag);
                        fragmentTransaction.commit();
                    }

                }
            });
            this.projectBookCover.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!multipleSelection) {
                        multipleSelection = true;
                        listSelectionState.clear();
                        listSelectionState.put(getAdapterPosition(), true);
                        pagesListToDelete.clear();
                        pagesListToDelete.add(bookPages.get(getAdapterPosition()));
                        projectsFragment.setLayoutVisibility(pagesListToDelete.size());
                        notifyDataSetChanged();
                    }
                    return false;
                }
            });
        }
    }
}