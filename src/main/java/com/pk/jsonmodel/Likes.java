package com.pk.jsonmodel;

import com.jsoniter.annotation.JsonIgnore;

public class Likes
{
    @JsonIgnore
    private Integer from;

    private Integer id;

    private Integer ts;

    public Integer getId ()
    {
        return id;
    }

    public void setId (Integer id)
    {
        this.id = id;
    }

    public Integer getTs ()
    {
        return ts;
    }

    public void setTs (Integer ts)
    {
        this.ts = ts;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", ts = "+ts+"]";
    }

    @JsonIgnore
    public Integer getFrom() {
        return from;
    }

    @JsonIgnore
    public void setFrom(Integer from) {
        this.from = from;
    }
}