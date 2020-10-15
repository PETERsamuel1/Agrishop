package com.devops.agrishop.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;



public class Chatroom implements Parcelable{

    private String chatroom_name;
    private String creator_id;
    private String chatroom_id;
    private List<ChatMessage> chatroom_messages;

    public Chatroom(String chatroom_name, String creator_id, String chatroom_id, List<ChatMessage> chatroom_messages) {
        this.chatroom_name = chatroom_name;
        this.creator_id = creator_id;
        this.chatroom_id = chatroom_id;
        this.chatroom_messages = chatroom_messages;
    }

    public Chatroom() {

    }

    protected Chatroom(Parcel in) {
        chatroom_name = in.readString();
        creator_id = in.readString();
        chatroom_id = in.readString();
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

    public List<ChatMessage> getChatroom_messages() {
        return chatroom_messages;
    }

    public void setChatroom_messages(List<ChatMessage> chatroom_messages) {
        this.chatroom_messages = chatroom_messages;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    public String getChatroom_name() {
        return chatroom_name;
    }

    public void setChatroom_name(String chatroom_name) {
        this.chatroom_name = chatroom_name;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "chatroom_name='" + chatroom_name + '\'' +
                ", creator_id='" + creator_id + '\'' +
                ", chatroom_id='" + chatroom_id + '\'' +
                ", chatroom_messages=" + chatroom_messages +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(chatroom_name);
        parcel.writeString(creator_id);
        parcel.writeString(chatroom_id);
    }
}
