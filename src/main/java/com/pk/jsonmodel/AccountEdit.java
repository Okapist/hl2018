package com.pk.jsonmodel;


import com.jsoniter.annotation.JsonProperty;

public class AccountEdit {

    @JsonProperty(required = false)
    public

    Integer id;

    @JsonProperty(required = false)
    public
    Integer birth;

    @JsonProperty(required = false)
    public
    String phone;

    @JsonProperty(required = false)
    public
    String sex;

    @JsonProperty(required = false)
    public
    Integer joined;

    @JsonProperty(required = false)
    public
    String status;

    @JsonProperty(required = false)
    public
    String[] interests;

    @JsonProperty(required = false)
    public
    String email;

    @JsonProperty(required = false)
    public
    Premium premium;

    @JsonProperty(required = false)
    public
    Likes[] likes;

    @JsonProperty(required = false)
    public
    String fname;

    @JsonProperty(required = false)
    public
    String sname;

    @JsonProperty(required = false)
    public
    String country;

    @JsonProperty(required = false)
    public
    String city;
}