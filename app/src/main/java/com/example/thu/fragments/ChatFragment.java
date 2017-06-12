package com.example.thu.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.thu.taxinhanh.R;
import com.example.thu.utils.Enums;

/**
 * Created by thu on 6/12/2017.
 */

public class ChatFragment extends Fragment {
    private View root = null;
    public static Fragment newInstance(Context context) {
        BookFragment f = new BookFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.activity_chat, null);

        ImageView btnSend = (ImageView)root.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etMessage = (EditText)root.findViewById(R.id.etMessage);
                addMessage(etMessage.getText().toString(), Enums.MessageType.SELF_MESSAGE);
                etMessage.setText("");
            }
        });

        return root;
    }

    private void addMessage(String messageContent, Enums.MessageType messageType) {
        if (messageContent.equals("")) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View viewMessage = null;

        //styling this message
        switch (messageType) {
            case SELF_MESSAGE:
                viewMessage = inflater.inflate(R.layout.layout_message_self, null);
                viewMessage.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                ((LinearLayout.LayoutParams)viewMessage.getLayoutParams()).
                        setMargins(dpToPx(50), dpToPx(10), dpToPx(10), dpToPx(10));
                break;
            case OTHER_MESSAGE:
                viewMessage = inflater.inflate(R.layout.layout_message, null);
                viewMessage.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                ((LinearLayout.LayoutParams)viewMessage.getLayoutParams())
                        .setMargins(dpToPx(10), dpToPx(10), dpToPx(50), dpToPx(10));
                break;
            default:
                return;
        }

        TextView tvMessage = (TextView)viewMessage.findViewById(R.id.tvMessage);
        tvMessage.setText(messageContent);

        //find scrollview and add message to the scrollview
        LinearLayout llChatContent = (LinearLayout)root.findViewById(R.id.llChatContent);
        llChatContent.addView(viewMessage);

        //scroll message to end - NOT WORKING
        ScrollView svChatContent = (ScrollView)root.findViewById(R.id.svChatContent);
        svChatContent.fullScroll(View.FOCUS_DOWN);
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
