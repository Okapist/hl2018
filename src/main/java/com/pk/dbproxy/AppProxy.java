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

    private static final Integer ALL_HM_SUM_INDEX = Integer.MAX_VALUE;
    int maxIntId = 1;
    private int ALL_COUNTRY_SUM_INDEX;

    public void load(Accounts myObjects) {

        for (Account jsonAccount : myObjects.getAccounts()) {

            com.pk.model.Account account =new com.pk.model.Account();
            account.id = jsonAccount.getId();

            if(jsonAccount.getEmail() != null) {
                String emailParts[] = jsonAccount.getEmail().split("@");
                account.email = emailParts[0].toCharArray();
                Integer domainIndex = Utils.findDomainIndex(emailParts[1].toCharArray());
                if(domainIndex == null || domainIndex == 0) {
                    if(AllLists.domainList.size() == 0)
                        AllLists.domainList.add(null);

                    AllLists.domainList.add(emailParts[1].toCharArray());
                    domainIndex = AllLists.domainList.size()-1;
                }
                account.emailDomain = domainIndex;
            }

            if(jsonAccount.getFname() != null)
                account.fname = jsonAccount.getFname().toCharArray();

            if(jsonAccount.getSname() != null)
                account.sname = jsonAccount.getSname().toCharArray();

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

/*
            while (AllLists.allAccounts.size() <= account.id) {
                com.pk.model.Account account1 = new com.pk.model.Account();
                AllLists.allAccounts.add(account1);
            }
*/

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
                    while (AllLists.interestAccounts.size() <= intId)
                        AllLists.interestAccounts.add(null);

                    if (AllLists.interestAccounts.get(intId) == null) {
                        AllLists.interestAccounts.set(intId, new ArrayList<>());
                    }

                    AllLists.interestAccounts.get(intId).add(account.id);

                    account.interests.add(intId);
                    account.interestsArray[i] = intId;
                }
            }
            //Arrays.sort(account.interestsArray);
            addLikes(jsonAccount);
        }
    }

    public void buildCountryCityList() {
        AllLists.countryCityList = new ArrayList[AllLists.countriesList.size()];

        cityCounryList = new int[AllLists.citiesList.size()];
        for(com.pk.model.Account account : AllLists.allAccounts) {
            if(account != null) {
                if(account.country != 0 && account.city != 0) {
                    cityCounryList[account.city] = account.country;

                    if(countryCityList[account.country] == null)
                        countryCityList[account.country] = new ArrayList<>();

                    if(!countryCityList[account.country].contains(account.city)) {
                        countryCityList[account.country].add(account.city);
                    }
                }
            }
        }
    }

    public void sortAccounts() {
        for(List<Integer> list : AllLists.interestAccounts) {
            if(list != null)
                Collections.sort(list);
        }
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

                while (AllLists.likesTO.size() <= likeId)
                    AllLists.likesTO.add(null);

                if(AllLists.likesTO.get(likeId) == null) {
                    AllLists.likesTO.set(likeId, new ArrayList<>());
                }

                AllLists.likesTO.get(likeId).add(jsonAccount.getId());
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
            while (AllLists.countryAccounts.size() <= account.country)
                AllLists.countryAccounts.add(null);

            if (AllLists.countryAccounts.get(account.country) == null) {
                AllLists.countryAccounts.set(account.country, new ArrayList<>());
            }

            AllLists.countryAccounts.get(account.country).add(account.id);

            //set city index
            while (AllLists.cityAccounts.size() <= account.city)
                AllLists.cityAccounts.add(null);

            if (AllLists.cityAccounts.get(account.city) == null) {
                AllLists.cityAccounts.set(account.city, new ArrayList<>());
            }

            AllLists.cityAccounts.get(account.city).add(account.id);

            //set status index
            while (AllLists.statusAccounts.size() <= account.status)
                AllLists.statusAccounts.add(null);

            if (AllLists.statusAccounts.get(account.status) == null) {
                AllLists.statusAccounts.set(account.status, new ArrayList<>());
            }

            AllLists.statusAccounts.get(account.status).add(account.id);

            //set email domain
            int domainIndex = account.emailDomain;

            while (AllLists.domainAccounts.size() <= domainIndex)
                AllLists.domainAccounts.add(null);

            if (AllLists.domainAccounts.get(domainIndex) == null) {
                AllLists.domainAccounts.set(domainIndex, new ArrayList<>());
            }

            AllLists.domainAccounts.get(domainIndex).add(account.id);

            if (account.fname != null) {
                AllLists.fnameAccounts.computeIfAbsent(new String(account.fname), p -> new ArrayList<>());
                AllLists.fnameAccounts.get(new String(account.fname)).add(account.id);
            } else {
                AllLists.fnameAccounts.computeIfAbsent(null, p -> new ArrayList<>());
                AllLists.fnameAccounts.get(null).add(account.id);
            }

            if (account.sname != null) {
                AllLists.snameAccounts.computeIfAbsent(new String(account.sname), p -> new ArrayList<>());
                AllLists.snameAccounts.get(new String(account.sname)).add(account.id);
            } else {
                AllLists.snameAccounts.computeIfAbsent(null, p -> new ArrayList<>());
                AllLists.snameAccounts.get(null).add(account.id);
            }

            if (account.phone != null) {
                String phone = new String(account.phone);
                String phoneCode = phone.substring(phone.indexOf("(") + 1, phone.indexOf(")"));
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
        createGroupFilterSumCountry();
        System.out.println("ALL FILTERS CREATED");
    }

    //HashMap<Integer,HashMap<Integer,HashMap<Integer,Integer>>>[][][] = count

    /*
    private void createGroupFilterSum() {
        for (com.pk.model.Account account : allAccounts) {

            if (account == null)
                continue;
            //[country]<city>[sex][status]<birth><interests> = count;
            int countryIndex = cityCounryList[account.city > 0 ? account.city : account.country];
            int cityIndex = account.city;
            int sexIndex = account.sex?2:1;
            int statusIndex = account.status;
            //int birth = getYear(account.birth);
            //int joined = getYear(account.joined);
            int[] interestsIndex = account.interestsArray;

            HashMap<Integer, Integer> toEditGroupInterest = groupFilter[countryIndex].get(cityIndex)[sexIndex][statusIndex];
            toEditGroupInterest.put(null, toEditGroupInterest.getOrDefault(null, 0) + 1);

            if(groupFilter[ALL_COUNTRY_SUM_INDEX] == null)
                groupFilter[ALL_COUNTRY_SUM_INDEX] = new HashMap<>();

            groupFilter[ALL_COUNTRY_SUM_INDEX].computeIfAbsent(cityIndex, k -> new HashMap[3][4]);
            groupFilter[ALL_COUNTRY_SUM_INDEX].computeIfAbsent(null, k -> new HashMap[3][4]);
            groupFilter[ALL_COUNTRY_SUM_INDEX].computeIfAbsent(ALL_HM_SUM_INDEX, k -> new HashMap[3][4]);
            groupFilter[countryIndex].computeIfAbsent(ALL_HM_SUM_INDEX, p-> new HashMap[3][4]);

            if(groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex] == null)
                groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex] = new HashMap<>();

            if(groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][0] == null)
                groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][0] = new HashMap<>();

            if(groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[0][statusIndex] == null)
                groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[0][statusIndex] = new HashMap<>();

            if(groupFilter[ALL_COUNTRY_SUM_INDEX].get(null)[sexIndex][statusIndex] == null)
                groupFilter[ALL_COUNTRY_SUM_INDEX].get(null)[sexIndex][statusIndex] = new HashMap<>();

            if(groupFilter[ALL_COUNTRY_SUM_INDEX].get(ALL_HM_SUM_INDEX)[sexIndex][statusIndex] == null)
                groupFilter[ALL_COUNTRY_SUM_INDEX].get(ALL_HM_SUM_INDEX)[sexIndex][statusIndex] = new HashMap<>();

            if(groupFilter[countryIndex].get(cityIndex)[sexIndex][0] == null)
                groupFilter[countryIndex].get(cityIndex)[sexIndex][0] = new HashMap<>();

            if(groupFilter[countryIndex].get(cityIndex)[0][statusIndex] == null)
                groupFilter[countryIndex].get(cityIndex)[0][statusIndex] = new HashMap<>();

            if(groupFilter[countryIndex].get(ALL_HM_SUM_INDEX)[0][statusIndex] == null)
                groupFilter[countryIndex].get(ALL_HM_SUM_INDEX)[0][statusIndex] = new HashMap<>();

            //groupFilter[countryIndex].get(cityIndex)[sexIndex][0].computeIfAbsent(birth, p-> new HashMap<>());
            //groupFilter[countryIndex].get(cityIndex)[0][statusIndex].computeIfAbsent(birth, p-> new HashMap<>());

            groupFilter[countryIndex].computeIfAbsent(null, p-> new HashMap[3][4]);

            if(groupFilter[countryIndex].get(null)[sexIndex][statusIndex] == null)
                groupFilter[countryIndex].get(null)[sexIndex][statusIndex] = new HashMap<>();

            //groupFilter[countryIndex].get(null)[sexIndex][statusIndex].computeIfAbsent(birth, p-> new HashMap<>());
            //groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex].computeIfAbsent(birth, p-> new HashMap<>());

            //HashMap<Integer, Integer> toEditStatus = groupFilter[countryIndex].get(cityIndex)[sexIndex][0].get(birth);
            //HashMap<Integer, Integer> toEditSex = groupFilter[countryIndex].get(cityIndex)[0][statusIndex].get(birth);

            if(groupFilter[countryIndex].get(ALL_HM_SUM_INDEX)[sexIndex][statusIndex] == null) {
                groupFilter[countryIndex].get(ALL_HM_SUM_INDEX)[sexIndex][statusIndex] = new HashMap<>();
            }

            //groupFilter[countryIndex].get(ALL_HM_SUM_INDEX)[sexIndex][statusIndex].computeIfAbsent(birth, p-> new HashMap<>());
            //HashMap<Integer, Integer> toEditCity = groupFilter[countryIndex].get(ALL_HM_SUM_INDEX)[sexIndex][statusIndex].get(birth);

            //HashMap<Integer, Integer> toEditCountry = groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex].get(birth);

            groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex].computeIfAbsent(ALL_HM_SUM_INDEX, p-> new HashMap<>());
            HashMap<Integer, Integer> toEditBirth = groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex].get(ALL_HM_SUM_INDEX);

            if(interestsIndex != null) {
                for(int interest : interestsIndex) {
                    toEditStatus.put(interest, toEditStatus.getOrDefault(interestsIndex, 0) + 1);
                    toEditSex.put(interest, toEditSex.getOrDefault(interestsIndex, 0) + 1);
                    toEditCity.put(interest, toEditSex.getOrDefault(interestsIndex, 0) + 1);
                    toEditCountry.put(interest, toEditSex.getOrDefault(interestsIndex, 0) + 1);
                    toEditBirth.put(interest, toEditSex.getOrDefault(interestsIndex, 0) + 1);
                }
            } else {
                toEditStatus.put(null, toEditStatus.getOrDefault(null, 0) + 1);
                toEditSex.put(null, toEditSex.getOrDefault(null, 0) + 1);
                toEditCity.put(null, toEditCity.getOrDefault(null, 0) + 1);
                toEditCountry.put(null, toEditCountry.getOrDefault(null, 0) + 1);
                toEditBirth.put(null, toEditSex.getOrDefault(null, 0) + 1);
            }
        }
    }
*/


    private void createGroupFilterSumCountry() {

        /*
        groupFilter[ALL_COUNTRY_SUM_INDEX] = new HashMap<>();

        for (com.pk.model.Account account : allAccounts) {
            if (account == null)
                continue;
            //[country]<city>[sex][status]<birth><interests> = count;
            int countryIndex = cityCounryList[account.city > 0 ? account.city : account.country];
            int cityIndex = account.city;
            int sexIndex = account.sex?2:1;
            int statusIndex = account.status;
            int[] interestsIndex = account.interestsArray;

            if(groupFilter[ALL_COUNTRY_SUM_INDEX] == null)
                groupFilter[ALL_COUNTRY_SUM_INDEX] = new HashMap<>();

            groupFilter[ALL_COUNTRY_SUM_INDEX].computeIfAbsent(cityIndex, k -> new HashMap[3][4]);
            if(groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex] == null)
                groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex] = new HashMap<>();

            //groupFilter[countryIndex].get(cityIndex)[sexIndex][statusIndex].computeIfAbsent(birth, p-> new HashMap<>());

            HashMap<Integer, Integer> toEdit = groupFilter[ALL_COUNTRY_SUM_INDEX].get(cityIndex)[sexIndex][statusIndex];

            if (interestsIndex != null) {
                for (int interest : interestsIndex) {
                    toEdit.put(interest, toEdit.getOrDefault(interest, 0) + 1);
                }
            } else {
                toEdit.put(null, toEdit.getOrDefault(null, 0) + 1);
            }
        }*/

    }


    private void createGroupFilter() {

        ALL_COUNTRY_SUM_INDEX = AllLists.countriesList.size();
        groupFilter = new HashMap[ALL_COUNTRY_SUM_INDEX+1];

        //sum for all country
        if(groupFilter[ALL_COUNTRY_SUM_INDEX] == null)
            groupFilter[ALL_COUNTRY_SUM_INDEX] = new HashMap<>();



        for (com.pk.model.Account account : allAccounts) {
            if (account == null)
                continue;
            //[country]<city>[sex][status]<interests> = count;
            int countryIndex = account.country;
            int cityIndex = account.city;
            int sexIndex = account.sex ? 2 : 1;
            int statusIndex = account.status;
            int[] interestsIndex = account.interestsArray;

            for (int tmpCountry : new int[]{countryIndex, ALL_COUNTRY_SUM_INDEX}) {

                if (groupFilter[tmpCountry] == null)
                    groupFilter[tmpCountry] = new HashMap<>();

                for (int tmpCity : new int[]{cityIndex, ALL_HM_SUM_INDEX}) {
                    groupFilter[tmpCountry].computeIfAbsent(tmpCity, k -> new HashMap[3][4]);

                    for (int tmpSex : new int[]{sexIndex, 0}) {

                        for (int tmpStatus : new int[]{statusIndex, 0}) {

                            if (groupFilter[tmpCountry].get(tmpCity)[tmpSex][tmpStatus] == null)
                                groupFilter[tmpCountry].get(tmpCity)[tmpSex][tmpStatus] = new HashMap<>();

                            HashMap<Integer, Integer> toEdit = groupFilter[tmpCountry].get(tmpCity)[tmpSex][tmpStatus];

                            toEdit.put(ALL_HM_SUM_INDEX, toEdit.getOrDefault(ALL_HM_SUM_INDEX, 0) + 1);

                            if (interestsIndex != null) {
                                for (int interest : interestsIndex) {
                                    toEdit.put(interest, toEdit.getOrDefault(interest, 0) + 1);
                                }
                            } else {
                                toEdit.put(null, toEdit.getOrDefault(null, 0) + 1);
                            }
                        }
                    }
                }
            }
        }

        int i =0;
    }

    Calendar cal = Calendar.getInstance();
    private int getYear(int timestamp) {
        cal.setTimeInMillis((long)timestamp*1000);
        return cal.get(Calendar.YEAR);
    }

    private void createRecommendFilter() {

        for (int premium = 0; premium < 2; ++premium) {
            for (int status = 0; status < 3; ++status) {
                recommendInteresFilter[premium][status] = new HashMap[AllLists.countriesList.size()];

                for (int countryIndex = 0; countryIndex < AllLists.countriesList.size(); ++countryIndex) {
                    recommendInteresFilter[premium][status][countryIndex] = new HashMap<>();
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

            recommendInteresFilter[premiumIndex][statusIndex][countryIndex].computeIfAbsent(cityIndex, p -> new HashMap<>());
            if (interestsIndex != null) {
                for (int interest : interestsIndex) {
                    recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(interest, p -> new HashSet<>());
                    recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(interest).add(account.id);
                }
            } else {
                recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).computeIfAbsent(0, p -> new HashSet<>());
                recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex).get(0).add(account.id);

            }
        }
    }

    public void createNewFilters() {
        for(int i=0; i<AllLists.allAccounts.length; ++i) {
            if(AllLists.allAccounts[i] != null) {

                while (AllLists.emailSortedAccounts.size() <= i)
                    AllLists.emailSortedAccounts.add(null);
                AllLists.emailSortedAccounts.set(i, i);

                while (AllLists.birthSortedAccounts.size() <= i)
                    AllLists.birthSortedAccounts.add(null);
                AllLists.birthSortedAccounts.set(i, i);


                while (AllLists.joinedSortedAccounts.size() <= i)
                    AllLists.joinedSortedAccounts.add(null);
                AllLists.joinedSortedAccounts.set(i, i);

                if(AllLists.allAccounts[i].premiumStart < Runner.curDate && AllLists.allAccounts[i].premiumEnd > Runner.curDate)
                    AllLists.premiumNowAccounts.add(i);

                if(AllLists.allAccounts[i].premiumStart > 0 && AllLists.allAccounts[i].premiumEnd > 0)
                    AllLists.premiumEverAccounts.add(i);
                else
                    AllLists.premiumNeverAccounts.add(i);

            }
        }

        Collections.sort(AllLists.emailSortedAccounts, (p1,p2) -> {
            if(p1 == null && p2 == null)
                return 0;
            if(p1 == null)
                return -1;
            if(p2 == null)
                return 1;

            return new String(AllLists.allAccounts[p1].email).compareTo(new String(AllLists.allAccounts[p2].email));
        });

        Collections.sort(AllLists.birthSortedAccounts, (p1,p2) -> {
            if(p1 == null && p2 == null)
                return 0;
            if(p1 == null)
                return -1;
            if(p2 == null)
                return 1;

            return AllLists.allAccounts[p1].birth - AllLists.allAccounts[p2].birth;
        });
        int year = 1931;
        for (int i = 0; i < AllLists.birthSortedAccounts.size(); i++) {
            if(AllLists.birthSortedAccounts.get(i) != null) {
                if(AllLists.allAccounts[AllLists.birthSortedAccounts.get(i)].birth >=Utils.getTimestamp(year)) {
                    while (AllLists.allAccounts[AllLists.birthSortedAccounts.get(i)].birth >= Utils.getTimestamp(year))
                        ++year;

                    AllLists.birthYears[year - 1 - 1930] = i;
                }
            }
        }

        Collections.sort(AllLists.joinedSortedAccounts, (p1,p2) -> {
            if(p1 == null && p2 == null)
                return 0;
            if(p1 == null)
                return -1;
            if(p2 == null)
                return 1;

            return AllLists.allAccounts[p1].joined - AllLists.allAccounts[p2].joined;
        });

        year = 1931;
        for (int i = 0; i < AllLists.joinedSortedAccounts.size(); i++) {
            if(AllLists.joinedSortedAccounts.get(i) != null) {
                if(AllLists.allAccounts[AllLists.joinedSortedAccounts.get(i)].joined >=Utils.getTimestamp(year)) {
                    while (AllLists.allAccounts[AllLists.joinedSortedAccounts.get(i)].joined >= Utils.getTimestamp(year))
                        ++year;

                    AllLists.joinedYears[year - 1 - 1930] = i;
                }
            }
        }


        char search = 'a';
        for (int i = 0; i < AllLists.emailSortedAccounts.size(); i++) {
            if(AllLists.emailSortedAccounts.get(i) != null) {
                if(AllLists.allAccounts[AllLists.emailSortedAccounts.get(i)].email[0] == search) {
                    AllLists.emailFirst[search-'a'] = i;
                    ++search;
                }
            }
        }
    }
}
