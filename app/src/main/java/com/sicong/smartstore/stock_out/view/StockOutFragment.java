package com.sicong.smartstore.stock_out.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sicong.smartstore.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StockOutFragment extends Fragment {


    public StockOutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stock_out, container, false);
    }

}
