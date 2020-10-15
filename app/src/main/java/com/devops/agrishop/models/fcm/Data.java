package com.devops.agrishop.models.fcm;

/**
 * Created by User on 11/16/2017.
 */

public class Data {

    private String title;
    private String message;
    private String data_type;
    private String chatroom_id;

    public Data(String title, String message, String data_type, String chatroom_id) {
        this.title = title;
        this.message = message;
        this.data_type = data_type;
        this.chatroom_id = chatroom_id;
    }

    public Data() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public String getChatroom_id() { return chatroom_id; }

    public void  setChatroom_id(String chatroom_id) { this.chatroom_id = chatroom_id; }

    @Override
    public String toString() {
        return "Data{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", data_type='" + data_type + '\'' +
                ", chatroom_id='" + chatroom_id + '\'' +
                '}';
    }
}
