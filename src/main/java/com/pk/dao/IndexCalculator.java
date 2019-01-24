package com.pk.dao;

import com.pk.Runner;
import com.pk.model.AllLists;
import com.pk.model.PostLists;

import java.util.*;

import static com.pk.model.AllLists.*;
import static com.pk.model.AllLists.fnames;
import static com.pk.model.AllLists.snames;
import static com.pk.model.PostLists.*;
//import static com.pk.model.PostLists.accIdAdded;
//import static com.pk.model.PostLists.accIdEdited;

public class IndexCalculator {


    public void calculateIndexes() {

        System.out.println("START RECALC INDEX " + Calendar.getInstance().getTimeInMillis());
        //Collections.sort(accIdAdded);
        //Collections.sort(accIdEdited);

        //System.out.println("accId sorted " + Calendar.getInstance().getTimeInMillis());

        addNewEmailAndDomains();
        //System.gc();

        System.out.println("addNewEmailAndDomains complete " + Calendar.getInstance().getTimeInMillis());

        commitFSnamesEmails();
        //System.gc();

        System.out.println("commitFSnamesEmails complete " + Calendar.getInstance().getTimeInMillis());

        if(isNewEmailDomain || isNewCity || isNewCountry)
            createCountryCityDomainsPhoneCodesAccountArrays();
        //System.gc();

        System.out.println("createCountryCityDomainsPhoneCodesAccountArrays complete " + Calendar.getInstance().getTimeInMillis());

        //city and country need only to sort;
        sortCountryCityEmails();
        //System.gc();

        System.out.println("sortCountryCityEmails complete " + Calendar.getInstance().getTimeInMillis());

        updatePremiumLists();
        //System.gc();
        System.out.println("updatePremiumLists complete " + Calendar.getInstance().getTimeInMillis());

        System.out.println("INDEX RECALC complete " + Calendar.getInstance().getTimeInMillis());
    }

    private void updatePremiumLists() {

        AllLists.premiumNowAccounts.clear();
        AllLists.premiumEverAccounts.clear();
        AllLists.premiumNeverAccounts.clear();
        for(int i=0; i<AllLists.allAccounts.length; ++i) {
            if(AllLists.allAccounts[i] != null) {

                if(AllLists.allAccounts[i].premiumStart < Runner.curDate && AllLists.allAccounts[i].premiumEnd > Runner.curDate)
                    AllLists.premiumNowAccounts.add(i);

                if(AllLists.allAccounts[i].premiumStart > 0 && AllLists.allAccounts[i].premiumEnd > 0)
                    AllLists.premiumEverAccounts.add(i);
                else
                    AllLists.premiumNeverAccounts.add(i);

            }
        }
    }

    private void rebuildEmailBorders(HashMap<String, Integer> temp, HashMap<Integer, Integer> tempId) {

        //build borders
        int max = Integer.MIN_VALUE;
        int i =0;
        for(char f = 'a'; f<='z'; ++f) {
            for(char s = 'a'; s<='z'; ++s) {
                for(char t = 'a'; t<='z'; ++t) {

                    while (i<allEmailList.size() && (new StringBuilder()).append(f).append(s).append(t).toString().compareTo(allEmailList.get(i)) > 0) {
                        ++i;
                        max = Math.max(max, tempId.getOrDefault(i, allEmailList.size()));
                    }
                    emailHightBorder[f-'a'][s-'a'][t-'a'] = max;
                }
            }
        }

        i = allEmailList.size() - 1;
        max = Integer.MIN_VALUE;
        for(char f = 'z'; f>='a'; --f) {
            for(char s = 'z'; s>='a'; --s) {
                for(char t = 'z'; t>='a'; --t) {

                    while (i>=0 && (new StringBuilder()).append(f).append(s).append(t).toString().compareTo(allEmailList.get(i)) < 0) {
                        --i;
                        max = Math.max(max, tempId.getOrDefault(i, allEmailList.size()));
                    }
                    emailLowBorder[f-'a'][s-'a'][t-'a'] = max;
                }
            }
        }

        //tempId.clear();
        //tempId = null;
    }

    private void createCountryCityDomainsPhoneCodesAccountArrays() {
        com.pk.model.Account[] allAccounts = AllLists.allAccounts;

        List<List<Integer>> tempCountryAccounts = null;
        //if(isNewCountry)
            tempCountryAccounts = new ArrayList<>(allAccounts.length);

        List<List<Integer>> tempCityAccounts = null;
        //if(isNewCity)
            tempCityAccounts = new ArrayList<>(allAccounts.length);

        //if(isNewEmailDomain)
            AllLists.domainAccounts.clear();

        //List прямо сортированные
        //create temp Lists
        for (int i = 0; i <allAccounts.length; ++i) {
            com.pk.model.Account account = allAccounts[i];
            if (account == null)
                continue;

            if (isNewCountry) {
                //set country index
                while (tempCountryAccounts.size() <= account.country)
                    tempCountryAccounts.add(null);

                if (tempCountryAccounts.get(account.country) == null) {
                    tempCountryAccounts.set(account.country, new ArrayList<>());
                }

                tempCountryAccounts.get(account.country).add(account.id);
            }

            //if (isNewCity) {
                //set city index
                while (tempCityAccounts.size() <= account.city)
                    tempCityAccounts.add(null);

                if (tempCityAccounts.get(account.city) == null) {
                    tempCityAccounts.set(account.city, new ArrayList<>());
                }

                tempCityAccounts.get(account.city).add(account.id);
            //}

            //if (isNewEmailDomain) {
                //set email domain
                int domainIndex = account.emailDomain;

                while (AllLists.domainAccounts.size() <= domainIndex)
                    AllLists.domainAccounts.add(null);

                if (AllLists.domainAccounts.get(domainIndex) == null) {
                    AllLists.domainAccounts.set(domainIndex, new ArrayList<>());
                }

                AllLists.domainAccounts.get(domainIndex).add(account.id);
            //}
        }

        //if(isNewCountry) {
            //store to fixedArrays
            AllLists.countryAccounts = new int[AllLists.countriesList.size()][];
            for (int i = 0; i < tempCountryAccounts.size(); i++) {
                List<Integer> acList = tempCountryAccounts.get(i);

                AllLists.countryAccounts[i] = new int[acList.size()];

                for (int i1 = 0; i1 < acList.size(); i1++) {
                    int cId = acList.get(i1);
                    AllLists.countryAccounts[i][i1] = cId;
                }
            }

            tempCountryAccounts.clear();
            tempCountryAccounts = null;
        //}

        //if(isNewCity) {
            AllLists.cityAccounts = new int[tempCityAccounts.size()][];
            for (int i = 0; i < tempCityAccounts.size(); i++) {
                List<Integer> acList = tempCityAccounts.get(i);
                if (acList != null) {
                    AllLists.cityAccounts[i] = new int[acList.size()];

                    for (int i1 = 0; i1 < acList.size(); i1++) {
                        int cId = acList.get(i1);
                        AllLists.cityAccounts[i][i1] = cId;
                    }
                }
            }
            tempCityAccounts.clear();
            tempCityAccounts = null;
        //}
    }

    private void commitFSnamesEmails() {

        int oldFnamesSize = fnames.length;
        int oldSnamesSize = snames.length;

        //AllLists.allEmailList.addAll(PostLists.newEmails);

        //for (String emailDomain : PostLists.newEmailDomains) {
            //AllLists.domainList.add(emailDomain.toCharArray());
        //}

        fnames = Arrays.copyOf(fnames, fnames.length + PostLists.fnames.size());
        snames = Arrays.copyOf(snames, snames.length + PostLists.snames.size());

        List<String> fnames1 = PostLists.fnames;
        for (int i = 0; i < fnames1.size(); i++) {
            String ttt = fnames1.get(i);
            if (!"".equals(ttt)) {
                fnames[oldFnamesSize + i] = ttt.toCharArray();
            }
        }

        List<String> snames1 = PostLists.snames;
        for (int i = 0; i < snames1.size(); i++) {
            String ttt = snames1.get(i);
            if (!"".equals(ttt)) {
                snames[oldSnamesSize + i] = ttt.toCharArray();
            }
        }
    }

    private void addNewEmailAndDomains() {

        for(String ttt : PostLists.newEmails) {
            AllLists.allEmailList.add(ttt);
        }

        for(String ttt: PostLists.newEmailDomains) {
            AllLists.domainList.add(ttt.toCharArray());
        }
    }


    private void sortCountryCityEmails() {
        ArrayList<String> tempCountryList = null;
        if(isNewCountry) {
            tempCountryList = new ArrayList<>(countriesList);
            Collections.sort(countriesList);
        }

        ArrayList<String> tempCityList = null;
        if(isNewCity) {
            tempCityList = new ArrayList<>(citiesList);
            Collections.sort(citiesList);
        }

        ArrayList<char[]> tempDomainList = null;
        if(isNewEmailDomain) {
            tempDomainList = new ArrayList<>(domainList);
            Collections.sort(domainList, Utils::compareCharArr);
        }

        ArrayList<String> tempEmailList = new ArrayList<>(allEmailList);
        Collections.sort(allEmailList);

        char[][] tempFnameList = Arrays.copyOf(fnames, fnames.length);
        Arrays.sort(fnames, Utils::compareCharArr);

        char[][] tempSnameList = Arrays.copyOf(snames, snames.length);
        Arrays.sort(snames, Utils::compareCharArr);

        HashMap<String, Integer> temp = new HashMap<>();
        for (int i = 0; i < allEmailList.size(); i++) {
            String p = allEmailList.get(i);
            temp.put(p, i);
        }

        HashMap<Integer, Integer> tempId = new HashMap<>();

        for(com.pk.model.Account account : allAccounts) {
            if (account != null) {

                if(isNewCountry && account.country > 0)
                    account.country = (short) Collections.binarySearch(countriesList, tempCountryList.get(account.country));

                if(isNewCity && account.city > 0)
                    account.city = (short) Collections.binarySearch(citiesList, tempCityList.get(account.city));

                if(account.email > 0) {
                    account.email = temp.get(tempEmailList.get(account.email));

                    if(isNewEmailDomain)
                        account.emailDomain = domainList.indexOf(tempDomainList.get(account.emailDomain));
                }

                if(account.fname > 0)
                    account.fname = Utils.getFnameIndexBinary(tempFnameList[account.fname]);

                if(account.sname > 0)
                    account.sname = Utils.getSnameIndexBinary(tempSnameList[account.sname]);

                tempId.put(account.email, Math.max(temp.getOrDefault(account.email, 0), account.id));
            }
        }

        rebuildEmailBorders(temp, tempId);

/*
        tempCountryList.clear();
        tempCountryList = null;

        tempCityList.clear();
        tempCityList = null;

        tempDomainList.clear();
        tempDomainList = null;

        tempEmailList.clear();
        tempEmailList = null;

        tempSnameList = null;
        tempFnameList = null;
*/

    }


    public void clearTempData() {

        PostLists.fnames.clear();
        //PostLists.fnames = null;

        PostLists.snames.clear();
        //PostLists.snames = null;

        //accIdAdded.clear();
        //accIdAdded = null;

        //accIdEdited.clear();
        //accIdEdited = null;

        PostLists.freeEmailDomain.clear();
        //PostLists.freeEmailDomain = null;

        PostLists.usedEmailDomain.clear();
        //PostLists.usedEmailDomain = null;


        PostLists.newEmailDomains.clear();
        //PostLists.newEmailDomains = null;

        PostLists.newEmails.clear();
        //PostLists.newEmails = null;


        PostLists.newLikes.clear();
        PostLists.newLikes = null;
    }

    private static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
