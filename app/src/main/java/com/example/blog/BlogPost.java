package com.example.blog;


import java.util.Date;

public class BlogPost {

    public String user_id, image_url, image_thumb, desc;
    public Date timestamp;

    public BlogPost() {
    }

    public BlogPost(String image_thumb, String image_url, String desc, String user_id, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.image_thumb = image_thumb;
        this.desc = desc;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
