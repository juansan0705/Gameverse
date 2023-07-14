package com.jsancre.gameverse.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jsancre.gameverse.R;
import com.jsancre.gameverse.activities.FiltersActivity;

/**
 * Los 'fragments' son utilizados para crear las vistas de las pantallas
 */
public class FilterFragment extends Fragment {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    View mView;
    CardView mCardViewPS4;
    CardView mCardViewXBOX;
    CardView mCardViewNINTENDO;
    CardView mCardViewPC;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    public FilterFragment() {
    }

    //--------------------
    //   MÉTODO ONCREATE
    //--------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_filter, container, false);

        mCardViewPS4 = mView.findViewById(R.id.cardViewPs4);
        mCardViewXBOX = mView.findViewById(R.id.cardViewXbox);
        mCardViewNINTENDO = mView.findViewById(R.id.cardViewNintendo);
        mCardViewPC = mView.findViewById(R.id.cardViewPc);

        mCardViewPS4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFilterActivity("PS4");
            }
        });

        mCardViewXBOX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFilterActivity("XBOX");
            }
        });

        mCardViewNINTENDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFilterActivity("NINTENDO");
            }
        });

        mCardViewPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFilterActivity("PC");
            }
        });



        return mView;
    }

    private void goToFilterActivity(String category){
        Intent intent = new Intent(getContext(), FiltersActivity.class);
        intent.putExtra("category",category);
        startActivity(intent);

    }
}