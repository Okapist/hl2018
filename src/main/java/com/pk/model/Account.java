package com.pk.model;

import com.pk.dao.Utils;

import java.util.Set;

public class Account
{
    public int id;

    public int birth;

    public char[] phone;

    public char[] phoneCode;

    public boolean sex;

    public int joined;

    public byte status; //1-свободны 2-всё сложно 3-заняты

    public Set<Integer> interests;
    public int[] interestsArray;

    public int email;

    public int emailDomain;

    public int fname;

    public int sname;

    public short country;

    public short city;

    public int premiumStart;
    public int premiumEnd;

    public String getStatusText() {
        return Utils.getStatusText(status);
    }

    @Override
    public boolean equals(Object obj) {
        return ((Account)obj).id == this.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}