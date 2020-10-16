package com.zwir.bigpen.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zwir.bigpen.R;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class BookPageAdapter extends RecyclerView.Adapter<BookPageAdapter.BookPageRowHolder>{

    private ArrayList<BookPage> bookPageList;
    private int height;
    private int width;
    private Context mContext;
    public BookPageAdapter(Context mContext, ArrayList<BookPage>  bookPageList) {
        this.mContext=mContext;
        this.bookPageList = bookPageList;
        width= mContext.getResources().getDisplayMetrics().widthPixels;
        height= mContext.getResources().getDisplayMetrics().heightPixels;
    }

    @NotNull
    @Override
    public BookPageRowHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_page_view,parent,false);
        return new BookPageRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull final BookPageRowHolder holder, final int position) {
        holder.imageLockState.setVisibility(bookPageList.get(position).isLockState()? View.GONE:View.VISIBLE);
        Glide.with(mContext)
                .load(bookPageList.get(position).getPageUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.imageBookPage);
    }

    @Override
    public int getItemCount() {
        return (null != bookPageList ? bookPageList.size() : 0);
    }
    class BookPageRowHolder extends RecyclerView.ViewHolder {
        ImageView imageBookPage;
        RelativeLayout cardBookPage;
        ImageView imageLockState;
        BookPageRowHolder(View itemView) {
            super(itemView);
            this.cardBookPage=itemView.findViewById(R.id.card_book_page);
            this.imageBookPage =itemView.findViewById(R.id.image_book_page);
            this.imageLockState =itemView.findViewById(R.id.image_lock_state);
            this.cardBookPage.getLayoutParams().width=width*3/4;
            this.cardBookPage.getLayoutParams().height=height*3/4;
            imageBookPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bookPageList.get(getAdapterPosition()).isLockState()){
                        Intent returnIntent = new Intent();
                        String pageUrl=bookPageList.get(getAdapterPosition()).getPageUrl();
                        returnIntent.putExtra(Constants.pageUrl, pageUrl);
                        ((AppCompatActivity)mContext).setResult(AppCompatActivity.RESULT_OK, returnIntent);
                        ((AppCompatActivity)mContext).finish();
                    }

                }
            });
        }
    }
}
