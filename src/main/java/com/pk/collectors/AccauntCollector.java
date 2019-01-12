package com.pk.collectors;

import com.pk.model.Account;

import java.util.Comparator;
import java.util.PriorityQueue;

public class AccauntCollector {

    private final int limit;
    private PriorityQueue<Account> pq;

    public AccauntCollector(int limit) {
        pq = new PriorityQueue<>(limit, Comparator.comparingInt(p -> p.id));
        this.limit = limit;
    }

    public void add(Account account) {
        pq.add(account);
        if(pq.size() > limit)
            pq.poll();
    }

    public Account poll() {
        return pq.poll();
    }

    public int size() {
        return pq.size();
    }

}
