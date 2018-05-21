package com.kiddnation254.kiddnation254;

public class Video {
    private int videoId;
    private String videoCode;
    private String videoTitle;

    public Video(int videoId, String videoCode, String videoTitle) {
        this.videoId = videoId;
        this.videoCode = videoCode;
        this.videoTitle = videoTitle;
    }
 
    public int getVideoId() {
        return this.videoId;
    }
 
    public void setVideoId(int username) {
        this.videoId = videoId;
    }
 
    public String getVideoCode() {
        return this.videoCode;
    }
 
    public void setVideoCode(String videoCode) {
        this.videoCode = videoCode;
    }
 
    public String getVideoTitle() {
        return this.videoTitle;
    }
 
    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

}