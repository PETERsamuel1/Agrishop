package com.devops.agrishop.models;


public class User {

    private String name;
    private String phone;
    private String county;
    private String subcounty;
    private String type_scale;
    private String type_farming;
    private String profile_image;
    private String role_id;
    private String messaging_token;
    private String department;
    private String user_id;

    public User(String name, String phone, String county, String subcounty, String type_scale, String type_farming,String profile_image, String role_id, String messaging_token, String department, String user_id) {
        this.name = name;
        this.phone = phone;
        this.county = county;
        this.subcounty = subcounty;
        this.type_scale = type_scale;
        this.type_farming = type_farming;
        this.profile_image = profile_image;
        this.department = department;
        this.role_id = role_id;
        this.messaging_token = messaging_token;
        this.user_id = user_id;
    }

    public User() {

    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole_id() {
        return role_id;
    }

    public void setRole_id(String role_id) {
        this.role_id = role_id;
    }

    public String getMessaging_token() {
        return messaging_token;
    }

    public void setMessaging_token(String messaging_token) {
        this.messaging_token = messaging_token;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubcounty() {return subcounty;}

    public void setSubcounty() {this.subcounty = subcounty;}

    public String getType_scale() {return type_scale;}

    public void setType_scale() {this.type_scale = type_scale;}

    public String getType_farming() {return type_farming;}

    public void setType_farming() {this.type_farming = type_farming;}

    public String getCounty() {return county;}

    public void setCounty() {this.county = county;}

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", county='" + county + '\'' +
                ", subcounty='" + subcounty + '\'' +
                ", type_scale='" + type_scale + '\'' +
                ", type_farming='" + type_farming + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", role_id='" + role_id + '\'' +
                ", messaging_token='" + messaging_token + '\'' +
                ", department='" + department + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
