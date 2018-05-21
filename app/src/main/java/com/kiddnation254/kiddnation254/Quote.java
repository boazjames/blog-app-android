package com.kiddnation254.kiddnation254;

public class Quote {
    private int quoteId;
    private String quoteBody;
    private String quoteAuthor;

    public Quote(int quoteId, String quoteBody, String quoteAuthor) {
        this.quoteId = quoteId;
        this.quoteBody = quoteBody;
        this.quoteAuthor = quoteAuthor;
    }
 
    public int getQuoteId() {
        return this.quoteId;
    }
 
    public void setQuoteId(int quoteId) {
        this.quoteId = quoteId;
    }
 
    public String getQuoteBody() {
        return this.quoteBody;
    }
 
    public void setQuoteBody(String quoteBody) {
        this.quoteBody = quoteBody;
    }
 
    public String getQuoteAuthor() {
        return this.quoteAuthor;
    }
 
    public void setQuoteAuthor(String quoteAuthor) {
        this.quoteAuthor = quoteAuthor;
    }

}