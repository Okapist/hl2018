package com.pk.dao;

import com.pk.model.Account;
import com.pk.model.AllLists;

import java.util.*;
import java.util.stream.Collectors;

import static com.pk.model.AllLists.*;


public class NewSuggest {

    private static final Float ZERO_FLOAT = new Float(0);

    public boolean suggest(int accId, Short country, Short city, int limit, StringBuilder buf) {

        if(accId >= AllLists.allAccounts.length || AllLists.allAccounts[accId] == null)
            return false;

        if (limit < 1)
            return false;

        int[] baseLikes = AllLists.likesAccounts.get(accId);

        if (baseLikes == null)
            return true;

        HashMap<Integer, Float> commonLike = new HashMap<>(50);
        for (int i = 0; i < baseLikes.length; i += 2) {
            int likeId = baseLikes[i];
            int likeTs = baseLikes[i + 1];
            if (likeId == 0)
                continue;
            int[] tmp = AllLists.likesTO[likeId];
            for (int accountId : tmp) {

                if (country != null) {
                    if (AllLists.allAccounts[accountId].country == 0 ||
                            AllLists.allAccounts[accountId].country != country)
                        continue;
                }
                if (city != null) {
                    if (AllLists.allAccounts[accountId].city == 0 ||
                            AllLists.allAccounts[accountId].city != city)
                        continue;
                }

                int[] otherLikesArray = AllLists.likesAccounts.get(accountId);
                float diff = 0;
                for (int j = 0; j < otherLikesArray.length; j += 2) {
                    int otherLikeId = otherLikesArray[j];
                    int otherLikeTs = otherLikesArray[j + 1];
                    if (otherLikeId != likeId)
                        continue;

                    int diffTs = otherLikeTs - likeTs;
                    if (diffTs == 0)
                        diff += 1;
                    else
                        diff += (float) (1.0 / (Math.abs(diffTs)));
                }
                commonLike.put(accountId, commonLike.getOrDefault(accountId, ZERO_FLOAT) + diff);
            }
        }

        LinkedHashMap<Integer, Float> commonLikeSortedByValue = commonLike.entrySet().stream()
                .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        int resultSize = 0;
        boolean isFirst = true;
        for (Integer key : commonLikeSortedByValue.keySet()) {
            if (limit == resultSize)
                break;
            Account search = AllLists.allAccounts[key];
            int[] searchLikes = AllLists.likesAccounts.get(search.id);
            for (int i = 0; i < searchLikes.length; i += 2) {

                if (limit == resultSize)
                    break;

                int likeId = searchLikes[i];

                Account cur = AllLists.allAccounts[likeId];
                if(cur.id < 1)
                    continue;

                if (Utils.likeToContainsId(baseLikes, cur.id))
                    continue;

                if(isFirst) {
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

                if(cur.fname > 0) {
                    char[] tmp = fnames[cur.fname];
                    buf.append(",\"fname\":\"");
                    buf.append(tmp);
                    buf.append("\"");
                }

                if(cur.sname > 0) {
                    char[] tmp1 = snames[cur.sname];
                    buf.append(",\"sname\":\"");
                    buf.append(tmp1);
                    buf.append("\"");
                }

                buf.append("}");
                ++resultSize;
            }
        }
        return true;
    }
}
