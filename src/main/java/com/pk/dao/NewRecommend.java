package com.pk.dao;

import com.pk.Runner;
import com.pk.model.Account;
import com.pk.model.AllLists;

import java.util.*;

public class NewRecommend {

    public boolean recommend(int accId, Short country, Short city, int limit, StringBuilder buf) {

        if(accId >= AllLists.allAccounts.length || AllLists.allAccounts[accId] == null)
            return false;

        if(limit <1)
            return false;

        Account baseAccount = AllLists.allAccounts[accId];
        Set<Integer> baseInterests = baseAccount.interests;
        if(baseInterests == null)
            return true;

        int[] baseInterestsArray = baseAccount.interestsArray;

        BaseFilter filters = new BaseFilter();

        if (country != null) {
            List<Integer> tempCountry = AllLists.countryAccounts.get(country);
            filters.add(tempCountry, 0, tempCountry.size()-1, false);
        }

        if (city != null) {
            List<Integer> tempCity = AllLists.cityAccounts.get(city);
            filters.add(tempCity, 0, tempCity.size()-1, false);
        }

        if(city==null && country == null) {
            int[] tempList = buildRecommend(baseAccount);
            filters.add(tempList, 0, tempList.length-1, false);
        }

        //store in reverse order to remove extra
        PriorityQueue<Account> heap = new PriorityQueue<>(limit, (p1,p2)-> {

            boolean p1Premium = p1.premiumStart!= 0 && p1.premiumEnd!= 0 && p1.premiumStart < Runner.curDate && p1.premiumEnd> Runner.curDate;
            boolean p2Premium = p2.premiumStart!= 0 && p2.premiumEnd!= 0 && p2.premiumStart < Runner.curDate && p2.premiumEnd> Runner.curDate;

            if(p1Premium != p2Premium) {
                return p1Premium?1:-1;
            }

            if(p1.status != p2.status)
                return p2.status - p1.status;

            int common1 = 0;
            int common2 = 0;
            for (int intId : baseInterestsArray) {
                if(p1.interests.contains(intId)) {
                    ++common1;
                }
                if(p2.interests.contains(intId)) {
                    ++common2;
                }
            }

            if(common1 != common2) {
                return common1 - common2;
            }

            return Math.abs(p2.birth-baseAccount.birth) - Math.abs(p1.birth-baseAccount.birth);
        });

        boolean searchPremium = true;
        int searchStatus = 1;
        while(true) {
            filters.resetCounters();
            while (filters.hasNext()) {
                Integer possibleId = filters.next();
                if (possibleId == null || possibleId < 1)
                    continue;

                final Account possible = AllLists.allAccounts[possibleId];

                if (possible.status != searchStatus)
                    continue;

                if (searchPremium) {
                    if (possible.premiumEnd < Runner.curDate || possible.premiumStart > Runner.curDate)
                        continue;
                } else {
                    if (possible.premiumStart < Runner.curDate && possible.premiumEnd > Runner.curDate)
                        continue;
                }

                if (possible.interests == null)
                    continue;

                if (city != null) {
                    if (possible.city == 0 || possible.city != city)
                        continue;
                }

                if (country != null) {
                    if (possible.country == 0 || possible.country != country)
                        continue;
                }

                if (possible.sex == baseAccount.sex)
                    continue;

                if (city != null || country != null) {
                    boolean founded = false;
                    for (int intId : baseInterestsArray) {
                        if (possible.interests.contains(intId)) {
                            founded = true;
                            break;
                        }
                    }
                    if (!founded)
                        continue;
                }

                heap.add(possible);

                while (heap.size() > limit) {
                    heap.poll();
                }
            }

            if(heap.size() >= limit)
                break;

            if(searchStatus<3) {
                ++searchStatus;
            } else {
                if(searchPremium) {
                    searchPremium = false;
                    searchStatus = 1;
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
