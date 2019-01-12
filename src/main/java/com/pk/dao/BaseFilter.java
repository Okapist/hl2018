package com.pk.dao;

import com.pk.model.AllLists;

import java.util.ArrayList;
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
    int filterSize = AllLists.allAccounts.length;

    //OR FILTER
    public List<List<Integer>> llaLst = null;
    public List<Integer> startPointerList;
    public List<Integer> endPointerList;
    public List<Integer> curPointers;


    public boolean hasNext() {
        if (isAnyFilters) {

            if(llaLst != null && curPointers == null) {
                curPointers = new ArrayList<>();
                for(int end : endPointerList)
                    curPointers.add(end);
                curPointer = -1;
            }

            if(llaLst != null) {
                for (int i = 0; i < curPointers.size(); i++) {
                    int pointer = curPointers.get(i);
                    if (pointer >= startPointerList.get(i))
                        return true;
                }
                return false;
            } else {
                if (curPointer == Integer.MAX_VALUE)
                    curPointer = endPointer;

                return curPointer >= startPointer;
            }
        } else {
            startPointer = 0;
            endPointer = AllLists.allAccounts.length-1;
            curPointer = endPointer;
            isAnyFilters = true;
            useAll = true;
            return curPointer >= startPointer;
        }
    }

    public Integer next() {
        if(llaLst != null) {
            int curList = getNextPointer();
            int valToReturn = llaLst.get(curList).get(curPointers.get(curList));

            curPointers.set(curList, curPointers.get(curList) - 1);

            return valToReturn;
        }

        if(!useAll) {
            --curPointer;
            if(lla != null) {
                if (lla.get(curPointer + 1) != null)
                    return lla.get(curPointer + 1);
                else
                    return null;
            } else {
                    return ids[curPointer+1];
            }
        } else {
            while (curPointer>= startPointer && AllLists.allAccounts[curPointer] == null)
                --curPointer;

            --curPointer;
            if(curPointer >= startPointer && AllLists.allAccounts[curPointer+1] != null)
                return AllLists.allAccounts[curPointer+1].id;
            else
                return null;
        }
    }

    private int getNextPointer() {

        int maxListIndex = Integer.MIN_VALUE;
        int maxListValue = Integer.MIN_VALUE;
        for (int i = 0; i < llaLst.size(); i++) {
            List<Integer> li = llaLst.get(i);
            if (curPointers.get(i) >= startPointerList.get(i)) {
                if(li.get(curPointers.get(i)) > maxListValue) {
                    maxListValue = li.get(curPointers.get(i));
                    maxListIndex = i;
                }
            }
        }
        return maxListIndex;
    }

    public void add(List<Integer> filter, int start, int end, boolean unsorted) {
        if (filter == null)
            return;

        int totalSize = (end - start) * (unsorted ? 10 : 1);

        if (lla == null && ids == null && llaLst == null) {
            if (totalSize > AllLists.allAccounts.length)
                return;
            isAnyFilters = true;
            lla = filter;
            startPointer = start;
            endPointer = end;
            curPointer = endPointer;
            this.unsorted = unsorted;
            this.filterSize = totalSize;
        } else {
            if (totalSize < this.filterSize) {
                this.filterSize = totalSize;
                lla = filter;
                ids = null;
                llaLst = null;
                startPointer = start;
                endPointer = end;
                curPointer = endPointer;
                this.unsorted = unsorted;
                this.filterSize = totalSize;
            }
        }
    }

    public void addList(List<List<Integer>> filterList, List<Integer> startList, List<Integer> endList, boolean unsorted) {
        if (filterList == null)
            return;

        int totalSize = 0;
        for (int i = 0; i < startList.size(); ++i) {
            int curSize = endList.get(i) - startList.get(i);
            totalSize += curSize;
        }
        if (unsorted)
            totalSize *= 10;


        if (lla == null && ids == null && llaLst == null) {
            if (totalSize > AllLists.allAccounts.length)
                return;
            isAnyFilters = true;
            llaLst = filterList;
            startPointerList = startList;
            endPointerList = endList;
            curPointer = Integer.MAX_VALUE;
            this.unsorted = unsorted;
            this.filterSize = totalSize;
        } else {
            if (totalSize < (this.endPointer - this.startPointer) * unsortedKoef()) {
                lla = null;
                ids = null;
                llaLst = filterList;
                startPointerList = startList;
                endPointerList = endList;
                curPointer = Integer.MAX_VALUE;
                this.unsorted = unsorted;
                this.filterSize = totalSize;
            }
        }
    }

    private int unsortedKoef() {
        return unsorted?10:1;
    }

    public void add(int[] filter, int start, int end, boolean unsorted) {
        if (filter == null)
            return;

        int totalSize = (end - start) * (unsorted ? 10 : 1);

        if (lla == null && ids == null && llaLst == null) {
            if (totalSize > AllLists.allAccounts.length)
                return;
            isAnyFilters = true;
            ids = filter;
            startPointer = start;
            endPointer = end;
            curPointer = endPointer;
            this.unsorted = unsorted;
            this.filterSize = totalSize;
        } else {
            if (totalSize < this.filterSize) {
                this.filterSize = totalSize;
                lla = null;
                ids = filter;
                llaLst = null;
                startPointer = start;
                endPointer = end;
                curPointer = endPointer;
                this.unsorted = unsorted;
                this.filterSize = totalSize;
            }
        }
    }
}
