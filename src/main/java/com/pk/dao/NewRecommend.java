package com.pk.dao;

import com.pk.Runner;
import com.pk.model.Account;
import com.pk.model.AllLists;

import java.util.*;

import static com.pk.model.AllLists.allEmailList;
import static com.pk.model.AllLists.fnames;
import static com.pk.model.AllLists.snames;

public class NewRecommend {

    public boolean recommend(int accId, Short country, Short city, int limit, StringBuilder buf) {

        if (accId >= AllLists.allAccounts.length || AllLists.allAccounts[accId] == null)
            return false;

        if (limit < 1)
            return false;

        Account baseAccount = AllLists.allAccounts[accId];
        Set<Integer> baseInterests = baseAccount.interests;
        if (baseInterests == null)
            return true;

        int[] baseInterestsArray = baseAccount.interestsArray;

        int searchCountry = 0;
        if (country != null) {
            searchCountry = country;
        }

        int searchCity = 0;
        if (city != null) {
            searchCity = city;
        }

        //store in reverse order to remove extra
        PriorityQueue<Account> heap = new PriorityQueue<>(limit*3, (p2, p1) -> {

            boolean p1Premium = p1.premiumStart < Runner.curDate && p1.premiumEnd > Runner.curDate;
            boolean p2Premium = p2.premiumStart < Runner.curDate && p2.premiumEnd > Runner.curDate;

            if (p1Premium != p2Premium) {
                return p1Premium ? 1 : -1;
            }

            if (p1.status != p2.status)
                return p2.status - p1.status;

            int common1 = 0;
            int common2 = 0;
            for (int intId : baseInterestsArray) {
                if (p1.interests.contains(intId)) {
                    ++common1;
                }
                if (p2.interests.contains(intId)) {
                    ++common2;
                }
            }

            if (common1 != common2) {
                return common1 - common2;
            }

            return Math.abs(p2.birth - baseAccount.birth) - Math.abs(p1.birth - baseAccount.birth);
        });

        //[premium][status][country]<city><interes><accounts>
        int premium = 0;
        int status = 0;
        final int startCountry = country == null ? 0 : searchCountry;
        final int toAddCountry = searchCountry > 0 ? searchCountry : 1;
        final Set<Integer> alreadyAdded = new HashSet<>(50);

        final PriorityQueue<Integer> addedIndexes = new PriorityQueue<>();

        while (true) {
            final HashMap<Integer, HashMap<Integer, int[]>>[] toSearch = AllLists.recommendInteresFilter[premium][status];
            final int endCountry = searchCountry == 0 ? toSearch.length - 1 : searchCountry;
            for (int curCountry = startCountry; curCountry <= endCountry; curCountry += toAddCountry) {

                if (searchCity != 0) {
                    if(toSearch[curCountry] != null) {
                        final HashMap<Integer, int[]> toSearchInterests = toSearch[curCountry].get(searchCity);
                        if (toSearchInterests != null) {
                            for (int i = 0; i < baseInterestsArray.length; i++) {
                                int interes = baseInterestsArray[i];
                                final int[] possibleList = toSearchInterests.get(interes);
                                if (possibleList != null) {
                                    for (int i1 = 0; i1 < possibleList.length; i1++) {
                                        Integer p = possibleList[i1];
                                        if(p==0)
                                            continue;
                                        Account possible = AllLists.allAccounts[p];
                                        if (possible != null && possible.sex != baseAccount.sex && !alreadyAdded.contains(possible.id)) {
                                            alreadyAdded.add(possible.id);

                                            if(heap.size() >= limit) {
                                                int curIndex = calcCommomIndex(possible, baseAccount);
                                                if(curIndex >= addedIndexes.peek()) {
                                                    if(curIndex > addedIndexes.peek()) {
                                                        addedIndexes.poll();
                                                    }
                                                    addedIndexes.add(curIndex);
                                                    heap.add(possible);
                                                }
                                            } else {
                                                heap.add(possible);
                                                addedIndexes.add(calcCommomIndex(possible, baseAccount));
                                            }
                                        }
                                    }
                                }
                            }
                            alreadyAdded.clear();
                        }
                    }
                } else {
                    if(toSearch[curCountry] != null) {
                        final Set<Integer> cityList = toSearch[curCountry].keySet();
                        final Integer[] cityArr = cityList.toArray(new Integer[0]);
                        for (int i1 = 0; i1 < cityArr.length; i1++) {
                            final Integer curCity = cityArr[i1];
                            final HashMap<Integer, int[]> toSearchInterests = toSearch[curCountry].get(curCity);
                            if (toSearchInterests != null) {
                                for (int i2 = 0; i2 < baseInterestsArray.length; i2++) {
                                    int interes = baseInterestsArray[i2];
                                    final int[] possibleList = toSearchInterests.get(interes);
                                    if (possibleList != null) {
                                        for (int i = 0, possibleListLength = possibleList.length; i < possibleListLength; i++) {
                                            Integer p = possibleList[i];
                                            if(p==0)
                                                continue;
                                            Account possible = AllLists.allAccounts[p];
                                            if (possible != null && possible.sex != baseAccount.sex && !alreadyAdded.contains(possible.id)) {
                                                alreadyAdded.add(possible.id);
                                                if(heap.size() >= limit) {
                                                    int curIndex = calcCommomIndex(possible, baseAccount);
                                                    if(curIndex >= addedIndexes.peek()) {
                                                        if(curIndex > addedIndexes.peek()) {
                                                            addedIndexes.poll();
                                                        }
                                                        addedIndexes.add(curIndex);
                                                        heap.add(possible);
                                                    }
                                                } else {
                                                    heap.add(possible);
                                                    addedIndexes.add(calcCommomIndex(possible, baseAccount));
                                                }
                                            }
                                        }
                                    }
                                }
                                alreadyAdded.clear();
                            }
                        }
                    }
                }
            }

            if (heap.size() >= limit)
                break;

            if (status < 2) {
                ++status;
            } else {
                if (premium == 0) {
                    premium = 1;
                    status = 0;
                } else {
                    break;
                }
            }
        }
        buildResult(heap, limit, buf);
        return true;
    }

    private void buildResult(PriorityQueue<Account> heap, int limit, StringBuilder buf) {

        int added = 0;
        boolean isFirst = true;
        while (!heap.isEmpty() && added < limit) {
            Account cur = heap.poll();
            if (cur.id == 0)
                continue;

            ++added;

            if (isFirst) {
                buf.append("{\"id\":");
                isFirst = false;
            } else
                buf.append(",{\"id\":");

            buf.append(cur.id);

            buf.append(",\"email\":\"");
            buf.append(allEmailList.get(cur.email)).append("@").append(AllLists.domainList.get(cur.emailDomain));
            buf.append("\"");

            buf.append(",\"status\":\"");
            buf.append(cur.getStatusText());
            buf.append("\"");

            if (cur.fname > 0) {
                char[] tmp = fnames[cur.fname];
                buf.append(",\"fname\":\"");
                buf.append(tmp);
                buf.append("\"");
            }

            if (cur.sname > 0) {
                char[] tmp1 = snames[cur.sname];
                buf.append(",\"sname\":\"");
                buf.append(tmp1);
                buf.append("\"");
            }

            buf.append(",\"birth\": ");
            buf.append(cur.birth);

            if (cur.premiumStart > 0 && cur.premiumEnd > 0) {
                buf.append(",\"premium\":{\"start\":");
                buf.append(cur.premiumStart);
                buf.append(",\"finish\":");
                buf.append(cur.premiumEnd);
                buf.append("}");
            }
            buf.append("}");
        }
    }

    private int calcCommomIndex(Account possible, Account base) {

        int index = (Integer.MAX_VALUE - Math.abs(possible.birth - base.birth)) >> 9;

        int common = 0;

        for (int intId : base.interestsArray) {
            if (possible.interests.contains(intId)) {
                ++common;
            }
        }
        boolean isPremium = possible.premiumStart < Runner.curDate && possible.premiumEnd > Runner.curDate;

        index |= common << (32-9);
        index |= (3-possible.status) << (32-4);
        if(isPremium)
            index |= 1 << 30;

         return index;
    }
}
