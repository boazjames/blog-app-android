package com.kiddnation254.kiddnation254;

public class Post {
    private String title;
    private String body;
    private String thumbnail;
    private String author;
    private String postedAt;
    private int postId;
 
    public Post(int postId, String title, String body, String thumbnail, String author, String postedAt) {
        this.title = title;
        this.body = body;
        this.thumbnail = thumbnail;
        this.author = author;
        this.postedAt = postedAt;
        this.postId = postId;
    }
 
    public String getTitle() {
        return this.title;
    }
 
    public void setTitle(String title) {
        this.title = title;
    }
 
    public String getBody() {
        return this.body;
    }
 
    public void setBody(String body) {
        this.body = body;
    }
 
    public String getThumbnail() {
        return this.thumbnail;
    }
 
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getAuthor(){
        return this.author;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getTime(){
        return this.postedAt;
    }

    public void setTime(String postedAt){
        this.postedAt = postedAt;
    }

    public int getPostId(){
        return this.postId;
    }

    public void setPostId(int postId){
        this.postId = postId;
    }
}