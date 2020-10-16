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
import com.zwir.bigpen.adapters.ProjectPageAdapter;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectsPagesFragment extends Fragment {


    public ProjectsPagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        assert getArguments() != null;
        ArrayList<BookPage> bookPages=getArguments().getParcelableArrayList(Constants.parcelableBookPages);
        int position=getArguments().getInt(Constants.pagesPosition);
        View view= inflater.inflate(R.layout.fragment_projects_pages, container, false);
        CarouselView carouselView =view.findViewById(R.id.carousel_project_pages);
        CoverFlowViewTransformer transformer = new CoverFlowViewTransformer();
        transformer.setYProjection(10f);
        carouselView.setTransformer(transformer);
        carouselView.setGravity(Gravity.CENTER);
        carouselView.setInfinite(false);
        carouselView.setClickToScroll(true);
        ProjectPageAdapter projectPageAdapter=new ProjectPageAdapter(getContext(),bookPages);
        carouselView.setAdapter(projectPageAdapter);
        carouselView.smoothScrollToPosition(position);
        return view;
    }

}
