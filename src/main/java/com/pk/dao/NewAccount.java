package com.pk.dao;

import com.pk.Runner;
import com.pk.model.Account;
import com.pk.model.AllLists;
import com.pk.model.PostLists;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.*;

import static com.pk.model.AllLists.*;

public class NewAccount {

    public HttpResponseStatus create(com.pk.jsonmodel.Account jsonAccount, byte status, String[] emailParts, List<int[]> likes, int[] premium, List<String> interests) {

        Account account = new Account();
        account.id = jsonAccount.getId();

        if(likes != null) {
            if(!addLikesHelper(likes))
                return HttpResponseStatus.BAD_REQUEST;
        }

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
        hash |= emailIndex << 8;
        if (Arrays.binarySearch(usedEmailDomain, hash) > -1 || PostLists.usedEmailDomain.contains(hash))
            return HttpResponseStatus.BAD_REQUEST;

        PostLists.usedEmailDomain.add(hash);

        account.email = emailIndex;
        account.emailDomain = domainIndex;


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

        if (jsonAccount.getPhone() != null) {
            account.phone = jsonAccount.getPhone().toCharArray();

            String phone = jsonAccount.getPhone();
            String phoneCode = phone.substring(phone.indexOf("(") + 1, phone.indexOf(")"));
            account.phoneCode = phoneCode.toCharArray();
            AllLists.phoneCodeAccounts.computeIfAbsent(phoneCode, p -> new ArrayList<>());
            int toInsertPos = -Collections.binarySearch(AllLists.phoneCodeAccounts.get(phoneCode), account.id);
            if (toInsertPos < AllLists.phoneCodeAccounts.get(phoneCode).size() - 1) {
                if (!Runner.isWarm)
                    AllLists.phoneCodeAccounts.get(phoneCode).add(toInsertPos - 1, account.id);
            } else {
                if (!Runner.isWarm)
                    AllLists.phoneCodeAccounts.get(phoneCode).add(account.id);
            }
        } else {
            if (!Runner.isWarm)
                AllLists.phoneCodeAccounts.computeIfAbsent(null, p -> new ArrayList<>());
            if (!Runner.isWarm)
                AllLists.phoneCodeAccounts.get(null).add(account.id);
        }

        account.sex = jsonAccount.getSexBoolean();

        if (jsonAccount.getCountry() != null) {
            Short coutryIndex = Utils.findCountryIndexBinary(jsonAccount.getCountry());
            if (coutryIndex == null) {
                if (AllLists.countriesList.size() == 0) {
                    AllLists.countriesList.add("");
                }
                PostLists.isNewCountry = true;
                if (!Runner.isWarm)
                    AllLists.countriesList.add(jsonAccount.getCountry());
                coutryIndex = (short) (AllLists.countriesList.size() - 1);
            }
            account.country = coutryIndex;
        }

        if (jsonAccount.getCity() != null) {
            Short citiIndex = Utils.findCityIndexBinary(jsonAccount.getCity());
            if (citiIndex == null) {
                if (AllLists.citiesList.size() == 0) {
                    AllLists.citiesList.add("");
                }
                if (!Runner.isWarm)
                    AllLists.citiesList.add(jsonAccount.getCity());
                PostLists.isNewCity = true;
                citiIndex = (short) (AllLists.citiesList.size() - 1);
            }
            account.city = citiIndex;
        }

        account.status = status;

        if (jsonAccount.getBirth() != null) {
            account.birth = jsonAccount.getBirth();
        }

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

        if (!Runner.isWarm) {
            AllLists.allAccounts[account.id] = account;
            createRecommendFilter(account);
            updateGroupFilter(account);
        }

        return HttpResponseStatus.CREATED;
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
            ++groupFilterBirth[countryIndex].get(account.city)[statusIndex][sexIndex][birth - MIN_BIRTH_YEAR];

        if(joined >= MIN_JOINED_YEAR && joined<=MAX_JOINED_YEAR)
            ++groupFilterJoined[countryIndex].get(account.city)[statusIndex][sexIndex][joined-MIN_JOINED_YEAR];

        if(birth >= MIN_BIRTH_YEAR && birth <= MAX_BIRTH_YEAR && account.interestsArray != null) {
            for (int interestId : account.interestsArray) {

                if(groupFilterBirthCityInterests[birth-MIN_BIRTH_YEAR][account.city] == null)
                    groupFilterBirthCityInterests[birth-MIN_BIRTH_YEAR][account.city] = new short[interestsById.size() + 1];

                if(groupFilterBirthCountryInterests[birth-MIN_BIRTH_YEAR][account.country] == null)
                    groupFilterBirthCountryInterests[birth-MIN_BIRTH_YEAR][account.country] = new short[interestsById.size() + 1];

                ++groupFilterBirthCityInterests[birth-MIN_BIRTH_YEAR][account.city][interestId];
                ++groupFilterBirthCityInterests[birth-MIN_BIRTH_YEAR][account.country][interestId];
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

    private final Calendar cal = Calendar.getInstance();
    private int getYear(int timestamp) {
        cal.setTimeInMillis((long)timestamp*1000);
        return cal.get(Calendar.YEAR);
    }

    private boolean addLikesHelper(List<int[]> likes) {

        if (likes != null) {

            for (int i = 0; i < likes.size(); ++i) {
                int[] like = likes.get(i);
                int likeToId = like[0];

                if(likeToId >= AllLists.allAccounts.length || AllLists.allAccounts[likeToId] == null)
                    return false;

                if(like[1] <= 0)
                    return false;
            }
            return true;
        }
        return false;
    }
}
