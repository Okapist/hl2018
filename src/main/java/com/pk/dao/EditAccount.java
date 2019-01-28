package com.pk.dao;

import com.pk.Runner;
import com.pk.model.Account;
import com.pk.model.AllLists;
import com.pk.model.PostLists;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.*;

import static com.pk.model.AllLists.*;
import static com.pk.model.AllLists.MIN_JOINED_YEAR;

public class EditAccount {

    public HttpResponseStatus edit(int accId, com.pk.jsonmodel.Account jsonAccount, byte status, String[] emailParts, List<int[]> likes, int[] premium, List<String> interests) {

        Account account;
        if(!Runner.isWarm)
            account = AllLists.allAccounts[accId];
        else {
            account = new Account();
            account.sex = true;
            account.email = 1;
            account.emailDomain = 2;
            account.id = 1_399_001;
        }

            boolean goodLikes = addLikes(account.id, likes);
        if (!goodLikes) {
            return HttpResponseStatus.BAD_REQUEST;
        }

        if(jsonAccount.getEmail() != null) {
            int emailIndex = Collections.binarySearch(AllLists.allEmailList, emailParts[0]);
            if (emailIndex < 0) {

                emailIndex = PostLists.newEmails.indexOf(emailParts[0]);
                if (emailIndex == -1) {
                    PostLists.newEmails.add(emailParts[0]);
                    emailIndex = AllLists.allEmailList.size() + PostLists.newEmails.size() - 1;
                } else {
                    emailIndex += AllLists.allEmailList.size();
                }
            }

            Integer domainIndex = Utils.findDomainIndexBinary(emailParts[1].toCharArray());
            if (domainIndex == null || domainIndex < 0) {
                PostLists.isNewEmailDomain = true;
                domainIndex = PostLists.newEmailDomains.indexOf(emailParts[1]);
                if (domainIndex == -1) {
                    PostLists.newEmailDomains.add(emailParts[1]);
                    domainIndex = AllLists.domainList.size() + PostLists.newEmailDomains.size() - 1;
                } else {
                    domainIndex += AllLists.domainList.size();
                }
            }

            int hash = domainIndex;
            hash |= emailIndex << 7;
            if (!PostLists.freeEmailDomain.contains(hash) && (Arrays.binarySearch(usedEmailDomain, hash) > -1 || PostLists.usedEmailDomain.contains(hash)))
                return HttpResponseStatus.BAD_REQUEST;

            PostLists.usedEmailDomain.add(hash);

            int oldHash = account.emailDomain;
            oldHash |= account.email << 7;
            PostLists.usedEmailDomain.remove(oldHash);

            if(!Runner.isWarm)
                removeRecommendFilter(account);

            account.email = emailIndex;
            account.emailDomain = domainIndex;
        }

        if(jsonAccount.getEmail() == null && !Runner.isWarm) {
            removeRecommendFilter(account);
        }

        if(!Runner.isWarm)
            removeGroupFilter(account);

        if (jsonAccount.getFname() != null) {

            int oldIndex = Arrays.binarySearch(AllLists.fnames, jsonAccount.getFname().toCharArray(), Utils::compareCharArr);
            if (oldIndex < 0) {
                PostLists.fnames.add(jsonAccount.getFname());
                account.fname = AllLists.fnames.length + PostLists.fnames.size() - 1;
            } else {
                account.fname = oldIndex;
            }
        }

        if (jsonAccount.getSname() != null) {
            int oldIndex = Arrays.binarySearch(AllLists.snames, jsonAccount.getSname().toCharArray(), Utils::compareCharArr);
            if (oldIndex < 0) {
                PostLists.snames.add(jsonAccount.getSname());
                account.sname = AllLists.snames.length + PostLists.snames.size() - 1;
            } else {
                account.sname = oldIndex;
            }
        }

        if(jsonAccount.getPhone() != null) {

            if(account.phone != null) {
                String oldPhone = new String(account.phone);
                String oldPhoneCode = oldPhone.substring(oldPhone.indexOf("(") + 1, oldPhone.indexOf(")"));
                int index = Collections.binarySearch(AllLists.phoneCodeAccounts.get(oldPhoneCode), accId);
                if (index >= 0)
                    AllLists.phoneCodeAccounts.get(oldPhoneCode).remove(index);
            }

            account.phone = jsonAccount.getPhone().toCharArray();

            String phone = jsonAccount.getPhone();
            String phoneCode = phone.substring(phone.indexOf("(") + 1, phone.indexOf(")"));
            account.phoneCode = phoneCode.toCharArray();
            if(!Runner.isWarm)
                AllLists.phoneCodeAccounts.computeIfAbsent(phoneCode, p -> new ArrayList<>());

            int toInsertPos = -Collections.binarySearch(AllLists.phoneCodeAccounts.get(phoneCode), account.id);
            if (toInsertPos < AllLists.phoneCodeAccounts.get(phoneCode).size()-1) {
                if(!Runner.isWarm)
                    AllLists.phoneCodeAccounts.get(phoneCode).add(toInsertPos-1, account.id);
            } else {
                if(!Runner.isWarm)
                    AllLists.phoneCodeAccounts.get(phoneCode).add(account.id);
            }
        }

        if(jsonAccount.getSex() != null)
            account.sex = jsonAccount.getSexBoolean();

        if (jsonAccount.getCountry() != null) {
            Short coutryIndex = Utils.findCountryIndexBinary(jsonAccount.getCountry());
            if (coutryIndex == null) {
                if (AllLists.countriesList.size() == 0) {
                    if(!Runner.isWarm)
                        AllLists.countriesList.add("");
                }
                if(!Runner.isWarm)
                    AllLists.countriesList.add(jsonAccount.getCountry());
                PostLists.isNewCountry = true;
                coutryIndex = (short) (AllLists.countriesList.size() - 1);
            }
            account.country = coutryIndex;
        }

        if (jsonAccount.getCity() != null) {
            Short citiIndex = Utils.findCityIndexBinary(jsonAccount.getCity());
            if (citiIndex == null) {
                if (AllLists.citiesList.size() == 0) {
                    if(!Runner.isWarm)
                        AllLists.citiesList.add("");
                }
                if(!Runner.isWarm)
                    AllLists.citiesList.add(jsonAccount.getCity());
                PostLists.isNewCity = true;
                citiIndex = (short) (AllLists.citiesList.size() - 1);
            }
            account.city = citiIndex;
        }

        if(status > 0)
            account.status = status;

        if (jsonAccount.getBirth() != null)
            account.birth = jsonAccount.getBirth();

        if (jsonAccount.getJoined() != null)
            account.joined = jsonAccount.getJoined();

        if (premium != null) {
            account.premiumStart = premium[0];
            account.premiumEnd = premium[1];
        }

        if (interests != null) {
            account.interests = new HashSet<>(interests.size());
            account.interestsArray = new int[interests.size()];
            for (int i = 0; i < interests.size(); i++) {
                String interest = interests.get(i);
                Integer intId = AllLists.interests.get(interest);
                account.interests.add(intId);
                account.interestsArray[i] = intId;
            }
        }

        if(!Runner.isWarm) {
            createRecommendFilter(account);
            updateGroupFilter(account);
        }
        return HttpResponseStatus.ACCEPTED;
    }

    private boolean addLikes(int id, List<int[]> likes) {

      /*  if (likes != null) {
            for (int i = 0; i < likes.size(); ++i) {
                int[] like = likes.get(i);
                int likeId = like[0];
                int likeTs = like[1];
                int[] postLike = new int[3];
                postLike[0] = id;
                postLike[1] = likeId;
                postLike[2] = likeTs;
                PostLists.newLikes.add(postLike);
            }
        }*/
        return true;
    }

    private void removeRecommendFilter(Account account) {
        if (account == null || account.interestsArray == null)
            return;

        int premiumIndex = account.premiumEnd > Runner.curDate ? 0 : 1;
        int countryIndex = account.country;
        int statusIndex = account.status - 1;
        int cityIndex = account.city;

        if (AllLists.recommendInteresFilter[premiumIndex][statusIndex] == null) {
            AllLists.recommendInteresFilter[premiumIndex][statusIndex] = new HashMap[AllLists.countriesList.size()];
        }

        if (AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex] == null) {
            AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex] = new HashMap<>();
        }

        AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].computeIfAbsent(cityIndex, p -> new HashMap<>());

        //AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(account.id, p -> new int[account.interestsArray.length]);
        AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(account.id, p -> new int[account.interestsArray.length]);

        for (int i = 0; i < account.interestsArray.length; i++) {
            int intId = account.interestsArray[i];

            if(AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(intId) == null) {
                AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(intId, p -> new int[1]);
                AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(intId)[0] = account.id;
            } else {
                int[] oldList = AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(intId);
                for (int i1 = 0; i1 < oldList.length; i1++) {
                    if(oldList[i1] == account.id) {
                        oldList[i1] = 0;
                        break;
                    }
                }
            }
        }
    }

    private void createRecommendFilter(Account account) {
        if (account == null || account.interestsArray == null)
            return;

        int premiumIndex = account.premiumEnd > Runner.curDate ? 0 : 1;
        int countryIndex = account.country;
        int statusIndex = account.status - 1;
        int cityIndex = account.city;

        if (AllLists.recommendInteresFilter[premiumIndex][statusIndex] == null) {
            AllLists.recommendInteresFilter[premiumIndex][statusIndex] = new HashMap[AllLists.countriesList.size()];
        }

        if (AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex] == null) {
            AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex] = new HashMap<>();
        }

        AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].computeIfAbsent(cityIndex, p -> new HashMap<>());

        //AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(account.id, p -> new int[account.interestsArray.length]);
        AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(account.id, p -> new int[account.interestsArray.length]);

        for (int i = 0; i < account.interestsArray.length; i++) {
            int intId = account.interestsArray[i];

            if(AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(intId) == null) {
                AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(intId, p -> new int[1]);
                AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(intId)[0] = account.id;
            } else {

                int[] oldList = AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(intId);
                boolean founded = false;
                for (int i1 = 0; i1 < oldList.length; i1++) {
                    if(oldList[i1] == 0) {
                        oldList[i1] = account.id;
                        founded = true;
                        break;
                    }
                }
                if(!founded) {
                    AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).put(intId, new int[(int) Math.max((oldList.length + 1) * 1.15, oldList.length + 2)]);

                    int[] newList = AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(intId);

                    System.arraycopy(oldList,0,newList,0, oldList.length);
                    newList[oldList.length] = account.id;
                }
            }
        }
    }

    private void removeGroupFilter(Account account) {
        int countryIndex = account.country;
        int sexIndex = account.sex ? 1 : 0;
        int statusIndex = account.status - 1;

        int birth = getYear(account.birth);
        int joined = getYear(account.joined);

        if(groupFilter[countryIndex] == null) {

            groupFilter[countryIndex] = new HashMap<>();
            groupFilterBirth[countryIndex] = new HashMap<>();
            groupFilterJoined[countryIndex] = new HashMap<>();
        }

        groupFilter[countryIndex].computeIfAbsent(account.city, p-> new short[3][2]);
        groupFilterBirth[countryIndex].computeIfAbsent(account.city, p-> new short[3][2][MAX_BIRTH_YEAR - MIN_BIRTH_YEAR + 1]);
        groupFilterJoined[countryIndex].computeIfAbsent(account.city, p-> new short[3][2][MAX_JOINED_YEAR - MIN_JOINED_YEAR + 1]);

        --groupFilter[countryIndex].get(account.city)[statusIndex][sexIndex];

        if(birth >= MIN_BIRTH_YEAR && birth <= MAX_BIRTH_YEAR)
            --groupFilterBirth[countryIndex].get(account.city)[statusIndex][sexIndex][birth-MIN_BIRTH_YEAR];

        if(joined >= MIN_JOINED_YEAR && joined<=MAX_JOINED_YEAR)
            --groupFilterJoined[countryIndex].get(account.city)[statusIndex][sexIndex][joined-MIN_JOINED_YEAR];
    }

    private void updateGroupFilter(Account account) {
        int countryIndex = account.country;
        int sexIndex = account.sex ? 1 : 0;
        int statusIndex = account.status - 1;

        int birth = getYear(account.birth);
        int joined = getYear(account.joined);

        if(groupFilter[countryIndex] == null) {

            groupFilter[countryIndex] = new HashMap<>();
            groupFilterBirth[countryIndex] = new HashMap<>();
            groupFilterJoined[countryIndex] = new HashMap<>();
        }

        groupFilter[countryIndex].computeIfAbsent(account.city, p-> new short[3][2]);
        groupFilterBirth[countryIndex].computeIfAbsent(account.city, p-> new short[3][2][MAX_BIRTH_YEAR - MIN_BIRTH_YEAR + 1]);
        groupFilterJoined[countryIndex].computeIfAbsent(account.city, p-> new short[3][2][MAX_JOINED_YEAR - MIN_JOINED_YEAR + 1]);

        ++groupFilter[countryIndex].get(account.city)[statusIndex][sexIndex];

        if(birth >= MIN_BIRTH_YEAR && birth <= MAX_BIRTH_YEAR)
            ++groupFilterBirth[countryIndex].get(account.city)[statusIndex][sexIndex][birth-MIN_BIRTH_YEAR];

        if(joined >= MIN_JOINED_YEAR && joined<=MAX_JOINED_YEAR)
            ++groupFilterJoined[countryIndex].get(account.city)[statusIndex][sexIndex][joined-MIN_JOINED_YEAR];
    }

    Calendar cal = Calendar.getInstance();
    private int getYear(int timestamp) {
        cal.setTimeInMillis((long)timestamp*1000);
        return cal.get(Calendar.YEAR);
    }

}
