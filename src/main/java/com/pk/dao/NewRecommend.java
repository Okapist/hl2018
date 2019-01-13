package com.pk.dao;

import com.pk.Runner;
import com.pk.model.Account;
import com.pk.model.AllLists;

import java.util.*;

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
            //searchCity = AllLists.citiesList.indexOf(city);
            int calcCountry = AllLists.cityCounryList[searchCity];
            if (calcCountry != searchCountry && searchCountry != 0)
                return true;

            searchCountry = calcCountry;
        }

        int[] interestsArray = baseAccount.interestsArray;

        //store in reverse order to remove extra
        PriorityQueue<Account> heap = new PriorityQueue<>(limit, (p1, p2) -> {

            boolean p1Premium = p1.premiumStart != 0 && p1.premiumEnd != 0 && p1.premiumStart < Runner.curDate && p1.premiumEnd > Runner.curDate;
            boolean p2Premium = p2.premiumStart != 0 && p2.premiumEnd != 0 && p2.premiumStart < Runner.curDate && p2.premiumEnd > Runner.curDate;

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
        final int startCountry = country==null?0:searchCountry;
        final int toAddCountry = searchCountry>0?searchCountry:1;

        while (true) {
            HashMap<Integer, HashMap<Integer, Set<Integer>>>[] toSearch = AllLists.recommendInteresFilter[premium][status];
            int endCountry = searchCountry == 0 ? toSearch.length - 1 : searchCountry;
            for (int curCountry = startCountry; curCountry <= endCountry; curCountry+=toAddCountry) {

                if (searchCity != 0) {
                    HashMap<Integer, Set<Integer>> toSearchInterests = toSearch[curCountry].get(searchCity);
                    if (toSearchInterests != null) {
                        Set<Integer> alreadyAdded = new HashSet<>();
                        for (int interes : baseInterestsArray) {
                            Set<Integer> possibleList = toSearchInterests.get(interes);
                            if (possibleList != null) {
                                possibleList.forEach(p -> {
                                    Account possible = AllLists.allAccounts[p];
                                    if (possible.sex != baseAccount.sex && !alreadyAdded.contains(possible.id)) {
                                        alreadyAdded.add(possible.id);
                                        heap.add(possible);
                                    }
                                });
                                while (heap.size() > limit) {
                                    heap.poll();
                                }
                            }
                        }
                    }
                } else {
                    for (Integer curCity : toSearch[curCountry].keySet()) {
                        HashMap<Integer, Set<Integer>> toSearchInterests = toSearch[curCountry].get(curCity);
                        if (toSearchInterests != null) {
                            Set<Integer> alreadyAdded = new HashSet<>();
                            for (int interes : baseInterestsArray) {
                                Set<Integer> possibleList = toSearchInterests.get(interes);
                                if (possibleList != null) {
                                    possibleList.forEach(p -> {
                                        Account possible = AllLists.allAccounts[p];
                                        if (possible.sex != baseAccount.sex && !alreadyAdded.contains(possible.id)) {
                                            alreadyAdded.add(possible.id);
                                            heap.add(possible);
                                        }
                                    });
                                    while (heap.size() > limit) {
                                        Account acc = heap.poll();
                                        int i = 0;
                                    }
                                }
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

        while (heap.size() > limit) {
            heap.poll();
        }

        buildResult(heap, limit, buf);
        return true;
    }

    private void buildResult(PriorityQueue<Account> heap, int limit, StringBuilder buf) {

        Account[] arr = new Account[heap.size()];
        for(int i=0; i<arr.length; ++i)
            arr[i] = heap.poll();

        boolean isFirst = true;
        for(int i= arr.length-1; i>=0; --i) {
            Account cur = arr[i];
            if(cur.id < 1)
                continue;

            if(isFirst) {
                buf.append("{\"id\":");
                isFirst = false;
            } else
                buf.append(",{\"id\":");

            buf.append(cur.id);

            if(cur.email != null) {
                buf.append(",\"email\":\"");
                buf.append(cur.email).append("@").append(AllLists.domainList.get(cur.emailDomain));
                buf.append("\"");
            }

            buf.append(",\"status\":\"");
            buf.append(cur.getStatusText());
            buf.append("\"");

            char[] tmp = cur.fname;
            if (tmp != null) {
                buf.append(",\"fname\":\"");
                buf.append(tmp);
                buf.append("\"");
            }
            char[] tmp1 = cur.sname;
            if (tmp1 != null) {
                buf.append(",\"sname\":\"");
                buf.append(tmp1);
                buf.append("\"");
            }

            buf.append(",\"birth\": ");
            buf.append(cur.birth);

            Integer premStart = cur.premiumStart;
            Integer premEnd = cur.premiumEnd;
            if (premStart!=null && premEnd!=null && premStart > 0 && premEnd > 0) {
                buf.append(",\"premium\":{\"start\":");
                buf.append(premStart);
                buf.append(",\"finish\":");
                buf.append(premEnd);
                buf.append("}");
            }

            buf.append("}");
            --limit;
        }
    }

    private int[] buildRecommend(Account account) {
        //List<Integer> res = new ArrayList<>(10000);
        Set<Integer> res = new TreeSet<>();

        for (Integer interest : account.interestsArray) {
            List<Integer> tempArr = AllLists.interestAccounts.get(interest);
            res.addAll(tempArr);
        }
        return res.stream().mapToInt(Integer::intValue).toArray();
    }
}
