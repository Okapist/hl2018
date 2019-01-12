package com.pk.jsonmodel;

public class Interest {

    private int id;
    private String interest;

    public Interest(int id, String interest) {
        this.id = id;
        this.interest = interest;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

}
