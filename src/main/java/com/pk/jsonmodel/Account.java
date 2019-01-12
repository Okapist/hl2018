package com.pk.jsonmodel;

import com.pk.Runner;

public class Account
{
    private Integer id;

    private Integer birth;

    private String phone;

    private String sex;

    private Integer joined;

    private String status;

    private String[] interests;

    private String email;

    private Premium premium;

    private Likes[] likes;

    private String fname;

    private String sname;

    private String country;

    private String city;

    public Integer getId ()
    {
        return id;
    }

    public void setId (Integer id)
    {
        this.id = id;
    }

    public Integer getBirth ()
    {
        return birth;
    }

    public void setBirth (Integer birth)
    {
        this.birth = birth;
    }

    public String getPhone ()
    {
        return phone;
    }

    public void setPhone (String phone)
    {
        this.phone = phone;
    }

    public String getSex ()
    {
        return sex;
    }

    public void setSex (String sex)
    {
        this.sex = sex;
    }

    public Integer getJoined ()
    {
        return joined;
    }

    public void setJoined (Integer joined)
    {
        this.joined = joined;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    public String[] getInterests ()
    {
        return interests;
    }

    public void setInterests (String[] interests)
    {
        this.interests = interests;
    }

    public String getEmail ()
    {
        return email;
    }

    public void setEmail (String email)
    {
        this.email = email;
    }

    public Premium getPremium ()
    {
        return premium;
    }

    public void setPremium (Premium premium)
    {
        this.premium = premium;
    }

    public Likes[] getLikes ()
    {
        return likes;
    }

    public void setLikes (Likes[] likes)
    {
        this.likes = likes;
    }

    public String getFname ()
    {
        return fname;
    }

    public void setFname (String fname)
    {
        this.fname = fname;
    }

    public String getCountry ()
    {
        return country;
    }

    public void setCountry (String country)
    {
        this.country = country;
    }

    public String getCity ()
    {
        return city;
    }

    public void setCity (String city)
    {
        this.city = city;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", birth = "+birth+", phone = "+phone+", sex = "+sex+", joined = "+joined+", status = "+status+", interests = "+interests+", email = "+email+", premium = "+premium+", likes = "+likes+", fname = "+fname+", country = "+country+", city = "+city+"]";
    }

    public Boolean getSexBoolean() {
        return "m".equals(this.sex);
    }

    public Integer getPremiumStart() {
        return getPremium()==null?null:getPremium().getStart();
    }

    public Integer getPremiumEnd() {
        return getPremium()==null?null:getPremium().getFinish();
    }

/*
    public Boolean getPremiumBoolean() {
        return getPremium()!=null&&Integer.parseInt(getPremium().getFinish()) > Runner.curDate;
    }
*/
}