package com.example.thu.fragments;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.thu.taxinhanh.R;

/**
 * Created by thu on 6/12/2017.
 * Guide at http://manishkpr.webheavens.com/android-navigation-drawer-example-using-fragments/
 */

public class BookFragment extends Fragment {
    private boolean isBookAvailable = false;

    public static Fragment newInstance(Context context) {
        BookFragment f = new BookFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_book, null);

        TextView tvPickUp = (TextView)root.findViewById(R.id.tvPickUp);
        final TextView tvDropOff = (TextView)root.findViewById(R.id.tvDropOff);
        tvPickUp.setSelected(true);
        tvDropOff.setSelected(true);

        ImageButton btnClearPickUp = (ImageButton)root.findViewById(R.id.btnClearDropOff);
        btnClearPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDropOff.setText(getResources().getText(R.string.please_choose_dropoff));
                tvDropOff.setTypeface(null, Typeface.ITALIC);
            }
        });

        final ImageButton btnBook = (ImageButton)root.findViewById(R.id.btnBook);
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBookAvailable) {
                    btnBook.setImageResource(R.drawable.book_visible);
                } else {
                    btnBook.setImageResource(R.drawable.book_invisible);
                }
                isBookAvailable = !isBookAvailable;

            }
        });

        return root;
    }
}
