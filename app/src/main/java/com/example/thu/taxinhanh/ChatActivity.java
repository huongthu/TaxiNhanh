package com.example.thu.taxinhanh;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {
    public enum MessageType {
        SELF_MESSAGE,
        OTHER_MESSAGE
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageView btnSend = (ImageView)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etMessage = (EditText)findViewById(R.id.etMessage);
                addMessage(etMessage.getText().toString(), MessageType.SELF_MESSAGE);
                etMessage.setText("");
            }
        });
    }

    private void addMessage(String messageContent, MessageType messageType) {
        if (messageContent.equals("")) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService
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
        LinearLayout llChatContent = (LinearLayout)findViewById(R.id.llChatContent);
        llChatContent.addView(viewMessage);

        //scroll message to end - NOT WORKING
        ScrollView svChatContent = (ScrollView)findViewById(R.id.svChatContent);
        svChatContent.fullScroll(View.FOCUS_DOWN);
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
