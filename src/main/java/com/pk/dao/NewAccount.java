package com.pk.dao;

import com.pk.jsonmodel.Likes;
import com.pk.model.Account;
import com.pk.model.AllLists;
import com.pk.model.PostLists;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.*;

import static com.pk.model.AllLists.usedEmailDomain;
import static com.pk.model.PostLists.accIdAdded;

public class NewAccount {

    public HttpResponseStatus create(com.pk.jsonmodel.Account jsonAccount) {

        String[] emailParts = jsonAccount.getEmail().split("@");

        Account account = new Account();
        account.id = jsonAccount.getId();

        int emailIndex = Collections.binarySearch(AllLists.allEmailList, emailParts[0]);
        Integer domainIndex = Utils.findDomainIndexBinary(emailParts[1].toCharArray());

        int hash = domainIndex;
        hash |= emailIndex<<7;
        if(!PostLists.freeEmailDomain.contains(hash) && (Arrays.binarySearch(usedEmailDomain, hash) > -1 || PostLists.usedEmailDomain.contains(hash)))
            return HttpResponseStatus.BAD_REQUEST;

        PostLists.usedEmailDomain.add(hash);

        account.email = emailIndex;
        account.emailDomain = domainIndex;

        if(jsonAccount.getFname() != null) {
            PostLists.fnames.add(jsonAccount.getFname());
            account.fname = AllLists.fnames.length + PostLists.fnames.size() - 1;
        }

        if(jsonAccount.getSname() != null) {
            PostLists.snames.add(jsonAccount.getSname());
            account.sname = AllLists.snames.length + PostLists.snames.size() - 1;
        }

        if(jsonAccount.getPhone() != null)
            account.phone = jsonAccount.getPhone().toCharArray();

        account.sex = jsonAccount.getSexBoolean();

        if(jsonAccount.getCountry() != null) {
            Short coutryIndex = Utils.findCountryIndexBinary(jsonAccount.getCountry());
            if(coutryIndex == null) {
                if(AllLists.countriesList.size() ==0) {
                    AllLists.countriesList.add("");
                }
                AllLists.countriesList.add(jsonAccount.getCountry());
                coutryIndex = (short)(AllLists.countriesList.size()-1);
            }
            account.country = coutryIndex;
        }

        if(jsonAccount.getCity() != null) {
            Short citiIndex = Utils.findCityIndexBinary(jsonAccount.getCity());
            if(citiIndex == null) {
                if(AllLists.citiesList.size() ==0) {
                    AllLists.citiesList.add("");
                }
                AllLists.citiesList.add(jsonAccount.getCity());
                citiIndex = (short)(AllLists.citiesList.size()-1);
            }
            account.city = citiIndex;
        }

        switch (jsonAccount.getStatus()) {
            case "свободны":
                account.status = 1;
                break;
            case "всё сложно":
                account.status = 2;
                break;
            case "заняты":
                account.status = 3;
                break;
        }
        if(jsonAccount.getBirth() != null)
            account.birth = jsonAccount.getBirth();

        account.joined = jsonAccount.getJoined();

        if(jsonAccount.getPremiumStart() != null)
            account.premiumStart = jsonAccount.getPremiumStart();

        if(jsonAccount.getPremiumEnd() != null)
            account.premiumEnd = jsonAccount.getPremiumEnd();

        AllLists.allAccounts[account.id] = account;
/*
        if(jsonAccount.getInterests() != null) {
            account.interests = new HashSet<>(jsonAccount.getInterests().length);
            account.interestsArray = new int[jsonAccount.getInterests().length];
            String[] interests = jsonAccount.getInterests();
            for (int i = 0; i < interests.length; i++) {
                String interest = interests[i];
                Integer intId = AllLists.interests.get(interest);
                account.interests.add(intId);
                account.interestsArray[i] = intId;
            }
        }
        addLikes(jsonAccount);
*/
        accIdAdded.add(account.id);
        return HttpResponseStatus.CREATED;
    }

    private void addLikes(com.pk.jsonmodel.Account jsonAccount) {

        if (jsonAccount.getLikes() != null) {

            Arrays.sort(jsonAccount.getLikes(), (p1, p2) -> {
                return Integer.compare(p2.getId(), p1.getId());
            });

            List<Likes> clearList = new ArrayList<>();
            int prevLikeId = Integer.MIN_VALUE;
            int totalSame = 0;
            long totalTs = 0;
            for (Likes like : jsonAccount.getLikes()) {
                if (like.getId() != prevLikeId) {
                    if (totalSame != 0) {
                        clearList.get(clearList.size()-1).setTs((int)((clearList.get(clearList.size()-1).getTs() + totalTs) / (totalSame+1)));
                        totalSame = 0;
                        totalTs = 0;
                    }
                    clearList.add(like);
                    prevLikeId = like.getId();
                } else {
                    ++totalSame;
                    totalTs += like.getTs();
                }
            }

            while (AllLists.likesAccounts.size() <= jsonAccount.getId()) {
                AllLists.likesAccounts.add(null);
            }

            for (int i = 0; i < clearList.size()*2; i+=2) {
                Likes like = clearList.get(i/2);
                int likeId = like.getId();
                int likeTs = like.getTs();

                if(AllLists.likesAccounts.get(jsonAccount.getId()) == null) {
                    AllLists.likesAccounts.set(jsonAccount.getId(), new int[clearList.size()*2]);
                }

                AllLists.likesAccounts.get(jsonAccount.getId())[i] = likeId;
                AllLists.likesAccounts.get(jsonAccount.getId())[i+1] = likeTs;

                while (AllLists.likesTO.size() <= likeId)
                    AllLists.likesTO.add(null);

                if(AllLists.likesTO.get(likeId) == null) {
                    AllLists.likesTO.set(likeId, new ArrayList<>());
                }

                AllLists.likesTO.get(likeId).add(jsonAccount.getId());
            }
        }
    }


}
