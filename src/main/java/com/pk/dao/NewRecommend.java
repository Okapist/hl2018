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
        PriorityQueue<Account> heap = new PriorityQueue<>(limit, (p2, p1) -> {

            boolean p1Premium = p1.premiumStart != 0 && p1.premiumEnd != 0 && p1.premiumStart < Runner.curDate && p1.premiumEnd > Runner.curDate;
            boolean p2Premium = p2.premiumStart != 0 && p2.premiumEnd != 0 && p2.premiumStart < Runner.curDate && p2.premiumEnd > Runner.curDate;

            if (p1Premium != p2Premium) {
                return p1Premium ? 1 : -1;
            }

            if (p1.status != p2.status)
                return p2.status - p1.status;

            int common1 = 0;
            int common2 = 0;
            for (int i = 0; i < baseInterestsArray.length; i++) {
                int intId = baseInterestsArray[i];
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
                                        Account possible = AllLists.allAccounts[p];
                                        if (possible.sex != baseAccount.sex && !alreadyAdded.contains(possible.id)) {
                                            alreadyAdded.add(possible.id);
                                            heap.add(possible);
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
                                            Account possible = AllLists.allAccounts[p];
                                            if (possible.sex != baseAccount.sex && !alreadyAdded.contains(possible.id)) {
                                                alreadyAdded.add(possible.id);
                                                heap.add(possible);
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

        //Account[] arr = new Account[heap.size()];
        //for (int i = 0; i < arr.length; ++i)
            //arr[i] = heap.poll();
        int added = 0;
        boolean isFirst = true;
        while(!heap.isEmpty() && added<limit) {
        //for (int i = arr.length - 1; i >= 0; --i) {
            Account cur = heap.poll();
            if (cur.id < 1)
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

            Integer premStart = cur.premiumStart;
            Integer premEnd = cur.premiumEnd;
            if (premStart != null && premEnd != null && premStart > 0 && premEnd > 0) {
                buf.append(",\"premium\":{\"start\":");
                buf.append(premStart);
                buf.append(",\"finish\":");
                buf.append(premEnd);
                buf.append("}");
            }

            buf.append("}");
        }
    }
}
