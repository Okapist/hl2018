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

        //store in reverse order to remove extra
        PriorityQueue<Account> heap = new PriorityQueue<>(limit, (p1,p2)-> {

            boolean p1Premium = p1.premiumStart!= 0 && p1.premiumEnd!= 0 && p1.premiumStart < Runner.curDate && p1.premiumEnd> Runner.curDate;
            boolean p2Premium = p2.premiumStart!= 0 && p2.premiumEnd!= 0 && p2.premiumStart < Runner.curDate && p2.premiumEnd> Runner.curDate;

            if(p1Premium != p2Premium) {
                return p1Premium?1:-1;
            }

            if(p1.status != p2.status)
                return Integer.compare(p2.status, p1.status);

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

/*
            if(commonInt.get(p1.id) != commonInt.get(p2.id)) {
               return Integer.compare(commonInt.get(p1.id), commonInt.get(p2.id));
            }
*/

            return Integer.compare(Math.abs(p2.birth-baseAccount.birth), Math.abs(p1.birth-baseAccount.birth));

        });

        while (filters.hasNext()) {
            Integer possibleId = filters.next();//1299980
            if(possibleId==null || possibleId < 1)
                continue;

            final Account possible = AllLists.allAccounts[possibleId];
            if(possible == null || possible.id < 1 || possible.interests == null)
                continue;

            if(city != null) {
                if(possible.city ==0 || possible.city != city)
                    continue;
            }

            if(country != null) {
                if(possible.country == 0 || possible.country != country)
                    continue;
            }

            if(possible.sex == baseAccount.sex)
                continue;

            boolean founded = false;
            //if(commonInt.get(possible.id) == null) {
                for (int intId : baseInterestsArray) {
                    if(possible.interests.contains(intId)) {
                        founded = true;
                        break;
                    }
                }
                //if (common > 0)
                    //commonInt.put(possible.id, common);
            //}
            if(!founded)
                continue;

            heap.add(possible);

            while (heap.size() > limit) {
                Account extra = heap.poll();
                //commonInt.remove(extra.id);
            }
        }

        while (heap.size() > limit) {
            //Account extra =
                    heap.poll();
            //commonInt.remove(extra.id);
        }

        //commonInt.clear();
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
}
