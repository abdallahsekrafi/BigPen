package com.zwir.bigpen.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.zwir.bigpen.R;
import com.zwir.bigpen.customs.BigPenProgressDialog;
import com.zwir.bigpen.customs.BigPenToast;
import com.zwir.bigpen.data.Book;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.fragments.BookPageFragment;
import com.zwir.bigpen.fragments.CartFragment;
import com.zwir.bigpen.fragments.LogInOrCreateFragment;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import static com.zwir.bigpen.util.FirebaseUtil.fireBaseUsers;

public class BookStoreAdapter extends RecyclerView.Adapter<BookStoreAdapter.BookStoreRowHolder> implements Filterable {
    private ArrayList<Book> bookList;
    private ArrayList<Book> bookListBackup;
    private Context mContext;
    private int height,viewType;
    public BookStoreAdapter(Context context, ArrayList<Book> bookList,int viewType) {
        this.bookList = bookList;
        bookListBackup=bookList;
        this.viewType=viewType;
        mContext = context;
        height= context.getResources().getDisplayMetrics().heightPixels;
    }

    @NonNull
    @Override
    public BookStoreRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_store_view, parent,false);
        return new BookStoreRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookStoreRowHolder holder, int position) {
        switch (viewType){
            case Constants.bookPurchasedType:
                holder.addBookToCart.setVisibility(View.GONE);
                holder.linearLayout.getLayoutParams().width= ViewGroup.LayoutParams.MATCH_PARENT;
                holder.linearLayout.setGravity(Gravity.CENTER);
                holder.textViewBookPrice.setVisibility(View.GONE);
                break;
            case Constants.bookCartType:
                holder.addBookToCart.setImageResource(R.drawable.ic_cart_delete);
                break;
        }
        holder.textViewBookTitle.setText(bookList.get(position).getBookTitle());
        if (viewType!=Constants.bookPurchasedType){
            String price= Constants.usdDollar + String.valueOf(bookList.get(position).getBookPrice());
            holder.textViewBookPrice.setText(price);
        }
        Glide.with(mContext)
                .load(bookList.get(position).getBookCoverUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.imageViewBookCover);
    }

    @Override
    public int getItemCount() {
        return (null != bookList ? bookList.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void addBookToCart(Book book){
        BigPenProgressDialog bigPenProgressDialog = new BigPenProgressDialog(mContext, mContext.getString(R.string.please_wait));
        bigPenProgressDialog.showMe();
        DatabaseReference mRef = fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.fireBaseCartsRef).child(book.getBookTitle());
        mRef.child(Constants.fireBaseBooksCover).setValue(book.getBookCoverUrl());
        mRef.child(Constants.fireBaseBooksPrice).setValue(book.getBookPrice());
        DatabaseReference pageRef = mRef.child(Constants.fireBasePagesRef);
        for (BookPage page : book.getBookPages()) {
            pageRef.push().setValue(page);
        }
        bigPenProgressDialog.dismissMe();
        new BigPenToast(mContext, R.drawable.ic_cart, R.string.successful_added_to_cart);
    }
    private void deleteBookFromCart(Book book){
        BigPenProgressDialog bigPenProgressDialog = new BigPenProgressDialog(mContext, mContext.getString(R.string.please_wait));
        bigPenProgressDialog.showMe();
        DatabaseReference mRef = fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.fireBaseCartsRef).child(book.getBookTitle());
        mRef.removeValue();
        bookList.remove(book);
        CartFragment cartFragment = (CartFragment)(((AppCompatActivity)mContext).getSupportFragmentManager().findFragmentByTag("cart_fragment"));
        if (cartFragment != null && cartFragment.isVisible())
            cartFragment.updateTotalCost(book.getBookPrice());
        bigPenProgressDialog.dismissMe();
    }
    public ArrayList<Book> getBookList() {
        return bookList;
    }
    public void clearBookList(){
        bookList.clear();
    }
    class BookStoreRowHolder extends RecyclerView.ViewHolder{
        ImageView imageViewBookCover;
        TextView textViewBookTitle;
        TextView textViewBookPrice;
        RelativeLayout bookCoverLayout;
        ImageButton addBookToCart;
        LinearLayout linearLayout;
        public BookStoreRowHolder(@NonNull View itemView) {
            super(itemView);
            this.bookCoverLayout=itemView.findViewById(R.id.card_book_store);
            this.imageViewBookCover=itemView.findViewById(R.id.image_book_cover);
            this.textViewBookTitle=itemView.findViewById(R.id.book_store_title);
            this.textViewBookPrice=itemView.findViewById(R.id.book_store_price);
            this.addBookToCart=itemView.findViewById(R.id.add_book_to_cart);
            this.bookCoverLayout.getLayoutParams().height=height/3;
            this.linearLayout=itemView.findViewById(R.id.book_title_layout);
            //
            this.imageViewBookCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(Constants.parcelableBookPages,bookList.get(getAdapterPosition()).getBookPages());
                    FragmentManager fragmentManager= ((AppCompatActivity)mContext).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                    BookPageFragment bookPageFragment=new BookPageFragment();
                    bookPageFragment.setArguments(bundle);
                    fragmentTransaction.add(R.id.fragment_layout,bookPageFragment, Constants.pageFragmentTag);
                    fragmentTransaction.addToBackStack(Constants.pageFragmentTag);
                    fragmentTransaction.commit();
                }
            });
            this.addBookToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                        switch (viewType){
                            case Constants.bookStoreType:
                                addBookToCart(bookList.get(getAdapterPosition()));
                                // add book to cart
                                break;
                            case Constants.bookCartType:
                                deleteBookFromCart(bookList.get(getAdapterPosition()));
                                //delete book from cart
                                break;
                        }
                    }
                    else {
                        LogInOrCreateFragment logInOrCreateFragment = new LogInOrCreateFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.actionString, Constants.addBookToCart);
                        logInOrCreateFragment.setArguments(bundle);
                        logInOrCreateFragment.show(((AppCompatActivity)mContext).getSupportFragmentManager(), "login_or_create");
                    }
                }
            });
        }
    }
    @Override
    public Filter getFilter() {
        return bookFilter;
    }
    private Filter bookFilter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Book> filteredBookList=new ArrayList<>();
            if (constraint==null||constraint.length()==0){
                filteredBookList.addAll(bookListBackup);
            }
            else {
                String filterPattern=constraint.toString().toLowerCase().trim();
                for (Book bookBackup: bookListBackup) {
                    if (bookBackup.getBookTitle().toLowerCase().contains(filterPattern))
                        filteredBookList.add(bookBackup);
                }
            }
            FilterResults results=new FilterResults();
            results.values=filteredBookList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            bookList=(ArrayList<Book>)results.values;
            notifyDataSetChanged();
        }
    };
}