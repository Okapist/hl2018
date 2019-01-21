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

    public HttpResponseStatus create(com.pk.jsonmodel.Account jsonAccount, byte status, String[] emailParts, List<int[]> likes, int[] premium, List<String> interests) {

        Account account = new Account();
        account.id = jsonAccount.getId();

        int emailIndex = Collections.binarySearch(AllLists.allEmailList, emailParts[0]);
        if(emailIndex < 0) {

            emailIndex = PostLists.newEmails.indexOf(emailParts[0]);
            if(emailIndex == -1) {
                PostLists.newEmails.add(emailParts[0]);
                emailIndex = AllLists.allEmailList.size() + PostLists.newEmails.size();
            } else {
                emailIndex += AllLists.allEmailList.size();
            }
        }

        Integer domainIndex = Utils.findDomainIndexBinary(emailParts[1].toCharArray());
        if(domainIndex==null || domainIndex < 0) {

            domainIndex = PostLists.newEmailDomains.indexOf(emailParts[1]);
            if(domainIndex == -1) {
                PostLists.newEmailDomains.add(emailParts[1]);
                domainIndex = AllLists.domainList.size() + PostLists.newEmailDomains.size();
            } else {
                domainIndex += AllLists.domainList.size();
            }
        }


        int hash = domainIndex;
        hash |= emailIndex<<7;
        if(!PostLists.freeEmailDomain.contains(hash) && (Arrays.binarySearch(usedEmailDomain, hash) > -1 || PostLists.usedEmailDomain.contains(hash)))
            return HttpResponseStatus.BAD_REQUEST;

        PostLists.usedEmailDomain.add(hash);

        account.email = emailIndex;
        account.emailDomain = domainIndex;


        if(jsonAccount.getFname() != null) {

            int oldIndex = Arrays.binarySearch(AllLists.fnames, jsonAccount.getFname().toCharArray(), Utils::compareCharArr);
            if(oldIndex < 0) {
                PostLists.fnames.add(jsonAccount.getFname());
                account.fname = AllLists.fnames.length + PostLists.fnames.size();
            } else {
                account.fname = oldIndex;
            }
        }

        if(jsonAccount.getSname() != null) {
            int oldIndex = Arrays.binarySearch(AllLists.snames, jsonAccount.getSname().toCharArray(), Utils::compareCharArr);
            if (oldIndex < 0) {
                PostLists.snames.add(jsonAccount.getSname());
                account.sname = AllLists.snames.length + PostLists.snames.size();
            } else {
                account.sname = oldIndex;
            }
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

        account.status = status;

        if(jsonAccount.getBirth() != null)
            account.birth = jsonAccount.getBirth();

        if(jsonAccount.getJoined() != null)
            account.joined = jsonAccount.getJoined();

        if(premium != null) {
            account.premiumStart = premium[0];
            account.premiumEnd = premium[1];
        }

        if(interests != null) {
            account.interests = new HashSet<>(interests.size());
            account.interestsArray = new int[interests.size()];
            for (int i = 0; i < interests.size(); i++) {
                String interest = interests.get(i);
                Integer intId = AllLists.interests.get(interest);
                account.interests.add(intId);
                account.interestsArray[i] = intId;
            }
        }


        boolean goodLikes = addLikes(account.id, likes);
        if(goodLikes) {
            AllLists.allAccounts[account.id] = account;
            accIdAdded.add(account.id);
            return HttpResponseStatus.CREATED;
        } else {
            return HttpResponseStatus.BAD_REQUEST;
        }
    }

    private boolean addLikes(int id, List<int[]> likes) {

        if (likes != null) {

            Collections.sort(likes, (p1, p2) -> {
                return Integer.compare(p2[0], p1[0]);
            });

            List<int[]> clearList = new ArrayList<>();
            int prevLikeId = Integer.MIN_VALUE;
            int totalSame = 0;
            long totalTs = 0;
            for (int[] like : likes) {
                if (like[0] != prevLikeId) {
                    if (totalSame != 0) {

                        clearList.get(clearList.size()-1)[1] = (int)((clearList.get(clearList.size()-1)[1] + totalTs) / (totalSame+1));
                        totalSame = 0;
                        totalTs = 0;
                    }
                    clearList.add(like);
                    prevLikeId = like[0];
                } else {
                    ++totalSame;
                    totalTs += like[0];
                }
            }

            for (int i = 0; i < clearList.size()*2; i+=2) {
                int[] like = clearList.get(i/2);
                int likeId = like[0];

                if(likeId > AllLists.allAccounts.length && AllLists.allAccounts[likeId] == null)
                    return false;
            }

            for (int i = 0; i < clearList.size()*2; i+=2) {
                int[] like = clearList.get(i/2);
                int likeId = like[0];
                int likeTs = like[1];
                int[] postLike = new int[3];
                postLike[0] = id;
                postLike[1] = likeId;
                postLike[2] = likeTs;
                PostLists.newLikes.add(postLike);
            }

        }
        return true;
    }
}
