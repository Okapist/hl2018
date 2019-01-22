package com.pk.dao;

import com.pk.jsonmodel.Likes;
import com.pk.model.Account;
import com.pk.model.AllLists;
import com.pk.model.PostLists;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.*;

import static com.pk.model.AllLists.usedEmailDomain;

public class EditAccount {

    public HttpResponseStatus edit(int accId, String[] emailParts, byte status, com.pk.jsonmodel.Account jsonAccount) {

        Account toEdit = AllLists.allAccounts[accId];
        if (emailParts != null) {

            int emailIndex = Collections.binarySearch(AllLists.allEmailList, emailParts[0]);
            if(emailIndex < 0) {

                emailIndex = PostLists.newEmails.indexOf(emailParts[0]);
                if(emailIndex == -1) {
                    PostLists.newEmails.add(emailParts[0]);
                    emailIndex = AllLists.allEmailList.size() + PostLists.newEmails.size();
                } else {
                    emailIndex += AllLists.allEmailList.size() + 1;
                }
            }

            Integer domainIndex = Utils.findDomainIndexBinary(emailParts[1].toCharArray());
            if(domainIndex==null || domainIndex < 0) {

                domainIndex = PostLists.newEmailDomains.indexOf(emailParts[1]);
                if(domainIndex == -1) {
                    PostLists.newEmailDomains.add(emailParts[1]);
                    domainIndex = AllLists.domainList.size() + PostLists.newEmailDomains.size();
                } else {
                    domainIndex += AllLists.domainList.size() + 1;
                }
            }

            int hash = domainIndex;
            hash |= emailIndex << 7;
            if(!PostLists.freeEmailDomain.contains(hash) && (Arrays.binarySearch(usedEmailDomain, hash) > -1 || PostLists.usedEmailDomain.contains(hash)))
                return HttpResponseStatus.BAD_REQUEST;

            PostLists.usedEmailDomain.add(hash);
            PostLists.freeEmailDomain.remove(hash);
            PostLists.freeEmailDomain.add(toEdit.emailDomain | toEdit.email<<7);

            toEdit.email = emailIndex;
            toEdit.emailDomain = domainIndex;
        }
/*

        if (status > -1) {
            toEdit.status = status;
        }

        if (jsonAccount.getFname() != null) {
            PostLists.fnames.add(jsonAccount.getFname());
            toEdit.fname = AllLists.fnames.length + PostLists.fnames.size() - 1;
        }

        if (jsonAccount.getSname() != null) {
            PostLists.snames.add(jsonAccount.getSname());
            toEdit.sname = AllLists.snames.length + PostLists.snames.size() - 1;
        }

        if (jsonAccount.getPhone() != null)
            toEdit.phone = jsonAccount.getPhone().toCharArray();

        toEdit.sex = jsonAccount.getSexBoolean();

        if (jsonAccount.getCountry() != null) {
            Short coutryIndex = Utils.findCountryIndexBinary(jsonAccount.getCountry());
            if (coutryIndex == null) {
                if (AllLists.countriesList.size() == 0) {
                    AllLists.countriesList.add("");
                }
                AllLists.countriesList.add(jsonAccount.getCountry());
                coutryIndex = (short) (AllLists.countriesList.size() - 1);
            }
            toEdit.country = coutryIndex;
        }

        if (jsonAccount.getCity() != null) {
            Short citiIndex = Utils.findCityIndexBinary(jsonAccount.getCity());
            if (citiIndex == null) {
                if (AllLists.citiesList.size() == 0) {
                    AllLists.citiesList.add("");
                }
                AllLists.citiesList.add(jsonAccount.getCity());
                citiIndex = (short) (AllLists.citiesList.size() - 1);
            }
            toEdit.city = citiIndex;
        }

        if (jsonAccount.getBirth() != null)
            toEdit.birth = jsonAccount.getBirth();

        if(jsonAccount.getJoined() != null)
            toEdit.joined = jsonAccount.getJoined();

        if (jsonAccount.getPremiumStart() != null)
            toEdit.premiumStart = jsonAccount.getPremiumStart();

        if (jsonAccount.getPremiumEnd() != null)
            toEdit.premiumEnd = jsonAccount.getPremiumEnd();

        if (jsonAccount.getInterests() != null) {
            toEdit.interests = new HashSet<>(jsonAccount.getInterests().length);
            toEdit.interestsArray = new int[jsonAccount.getInterests().length];
            String[] interests = jsonAccount.getInterests();
            for (int i = 0; i < interests.length; i++) {
                String interest = interests[i];
                Integer intId = AllLists.interests.get(interest);
                toEdit.interests.add(intId);
                toEdit.interestsArray[i] = intId;
            }
        }
*/

        boolean goodLikes = addLikes(jsonAccount);
        if(goodLikes) {
            //AllLists.allAccounts[account.id] = account;
            //accIdEdited.add(toEdit.id);
            return HttpResponseStatus.ACCEPTED;
        } else {
            return HttpResponseStatus.BAD_REQUEST;
        }
    }

    private boolean addLikes(com.pk.jsonmodel.Account jsonAccount) {

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
/*

            while (AllLists.likesAccounts.size() <= jsonAccount.getId()) {
                AllLists.likesAccounts.add(null);
            }
*/

            for (int i = 0; i < clearList.size()*2; i+=2) {
                Likes like = clearList.get(i/2);
                int likeId = like.getId();
                int likeTs = like.getTs();

                if(likeId > AllLists.allAccounts.length && AllLists.allAccounts[likeId] == null)
                    return false;

/*
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
*/
            }
        }
        return true;
    }
}
