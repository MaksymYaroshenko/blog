package com.example.blog;

import java.util.Date;

public class Comments {
    public String message, user_id;
    public Date timestamp;

    public Comments() {
    }

    public Comments(String message, String userId, Date timestamp) {
        this.message = message;
        this.user_id = userId;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
