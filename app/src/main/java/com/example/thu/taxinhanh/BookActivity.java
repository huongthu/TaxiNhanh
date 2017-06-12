package com.example.thu.taxinhanh;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class BookActivity extends AppCompatActivity {
    private boolean isBookAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        TextView tvPickUp = (TextView)findViewById(R.id.tvPickUp);
        final TextView tvDropOff = (TextView)findViewById(R.id.tvDropOff);
        tvPickUp.setSelected(true);
        tvDropOff.setSelected(true);

        ImageButton btnClearPickUp = (ImageButton)findViewById(R.id.btnClearDropOff);
        btnClearPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDropOff.setText(getResources().getText(R.string.please_choose_dropoff));
                tvDropOff.setTypeface(null, Typeface.ITALIC);
            }
        });

        final ImageButton btnBook = (ImageButton)findViewById(R.id.btnBook);
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
    }
}
