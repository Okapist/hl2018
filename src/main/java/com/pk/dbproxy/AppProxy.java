package com.pk.dbproxy;

import com.pk.Runner;
import com.pk.dao.Utils;
import com.pk.jsonmodel.Account;
import com.pk.jsonmodel.Accounts;
import com.pk.jsonmodel.Likes;
import com.pk.model.AllLists;

import java.util.*;

import static com.pk.model.AllLists.*;

public class AppProxy {

    int maxIntId = 1;

    private ArrayList<String> fnames = new ArrayList<>();
    private ArrayList<String> snames = new ArrayList<>();

    public HashMap<String, Integer> tempEmailIndex = new HashMap<>();
    private Set<Integer> tempUsedEmails = new HashSet<>(1_100_000);

    private List<List<Integer>> tempCountryAccounts = new ArrayList<>();
    private List<List<Integer>> tempCityAccounts = new ArrayList<>();

    private List<List<Integer>> tempLikesTo = new ArrayList<>();

    List<List<Integer>> tempIinterestAccounts = new ArrayList<>();
    private HashMap<Integer, HashMap<Integer, List<Integer>>>[][][] tempRecommendInteresFilter = new HashMap[2][3][];

    public AppProxy() {
        fnames.add("");
        snames.add("");
    }

    public void load(Accounts myObjects) {

        for (Account jsonAccount : myObjects.getAccounts()) {

            com.pk.model.Account account =new com.pk.model.Account();
            account.id = jsonAccount.getId();

            if(jsonAccount.getEmail() != null) {
                String emailParts[] = jsonAccount.getEmail().split("@");

                if(tempEmailIndex.get(emailParts[0]) == null) {
                    allEmailList.add(emailParts[0]);
                    account.email = allEmailList.size()-1;
                    tempEmailIndex.put(emailParts[0], allEmailList.size()-1);
                } else {
                    account.email = tempEmailIndex.get(emailParts[0]);
                }

                Integer domainIndex = Utils.findDomainIndex(emailParts[1].toCharArray());
                if(domainIndex == null || domainIndex == 0) {
                    if(AllLists.domainList.size() == 0)
                        AllLists.domainList.add(null);

                    AllLists.domainList.add(emailParts[1].toCharArray());
                    domainIndex = AllLists.domainList.size()-1;
                }
                account.emailDomain = domainIndex;

                int hash = account.emailDomain;
                hash |= account.email<<7;
                if(tempUsedEmails.contains(hash)) {
                    System.out.println("WHONG HASH CALC " + hash);
                }
                tempUsedEmails.add(hash);
            }

            if(jsonAccount.getFname() != null) {
                if(fnames.indexOf(jsonAccount.getFname()) == -1) {
                    fnames.add(jsonAccount.getFname());
                }
                account.fname = fnames.indexOf(jsonAccount.getFname());
            }

            if(jsonAccount.getSname() != null) {
                if(snames.indexOf(jsonAccount.getSname()) == -1) {
                    snames.add(jsonAccount.getSname());
                }
                account.sname = snames.indexOf(jsonAccount.getSname());
            }

            if(jsonAccount.getPhone() != null)
                account.phone = jsonAccount.getPhone().toCharArray();

            account.sex = jsonAccount.getSexBoolean();

            if(jsonAccount.getCountry() != null) {
                Short coutryIndex = Utils.findCountryIndex(jsonAccount.getCountry());
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
                Short citiIndex = Utils.findCityIndex(jsonAccount.getCity());
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

            if(jsonAccount.getInterests() != null) {
                account.interests = new HashSet<>(jsonAccount.getInterests().length);
                account.interestsArray = new int[jsonAccount.getInterests().length];
                String[] interests = jsonAccount.getInterests();
                for (int i = 0; i < interests.length; i++) {
                    String interest = interests[i];
                    AllLists.interests.computeIfAbsent(interest, p -> maxIntId++);
                    Integer intId = AllLists.interests.get(interest);

                    //set interests index

                    while (tempIinterestAccounts.size() <= intId)
                        tempIinterestAccounts.add(null);

                    if (tempIinterestAccounts.get(intId) == null) {
                        tempIinterestAccounts.set(intId, new ArrayList<>());
                    }

                    tempIinterestAccounts.get(intId).add(account.id);


                    account.interests.add(intId);
                    account.interestsArray[i] = intId;
                }
            }
            addLikes(jsonAccount);
        }
    }

    public void buildCountryCityList() {
        AllLists.countryCityList = new ArrayList[AllLists.countriesList.size()];

        for (com.pk.model.Account account : AllLists.allAccounts) {
            if (account != null) {

                if (countryCityList[account.country] == null)
                    countryCityList[account.country] = new ArrayList<>();

                if (!countryCityList[account.country].contains(account.city)) {
                    countryCityList[account.country].add(account.city);
                }
            }
        }
    }

    public void sortAccounts() {

        for(int[] list : AllLists.interestAccounts) {
            if(list != null)
                Arrays.sort(list);
        }

        ArrayList<String> tempCountryList = new ArrayList<>(countriesList);
        Collections.sort(countriesList);

        ArrayList<String> tempCityList = new ArrayList<>(citiesList);
        Collections.sort(citiesList);

        ArrayList<char[]> tempDomainList = new ArrayList<>(domainList);
        Collections.sort(domainList, Utils::compareCharArr);

        ArrayList<String> tempEmailList = new ArrayList<>(allEmailList);
        Collections.sort(allEmailList);
        HashMap<String, Integer> temp = new HashMap<>();
        for (int i = 0; i < allEmailList.size(); i++) {
            String p = allEmailList.get(i);
            temp.put(p, i);
        }

        for(com.pk.model.Account account : allAccounts) {
            if(account != null) {
                account.country = (short) countriesList.indexOf(tempCountryList.get(account.country));
                account.city = (short) citiesList.indexOf(tempCityList.get(account.city));

                account.email = temp.get(tempEmailList.get(account.email));
                account.emailDomain = domainList.indexOf(tempDomainList.get(account.emailDomain));
            }
        }

        temp.clear();

        HashMap<Integer, Integer> tempId = new HashMap<>();
        for(com.pk.model.Account account : allAccounts) {
            if(account != null) {
                tempId.put(account.email, Math.max(temp.getOrDefault(account.email, 0), account.id));
            }
        }

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

        tempId.clear();
        tempId = null;
        temp.clear();
        temp = null;
    }

    private void addLikes(Account jsonAccount) {

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

                while (tempLikesTo.size() <= likeId)
                    tempLikesTo.add(null);

                if(tempLikesTo.get(likeId) == null) {
                    tempLikesTo.set(likeId, new ArrayList<>());
                }

                tempLikesTo.get(likeId).add(jsonAccount.getId());
            }
        }
    }

    public void createFilters() {

        com.pk.model.Account[] allAccounts = AllLists.allAccounts;

        //List прямо сортированные
        for (int i = 0; i <allAccounts.length; ++i) {
            com.pk.model.Account account = allAccounts[i];
            if(account == null)
                continue;

            //set country index
            while (tempCountryAccounts.size() <= account.country)
                tempCountryAccounts.add(null);

            if (tempCountryAccounts.get(account.country) == null) {
                tempCountryAccounts.set(account.country, new ArrayList<>());
            }

            tempCountryAccounts.get(account.country).add(account.id);

            //set city index
            while (tempCityAccounts.size() <= account.city)
                tempCityAccounts.add(null);

            if (tempCityAccounts.get(account.city) == null) {
                tempCityAccounts.set(account.city, new ArrayList<>());
            }

            tempCityAccounts.get(account.city).add(account.id);

            //set email domain
            int domainIndex = account.emailDomain;

            while (AllLists.domainAccounts.size() <= domainIndex)
                AllLists.domainAccounts.add(null);

            if (AllLists.domainAccounts.get(domainIndex) == null) {
                AllLists.domainAccounts.set(domainIndex, new ArrayList<>());
            }

            AllLists.domainAccounts.get(domainIndex).add(account.id);

            if (account.phone != null) {
                String phone = new String(account.phone);
                String phoneCode = phone.substring(phone.indexOf("(") + 1, phone.indexOf(")"));
                account.phoneCode = phoneCode.toCharArray();
                AllLists.phoneCodeAccounts.computeIfAbsent(phoneCode, p -> new ArrayList<>());
                AllLists.phoneCodeAccounts.get(phoneCode).add(account.id);
            } else {
                AllLists.phoneCodeAccounts.computeIfAbsent(null, p -> new ArrayList<>());
                AllLists.phoneCodeAccounts.get(null).add(account.id);
            }
        }

        for(String key : AllLists.interests.keySet()) {
            AllLists.interestsById.put(AllLists.interests.get(key), key);
        }
        System.out.println("CREATE NEW FILTERS");
        createNewFilters();
        System.gc();
        System.out.println("CREATE RECOMMEND FILTERS");
        createRecommendFilter();
        System.gc();
        System.out.println("CREATE GROUP FILTERS");
        createGroupFilter();
        System.gc();
        System.out.println("CREATE GROUP COUNTRY SUMS");
        //createGroupFilterSumCountry();
        System.out.println("ALL FILTERS CREATED");
    }

    private void createGroupFilterSumCountry() {
    }


    private void createGroupFilter() {

        groupFilter = new HashMap[countriesList.size()];
        groupFilterBirth = new HashMap[countriesList.size()];
        groupFilterJoined = new HashMap[countriesList.size()];

        for (com.pk.model.Account account : allAccounts) {
            if (account == null)
                continue;
            int countryIndex = account.country;
            int sexIndex = account.sex ? 1 : 0;
            int statusIndex = account.status - 1;

            int birth = getYear(account.birth);
            int joined = getYear(account.joined);
            //int fixedCityIndex = countryCityList[account.country].indexOf(account.city);

            if(groupFilter[countryIndex] == null) {

                groupFilter[countryIndex] = new HashMap<>();
                groupFilterBirth[countryIndex] = new HashMap<>();
                groupFilterJoined[countryIndex] = new HashMap<>();
            }

            groupFilter[countryIndex].computeIfAbsent(account.city, p-> new short[3][2]);
            groupFilterBirth[countryIndex].computeIfAbsent(account.city, p-> new short[3][2][MAX_BIRTH_YEAR - MIN_BIRTH_YEAR + 1]);
            groupFilterJoined[countryIndex].computeIfAbsent(account.city, p-> new short[3][2][MAX_JOINED_YEAR - MIN_JOINED_YEAR + 1]);

            ++groupFilter[countryIndex].get(account.city)[statusIndex][sexIndex];
            ++groupFilterBirth[countryIndex].get(account.city)[statusIndex][sexIndex][birth-MIN_BIRTH_YEAR];
            ++groupFilterJoined[countryIndex].get(account.city)[statusIndex][sexIndex][joined-MIN_JOINED_YEAR];
        }

        int i = 0;
    }

    Calendar cal = Calendar.getInstance();
    private int getYear(int timestamp) {
        cal.setTimeInMillis((long)timestamp*1000);
        return cal.get(Calendar.YEAR);
    }

    private void createRecommendFilter() {

        for (int premium = 0; premium < 2; ++premium) {
            for (int status = 0; status < 3; ++status) {
                tempRecommendInteresFilter[premium][status] = new HashMap[AllLists.countriesList.size()];

                for (int countryIndex = 0; countryIndex < AllLists.countriesList.size(); ++countryIndex) {
                    tempRecommendInteresFilter[premium][status][countryIndex] = new HashMap<>();
                }
            }
        }

        for (com.pk.model.Account account : allAccounts) {
            if(account==null)
                continue;

            int premiumIndex = account.premiumEnd > Runner.curDate ? 0 : 1;
            int countryIndex = account.country;
            int statusIndex = account.status - 1;
            int cityIndex = account.city;
            int[] interestsIndex = account.interestsArray;

            tempRecommendInteresFilter[premiumIndex][statusIndex][countryIndex].computeIfAbsent(cityIndex, p -> new HashMap<>());
            if (interestsIndex != null) {
                for (int interest : interestsIndex) {
                    tempRecommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(interest, p -> new ArrayList<>());
                    tempRecommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(interest).add(account.id);
                }
            } else {
                tempRecommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(0, p -> new ArrayList<>());
                tempRecommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(0).add(account.id);

            }
        }

        for(int premiumIndex=0; premiumIndex<tempRecommendInteresFilter.length; ++premiumIndex) {
            for(int statusIndex=0; statusIndex<tempRecommendInteresFilter[premiumIndex].length; ++statusIndex) {

                AllLists.recommendInteresFilter[premiumIndex][statusIndex] = new HashMap[AllLists.countriesList.size()];

                for(int countryIndex=0; countryIndex<tempRecommendInteresFilter[premiumIndex][statusIndex].length; ++countryIndex) {

                    AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex] = new HashMap<>();

                    for(Integer cityIndex : tempRecommendInteresFilter[premiumIndex][statusIndex][countryIndex].keySet()) {
                        AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].computeIfAbsent(cityIndex, p -> new HashMap<>());

                        HashMap<Integer, List<Integer>> aa = tempRecommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex);
                        for(Integer intId : aa.keySet()) {
                            AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(intId, p -> new int[(int) Math.max(aa.get(intId).size() * 1.15, aa.get(intId).size() + 1)]);

                            List<Integer> get = aa.get(intId);
                            for (int i = 0; i < get.size(); i++) {
                                int accId = get.get(i);
                                AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(intId)[i] = accId;
                            }
                        }

                    }
                }
            }
        }
        tempRecommendInteresFilter = null;
    }

    public void createNewFilters() {

        /*
        for(int i=0; i<AllLists.allAccounts.length; ++i) {

            fnameAccounts = new int[allAccounts.length];
            snameAccounts = new int[allAccounts.length];
            emailAscAccounts = new int[allAccounts.length];
            emailDescAccounts = new int[allAccounts.length];
            birthAccount = new int[allAccounts.length];
        }
        */

        for(int i=0; i<AllLists.allAccounts.length; ++i) {
            if(AllLists.allAccounts[i] != null) {

/*
                while (AllLists.joinedSortedAccounts.size() <= i)
                    AllLists.joinedSortedAccounts.add(null);
                AllLists.joinedSortedAccounts.set(i, i);
*/

                if(AllLists.allAccounts[i].premiumStart < Runner.curDate && AllLists.allAccounts[i].premiumEnd > Runner.curDate)
                    AllLists.premiumNowAccounts.add(i);

                if(AllLists.allAccounts[i].premiumStart > 0 && AllLists.allAccounts[i].premiumEnd > 0)
                    AllLists.premiumEverAccounts.add(i);
                else
                    AllLists.premiumNeverAccounts.add(i);

            }
        }

/*
        Collections.sort(AllLists.joinedSortedAccounts, (p1,p2) -> {
            if(p1 == null && p2 == null)
                return 0;
            if(p1 == null)
                return -1;
            if(p2 == null)
                return 1;

            return AllLists.allAccounts[p1].joined - AllLists.allAccounts[p2].joined;
        });
*/

/*
        int year = 1931;
        for (int i = 0; i < AllLists.joinedSortedAccounts.size(); i++) {
            if(AllLists.joinedSortedAccounts.get(i) != null) {
                if(AllLists.allAccounts[AllLists.joinedSortedAccounts.get(i)].joined >=Utils.getTimestamp(year)) {
                    while (AllLists.allAccounts[AllLists.joinedSortedAccounts.get(i)].joined >= Utils.getTimestamp(year))
                        ++year;

                    if(MIN_JOINED_YEAR == Integer.MAX_VALUE) {
                        MIN_JOINED_YEAR = year-1;
                    }
                    MAX_JOINED_YEAR = year;

                    AllLists.joinedYears[year - 1 - 1930] = i;
                }
            }
        }
*/
    }


    public void sortEasyIndexes() {

        ArrayList<String> sortedfnames = new ArrayList<>(fnames);
        ArrayList<String> sortedsnames = new ArrayList<>(snames);

        Collections.sort(sortedfnames);
        Collections.sort(sortedsnames);

        ArrayList<Integer>[] status = new ArrayList[3];

        for(int i=0; i<3; ++i)
            status[i] = new ArrayList<>();

        for(com.pk.model.Account account : allAccounts) {
            if(account != null) {
                if(account.fname > 0)
                    account.fname = Collections.binarySearch(sortedfnames, fnames.get(account.fname));

                if(account.sname > 0)
                    account.sname = Collections.binarySearch(sortedsnames, snames.get(account.sname));

                status[account.status-1].add(account.id);
            }
        }

        AllLists.fnames = new char[fnames.size()][];
        for (int i = 0; i < fnames.size(); i++) {
            String fn = sortedfnames.get(i);
            AllLists.fnames[i] = fn.toCharArray();
        }
        fnames.clear();
        fnames = null;

        AllLists.snames = new char[snames.size()][];
        for (int i = 0; i < sortedsnames.size(); i++) {
            String sn = sortedsnames.get(i);
            AllLists.snames[i] = sn.toCharArray();
        }
        snames.clear();
        snames = null;

        List<com.pk.model.Account> sortedAccount = new ArrayList(Arrays.asList(allAccounts));
        Collections.sort(sortedAccount, (p1,p2) -> {
            if(p1 == null && p2 == null)
                return 0;
            if(p1 == null)
                return -1;
            if(p2==null)
                return 1;

            if(p1.fname != p2.fname)
                return p1.fname - p2.fname;

            return p1.id - p2.id;
        });

        Collections.sort(sortedAccount, (p1,p2) -> {
            if(p1 == null && p2 == null)
                return 0;
            if(p1 == null)
                return -1;
            if(p2==null)
                return 1;

            if(p1.sname != p2.sname)
                return p1.sname - p2.sname;

            return p1.id - p2.id;
        });
    }

    public void createNameIndexes() {

        List<Integer>[] temp = new ArrayList[AllLists.fnames.length];
        List<Integer>[] temp1 = new ArrayList[AllLists.snames.length];

        for(com.pk.model.Account account : allAccounts) {
            if(account ==null)
                continue;

            if(temp[account.fname] == null)
                temp[account.fname] = new ArrayList<>();
            if(temp1[account.sname] == null)
                temp1[account.sname] = new ArrayList<>();

            temp[account.fname].add(account.id);
            temp1[account.sname].add(account.id);
        }

        AllLists.fnameAccounts = new int[temp.length][];
        AllLists.snameAccounts = new int[temp1.length][];
        for(int i=0 ;i<temp.length; ++i) {
            if(temp[i] != null)
                AllLists.fnameAccounts[i] = temp[i].stream().mapToInt(Integer::intValue).toArray();
        }
        for(int i=0 ;i<temp1.length; ++i) {
            if(temp1[i] != null)
            AllLists.snameAccounts[i] = temp1[i].stream().mapToInt(Integer::intValue).toArray();
        }

    }

    public void createBirthYearIndex() {
        HashMap<Integer, List<Integer>> temp = new HashMap();

        int minYear = Integer.MAX_VALUE;
        int maxYear = Integer.MIN_VALUE;
        for(com.pk.model.Account account : allAccounts) {
            if (account == null)
                continue;

            minYear = Math.min(minYear, getYear(account.birth));
            maxYear = Math.max(maxYear, getYear(account.birth));

            MIN_JOINED_YEAR = Math.min(MIN_JOINED_YEAR, getYear(account.joined));
            MAX_JOINED_YEAR = Math.max(MAX_JOINED_YEAR, getYear(account.joined));

            temp.computeIfAbsent(getYear(account.birth), p-> new ArrayList<>());
            temp.get(getYear(account.birth)).add(account.id);
        }

        MIN_JOINED_YEAR -= 2;
        MAX_JOINED_YEAR += 2;

        AllLists.birthYearsAccount = new int[maxYear-minYear+1][];
        for(int year : temp.keySet()) {
            AllLists.birthYearsAccount[year - minYear] = temp.get(year).stream().mapToInt(Integer::intValue).toArray();
        }
        MIN_BIRTH_YEAR = minYear-2;
        MAX_BIRTH_YEAR = maxYear+2;
    }

    public void commitInterests() {

        AllLists.interestAccounts = new int[tempIinterestAccounts.size()][];
        for (int i = 0; i < tempIinterestAccounts.size(); i++) {
            List<Integer> interest = tempIinterestAccounts.get(i);
            if(interest != null)
                AllLists.interestAccounts[i] = interest.stream().mapToInt(Integer::intValue).toArray();
        }

        tempIinterestAccounts.clear();
        tempIinterestAccounts = null;
    }

    public void commitUsedEmails() {
        usedEmailDomain = new int[tempUsedEmails.size()];
        int index = 0;
        for(Integer i : tempUsedEmails) {
            usedEmailDomain[index] = i;
        }

        tempUsedEmails.clear();
        tempUsedEmails = null;
        Arrays.sort(usedEmailDomain);
    }

    public void commitCountryCityAccounts() {

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

        AllLists.cityAccounts = new int[tempCityAccounts.size()][];
        for (int i = 0; i < tempCityAccounts.size(); i++) {
            List<Integer> acList = tempCityAccounts.get(i);

            AllLists.cityAccounts[i] = new int[acList.size()];

            for (int i1 = 0; i1 < acList.size(); i1++) {
                int cId = acList.get(i1);
                AllLists.cityAccounts[i][i1] = cId;
            }
        }
        tempCityAccounts.clear();
        tempCityAccounts = null;

    }

    public void commitLikes() {
        for (int i = 0; i < tempLikesTo.size(); i++) {
            List<Integer> temp = tempLikesTo.get(i);
            if(temp == null)
                continue;

            int[] commitedLikes = new int[temp.size()];
            AllLists.likesTO[i] = commitedLikes;
            for(int j=0; j<temp.size(); ++j) {
                commitedLikes[j] = temp.get(j);
            }
        }

        tempLikesTo.clear();
        tempLikesTo = null;
    }

    public void topLow50IntCalc() {
        HashMap<Integer, Integer> interestGroups = new HashMap<>();
        for (com.pk.model.Account account : allAccounts) {
            if (account == null || account.interestsArray == null || account.interestsArray.length == 0)
                continue;

            for (int intId : account.interestsArray) {
                interestGroups.put(intId, interestGroups.getOrDefault(intId, 0) + 1);
            }
        }

        LinkedHashMap<Integer, Integer> sorted = sortByValue(interestGroups);
        int size = sorted.size();
        int pointer = 0;
        for (Integer key : sorted.keySet()) {
            if (pointer < 50)
                topInterests50[pointer] = new int[]{key, sorted.get(key)};

            if (size - pointer <= 50)
                lowInterests50[size - pointer - 1] = new int[]{key, sorted.get(key)};

            ++pointer;
        }

        Arrays.sort(topInterests50, (p1, p2) -> {
            if (p1[1] != p2[1])
                return p1[1] - p2[1];
            return AllLists.interestsById.get(p1[0]).compareTo(AllLists.interestsById.get(p2[0]));
        });

        Arrays.sort(lowInterests50, (p2, p1) -> {
            if (p1[1] != p2[1])
                return p1[1] - p2[1];
            return AllLists.interestsById.get(p1[0]).compareTo(AllLists.interestsById.get(p2[0]));
        });
    }

    public  <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
