package com.kiddnation254.kiddnation254;

public class Comment {
    private String username;
    private String commentBody;
    private String userImg;
    private String commentTime;

    public Comment(String username, String commentBody, String commentTime, String userImg) {
        this.username = username;
        this.commentBody = commentBody;
        this.commentTime = commentTime;
        this.userImg = userImg;
    }
 
    public String getUsername() {
        return this.username;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }
 
    public String getCommentBody() {
        return this.commentBody;
    }
 
    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }
 
    public String getUserImg() {
        return this.userImg;
    }
 
    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getCommentTime(){
        return this.commentTime;
    }

    public void setCommentTime(String commentTime){
        this.commentTime = commentTime;
    }

}