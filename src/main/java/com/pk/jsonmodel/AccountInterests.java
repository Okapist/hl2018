package com.pk.jsonmodel;

public class AccountInterests {

    private int accountId;
    private int interestId;

    public AccountInterests(Integer accountId, Integer interestId) {
        this.accountId = accountId;
        this.interestId = interestId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getInterestId() {
        return interestId;
    }

    public void setInterestId(int interestId) {
        this.interestId = interestId;
    }
}
