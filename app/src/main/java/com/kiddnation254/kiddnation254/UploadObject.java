package com.kiddnation254.kiddnation254;

public class UploadObject {
    private String message;
    private Boolean error;
    private  String user_image;

    public UploadObject(Boolean error, String message, String user_image) {
        this.error = error;
        this.message = message;
        this.user_image = user_image;
    }
    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getUser_image() {
        return user_image;
    }
}