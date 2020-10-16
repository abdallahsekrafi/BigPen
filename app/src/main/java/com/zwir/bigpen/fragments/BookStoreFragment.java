package com.zwir.bigpen.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.zwir.bigpen.R;
import com.zwir.bigpen.activitys.BookRepository;
import com.zwir.bigpen.adapters.BookStoreAdapter;
import com.zwir.bigpen.data.Book;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.zwir.bigpen.util.FirebaseUtil.bookRepoRef;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookStoreFragment extends Fragment {

    private BookStoreAdapter bookStoreAdapter;
    private LinearLayout emptyViewLayout;
    private TextView emptyData;
    private ProgressBar progressBS;
    private ArrayList<Book> books;
    public BookStoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_store, container, false);
        emptyViewLayout = view.findViewById(R.id.empty_view_book_store);
        RecyclerView gridViewBookStore=view.findViewById(R.id.recyclerView_book_store);
        // set a GridLayoutManager with default vertical orientation and 2 number of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        gridViewBookStore.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
        emptyData = view.findViewById(R.id.empty_book_store);
        progressBS=view.findViewById(R.id.progress_b_s);
        books = new ArrayList<>();
        bookStoreAdapter = new BookStoreAdapter(getContext(), books,Constants.bookStoreType);
        gridViewBookStore.setAdapter(bookStoreAdapter);
        bookRepoRef.addValueEventListener(valueEventListener);
        return view;
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                emptyViewLayout.setVisibility(View.GONE);
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
                }
                bookStoreAdapter.notifyDataSetChanged();
                List<String> listTitle=new ArrayList<>();
                for (Book bookSearch: books)
                    listTitle.add(bookSearch.getBookTitle());
                ((BookRepository)getActivity()).setVisibleSearchView(bookStoreAdapter,listTitle);
            } else {
                ((BookRepository)getActivity()).setInVisibleSearchView();
                emptyViewLayout.setVisibility(View.VISIBLE);
                progressBS.setVisibility(View.GONE);
                emptyData.setText(R.string.message_empty_data);
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            emptyViewLayout.setVisibility(View.VISIBLE);
            progressBS.setVisibility(View.GONE);
            emptyData.setText(R.string.message_loading_books_error);
        }
    };

    @Override
    public void onDestroy() {
        bookRepoRef.removeEventListener(valueEventListener);
        ((BookRepository)getActivity()).setInVisibleSearchView();
        super.onDestroy();
    }
}
