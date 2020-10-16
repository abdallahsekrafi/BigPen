package com.zwir.bigpen.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gtomato.android.ui.transformer.CoverFlowViewTransformer;
import com.gtomato.android.ui.widget.CarouselView;
import com.zwir.bigpen.R;
import com.zwir.bigpen.adapters.BookPageAdapter;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;

import java.util.ArrayList;

public class BookPageFragment extends Fragment {


    public BookPageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        assert getArguments() != null;
        ArrayList<BookPage> bookPages=getArguments().getParcelableArrayList(Constants.parcelableBookPages);
        View view= inflater.inflate(R.layout.fragment_book_page, container, false);
        CarouselView carouselView =view.findViewById(R.id.carousel_book_pages);
        CoverFlowViewTransformer transformer = new CoverFlowViewTransformer();
        transformer.setYProjection(10f);
        carouselView.setTransformer(transformer);
        carouselView.setGravity(Gravity.CENTER);
        carouselView.setInfinite(false);
        carouselView.setClickToScroll(true);
        BookPageAdapter bookPageAdapter=new BookPageAdapter(getContext(),bookPages);
        carouselView.setAdapter(bookPageAdapter);
        return view;
    }
}
