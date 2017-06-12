package com.example.thu.taxinhanh;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.thu.utils.BookHistory;

import static com.example.thu.taxinhanh.ChatActivity.MessageType.SELF_MESSAGE;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        BookHistory history = new BookHistory("This is a pick up location bla bla bla",
                "This is a drop off location bla bla bla",
                "12/06/2017", "30 mins", "Nguyễn Văn Tài Xế", "http://");

        for (int i = 0; i < 10; i++) {
            addHistoryRow(history);
        }
    }

    private void addHistoryRow(BookHistory history) {
        if (null == history) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View viewHistory = inflater.inflate(R.layout.layout_history_row, null);

        //get TextViews
        TextView tvPickUp = (TextView)viewHistory.findViewById(R.id.tvPickUp);
        TextView tvDropOff = (TextView)viewHistory.findViewById(R.id.tvDropOff);
        TextView tvHistoryDay = (TextView)viewHistory.findViewById(R.id.tvHistoryDay);
        TextView tvHistoryDuring = (TextView)viewHistory.findViewById(R.id.tvHistoryDuring);
        TextView tvHistoryDriver = (TextView)viewHistory.findViewById(R.id.tvHistoryDriver);

        //set textviews' value
        tvPickUp.setText(history.getPickUp());
        tvDropOff.setText(history.getDropOff());
        tvHistoryDay.setText(history.getDate());
        tvHistoryDuring.setText(history.getDuringTime());
        tvHistoryDriver.setText(history.getTaxiDriver());

        //focus textview to scroll horizontally
        tvPickUp.setSelected(true);
        tvDropOff.setSelected(true);
        tvHistoryDay.setSelected(true);
        tvHistoryDuring.setSelected(true);
        tvHistoryDriver.setSelected(true);

        //find linearLayout and put history row to this
        LinearLayout llChatContent = (LinearLayout)findViewById(R.id.llHistoryContent);
        llChatContent.addView(viewHistory);

        //scroll to end - NOT WORKING
        ScrollView svChatContent = (ScrollView)findViewById(R.id.svHistoryContent);
        svChatContent.fullScroll(View.FOCUS_DOWN);
    }
}
