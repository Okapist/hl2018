package com.pk.dao;

import com.pk.model.AllLists;

import java.util.List;

public class BaseFilter {

    public List<Integer> lla = null;
    public int[] ids = null;
    public int startPointer;
    public int endPointer;
    int curPointer = Integer.MAX_VALUE;
    boolean useAll = false;
    public boolean unsorted = false;

    boolean isAnyFilters = false;

    public boolean hasNext() {
        if (isAnyFilters) {
            return curPointer > startPointer;
        } else {
            startPointer = 0;
            endPointer = AllLists.allAccounts.length-1;
            curPointer = endPointer;
            isAnyFilters = true;
            useAll = true;
            return curPointer > startPointer;
        }
    }

    public Integer next() {
        if(!useAll) {
            return lla != null ? lla.get(curPointer--) : ids[curPointer--];
        } else {
            while (AllLists.allAccounts[curPointer] == null)
                --curPointer;

            return AllLists.allAccounts[curPointer--].id;
        }
    }

    public void add(List<Integer> filter, int start, int end, boolean unsorted) {
        if(filter == null)
            return;

        if(lla == null && ids == null) {
            if(unsorted) {
                if((end-start) * 10 > AllLists.allAccounts.length)
                    return;
            }
            isAnyFilters = true;
            lla = filter;
            startPointer = start;
            endPointer = end;
            curPointer = endPointer;
            this.unsorted = unsorted;
        } else {
            if (lla != null) {
                if ((end-start) * (unsorted?10:0) < (this.endPointer-this.startPointer) * unsortedKoef()) {
                    lla = filter;
                    startPointer = start;
                    endPointer = end;
                    curPointer = endPointer;
                    this.unsorted = unsorted;
                }
            }

            if (ids != null) {
                if ((end-start)* (unsorted?10:0) < (this.endPointer-this.startPointer)* unsortedKoef()) {
                    lla = filter;
                    ids = null;
                    startPointer = start;
                    endPointer = end;
                    curPointer = endPointer;
                    this.unsorted = unsorted;
                }
            }
        }
    }

    private int unsortedKoef() {
        return unsorted?10:1;
    }

    public void add(int[] filter, int start, int end, boolean unsorted) {
        if(filter == null)
            return;

        isAnyFilters = true;
        if(lla == null && ids == null) {
            ids = filter;
            startPointer = start;
            endPointer = end;
            curPointer = endPointer;
            this.unsorted = unsorted;
        } else {
            if (lla != null) {
                if ((end-start)* (unsorted?10:0) < (this.endPointer-this.startPointer)* unsortedKoef()) {
                    lla = null;
                    ids = filter;
                    startPointer = start;
                    endPointer = end;
                    curPointer = endPointer;
                    this.unsorted = unsorted;
                }
            }

            if (ids != null) {
                if ((end-start)* (unsorted?10:0) < (this.endPointer-this.startPointer)* unsortedKoef()) {
                    ids = filter;
                    startPointer = start;
                    endPointer = end;
                    curPointer = endPointer;
                    this.unsorted = unsorted;
                }
            }
        }
    }
}
