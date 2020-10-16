package com.zwir.bigpen.fragments;


import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.zwir.bigpen.R;
import com.zwir.bigpen.adapters.BookStoreAdapter;
import com.zwir.bigpen.customs.BigPenProgressDialog;
import com.zwir.bigpen.customs.BigPenToast;
import com.zwir.bigpen.data.Book;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.util.GlobalMethods;

import java.text.ParseException;
import java.util.ArrayList;

import static com.zwir.bigpen.util.FirebaseUtil.fireBaseUsers;

/**
 * A simple {@link Fragment} subclass.
 */
public class CartFragment extends Fragment {

    private DatabaseReference cartRef,balanceRef;
    private BookStoreAdapter bookStoreAdapter;
    private LinearLayout emptyViewLayout;
    private TextView emptyData,coastText;
    private ProgressBar progressBS;
    private ArrayList<Book> books;
    private boolean isFirstLoad=true;
    private LinearLayout payLayout;
    private double userBalance=0.0,totalCost=0.0;
    BoomMenuButton bmbPayOption;
    private String bmb_subText;
    public CartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_store, container, false);
        cartRef = fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.fireBaseCartsRef);
        balanceRef =fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.fireBaseBalance);
        emptyViewLayout = view.findViewById(R.id.empty_view_book_store);
        coastText=view.findViewById(R.id.total_coast);
        payLayout=view.findViewById(R.id.pay_layout);
        RecyclerView gridViewBookStore=view.findViewById(R.id.recyclerView_book_store);
        // set a GridLayoutManager with default vertical orientation and 2 number of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        gridViewBookStore.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
        emptyData = view.findViewById(R.id.empty_book_store);
        progressBS=view.findViewById(R.id.progress_b_s);
        bmbPayOption=view.findViewById(R.id.bmb_pay_option);
        books = new ArrayList<>();
        bookStoreAdapter = new BookStoreAdapter(getContext(), books,Constants.bookCartType);
        gridViewBookStore.setAdapter(bookStoreAdapter);
        cartRef.addValueEventListener(valueEventListener);
        cartRef.addChildEventListener(ChildValueEventListener);
        balanceRef.addValueEventListener(balanceEventListener);
        view.findViewById(R.id.pay_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmbPayOption.boom();
            }
        });
        loadBoomButton(bmbPayOption);
        return view;
    }
    private ValueEventListener balanceEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userBalance=Double.parseDouble(dataSnapshot.getValue().toString());
            bmb_subText =getString(R.string.user_balance)+" "+Constants.usdDollar+userBalance;
            loadBoomButton(bmbPayOption);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(isFirstLoad) {
                if (dataSnapshot.exists()) {
                    emptyViewLayout.setVisibility(View.GONE);
                    payLayout.setVisibility(View.VISIBLE);
                    for (DataSnapshot booksSnapshot : dataSnapshot.getChildren()) {
                        Book book = new Book();
                        book.setBookTitle(booksSnapshot.getKey());
                        book.setBookCoverUrl(booksSnapshot.child(Constants.fireBaseBooksCover).getValue().toString());
                        book.setBookPrice(Double.parseDouble(booksSnapshot.child(Constants.fireBaseBooksPrice).getValue().toString()));
                        ArrayList<BookPage> bookPages = new ArrayList<>();
                        for (DataSnapshot bookPagesSnapshot : booksSnapshot.child(Constants.fireBasePagesRef).getChildren()) {
                            bookPages.add(bookPagesSnapshot.getValue(BookPage.class));
                        }
                        book.setBookPages(bookPages);
                        books.add(book);
                        try {
                            totalCost=GlobalMethods.reformDouble(totalCost+=book.getBookPrice());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    bookStoreAdapter.notifyDataSetChanged();
                    String coast=Constants.usdDollar + totalCost;
                    loadBoomButton(bmbPayOption);
                    coastText.setText(coast);
                }
                else {
                    emptyViewLayout.setVisibility(View.VISIBLE);
                    progressBS.setVisibility(View.GONE);
                    emptyData.setText(R.string.message_empty_cart);
                    payLayout.setVisibility(View.GONE);
                }
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            emptyViewLayout.setVisibility(View.VISIBLE);
            progressBS.setVisibility(View.GONE);
            emptyData.setText(R.string.message_loading_books_error);
            payLayout.setVisibility(View.GONE);
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
            bookStoreAdapter.notifyDataSetChanged();
            verificationData();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private void verificationData(){
        if (books.size()==0) {
            emptyViewLayout.setVisibility(View.VISIBLE);
            progressBS.setVisibility(View.GONE);
            emptyData.setText(R.string.message_empty_cart);
            payLayout.setVisibility(View.GONE);
        }
        else{
            loadBoomButton(bmbPayOption);
        }
    }
    public void updateTotalCost(double bookPrice){
         try {
             totalCost=GlobalMethods.reformDouble(totalCost-=bookPrice);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String coast=Constants.usdDollar + totalCost;
        coastText.setText(coast);
    }
    private void loadBoomButton(BoomMenuButton boomMenuButton){
       Typeface typeface= ResourcesCompat.getFont(getContext(), R.font.big_pen_font);
        boomMenuButton.clearBuilders();
        for (int i = 0; i <  boomMenuButton.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder()
                    .normalImageRes(Constants.boomPayOptionImg[i])
                    .normalTextRes(Constants.boomPayTitle[i])
                    .normalTextColorRes(R.color.white)
                    .textSize(14)
                    .typeface(typeface)
                    .subNormalText(i==0?bmb_subText:getString(R.string.coming_soon))
                    .subNormalTextColorRes(i==0?userBalance>=totalCost?R.color.green:R.color.red:R.color.blue)
                    .subTextSize(12)
                    .subTypeface(typeface)
                    .normalColorRes(R.color.colorPrimary)
                    .highlightedColorRes(R.color.colorPrimary)
                    .listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            switch (index){
                                case 0:
                                    if (userBalance>=totalCost)
                                    purchaseWithBigPenBalance(bookStoreAdapter.getBookList());//Pay with your Big Pen balance
                                    else
                                        new BigPenToast(getContext(), R.drawable.ic_error, R.string.insufficient_balance);
                                    break;
                                case 1:
                                    //Pay with sending an SMS
                                    break;
                                case 2:
                                    //Pay with your google play account
                                    break;
                            }
                        }
                    });
            boomMenuButton.addBuilder(builder);
        }

    }
    private void purchaseWithBigPenBalance(ArrayList<Book> books){
        BigPenProgressDialog bigPenProgressDialog = new BigPenProgressDialog(getContext(), getContext().getString(R.string.please_wait));
        bigPenProgressDialog.showMe();
        DatabaseReference mRef = fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.fireBaseBooksRef);
        for (Book book:books){
            DatabaseReference bookRef= mRef.child(book.getBookTitle());
            bookRef.child(Constants.fireBaseBooksCover).setValue(book.getBookCoverUrl());
            DatabaseReference pageRef = bookRef.child(Constants.fireBasePagesRef);
            for (BookPage page : book.getBookPages()) {
                page.setLockState(true);
                pageRef.push().setValue(page);
            }
        }
        bookStoreAdapter.clearBookList();
        cartRef.removeValue();
        try {
            balanceRef.setValue(GlobalMethods.reformDouble(userBalance-totalCost));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        bigPenProgressDialog.dismissMe();
        new BigPenToast(getContext(), R.drawable.ic_cart_validate, R.string.successful_purchase);
    }
    @Override
    public void onDestroy() {
        cartRef.removeEventListener(valueEventListener);
        cartRef.removeEventListener(ChildValueEventListener);
        balanceRef.removeEventListener(balanceEventListener);
        super.onDestroy();
    }
}
