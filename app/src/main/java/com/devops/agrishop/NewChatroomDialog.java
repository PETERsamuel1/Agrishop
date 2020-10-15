package com.devops.agrishop;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.devops.agrishop.models.ChatMessage;
import com.devops.agrishop.models.Chatroom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;




public class NewChatroomDialog extends DialogFragment {

    private static final String TAG = "NewChatroomDialog";

    private EditText mChatroomName;
    private TextView mCreateChatroom, mSecurityLevel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_chatroom, container, false);
        mChatroomName = (EditText) view.findViewById(R.id.input_chatroom_name);
        mCreateChatroom = (TextView) view.findViewById(R.id.create_chatroom);

        mCreateChatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!mChatroomName.getText().toString().equals("")){
                    Log.d(TAG, "onClick: creating new chat room");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE");
                        //get the new chatroom unique id
                        String chatroomId = reference
                                .child(getString(R.string.dbnode_chatrooms))
                                .push().getKey();

                        //create the chatroom
                        Chatroom chatroom = new Chatroom();
                        //chatroom.setSecurity_level(String.valueOf(mSeekBar.getProgress()));
                        chatroom.setChatroom_name(mChatroomName.getText().toString());
                        chatroom.setCreator_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        chatroom.setChatroom_id(chatroomId);


                        //insert the new chatroom into the database
                        reference
                                .child(getString(R.string.dbnode_chatrooms))
                                .child(chatroomId)
                                .setValue(chatroom);

                        //create a unique id for the message
                        String messageId = reference
                                .child(getString(R.string.dbnode_chatrooms))
                                .push().getKey();

                        //insert the first message into the chatroom
                        ChatMessage message = new ChatMessage();

                        message.setMessage("Welcome to the new chatroom!");
                        message.setTimestamp(getTimestamp());
                        reference
                                .child(getString(R.string.dbnode_chatrooms))
                                .child(chatroomId)
                                .child(getString(R.string.field_chatroom_messages))
                                .child(messageId)
                                .setValue(message);
                        ((ChatActivity)getActivity()).init();
                        getDialog().dismiss();

                }

            }
        });

        TextView cancelDialog = view.findViewById(R.id.cancel_chatroom);
        cancelDialog.setOnClickListener(v -> {
            getDialog().dismiss();
        });

        return view;
    }

    /**
     * Return the current timestamp in the form of a string
     * @return
     */
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Africa/Nairobi"));
        return sdf.format(new Date());
    }

}

















