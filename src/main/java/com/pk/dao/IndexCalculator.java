package com.pk.dao;

import com.pk.Runner;
import com.pk.model.Account;
import com.pk.model.AllLists;
import com.pk.model.PostLists;

import java.util.*;

import static com.pk.model.AllLists.*;
import static com.pk.model.AllLists.fnames;
import static com.pk.model.AllLists.snames;
import static com.pk.model.PostLists.*;

public class IndexCalculator {


    public void calculateIndexes() {

        if(!Runner.isWarm)
            System.out.println("START RECALC INDEX " + Calendar.getInstance().getTimeInMillis());

        Thread t = new Thread(() -> {
            updatePremiumLists();
            //System.gc();
            if(!Runner.isWarm)
                System.out.println("updatePremiumLists complete " + Calendar.getInstance().getTimeInMillis());
        });
        t.start();

        Thread t1 = new Thread(() -> {
            sortLikes();
            //System.gc();
            if(!Runner.isWarm)
                System.out.println("sort from to likes complete " + Calendar.getInstance().getTimeInMillis());
        });
        t1.start();

        addNewEmailAndDomains();
        //System.gc();

        if(!Runner.isWarm)
            System.out.println("addNewEmailAndDomains complete " + Calendar.getInstance().getTimeInMillis());

        commitFSnamesEmails();
        //System.gc();

        if(!Runner.isWarm)
            System.out.println("commitFSnamesEmails complete " + Calendar.getInstance().getTimeInMillis());

        //city and country need only to sort;
        sortCountryCityEmails();
        //System.gc();

        if(!Runner.isWarm)
            System.out.println("sortCountryCityEmails complete " + Calendar.getInstance().getTimeInMillis());

        if(isNewEmailDomain || isNewCity || isNewCountry)
            createCountryCityDomainsPhoneCodesAccountArrays();
        //System.gc();

        if(!Runner.isWarm)
            System.out.println("createCountryCityDomainsPhoneCodesAccountArrays complete " + Calendar.getInstance().getTimeInMillis());

        if(!Runner.isWarm)
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

    private void rebuildEmailBorders(HashMap<Integer, Integer> tempId) {

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

            //if (isNewCountry) {
                //set country index
                while (tempCountryAccounts.size() <= account.country)
                    tempCountryAccounts.add(null);

                if (tempCountryAccounts.get(account.country) == null) {
                    tempCountryAccounts.set(account.country, new ArrayList<>());
                }

                tempCountryAccounts.get(account.country).add(account.id);
            //}

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

        for(String ttt: PostLists.newEmails) {
            if(!"".equals(ttt))
                AllLists.allEmailList.add(ttt);
        }


        for(String ttt: PostLists.newEmailDomains) {
            if(!"".equals(ttt))
                AllLists.domainList.add(ttt.toCharArray());
        }
    }

    private void sortCountryCityEmails() {
        ArrayList<String> tempCountryList = null;
        if (isNewCountry) {
            tempCountryList = new ArrayList<>(countriesList);
            Collections.sort(countriesList);
        }

        ArrayList<String> tempCityList = null;
        if (isNewCity) {
            tempCityList = new ArrayList<>(citiesList);
            Collections.sort(citiesList);
        }

        ArrayList<char[]> tempDomainList = null;
        if (isNewEmailDomain) {
            tempDomainList = new ArrayList<>(domainList);
            domainList.sort(Utils::compareCharArr);
        }

        ArrayList<String> tempEmailList = new ArrayList<>(allEmailList);
        Collections.sort(allEmailList);

        char[][] tempFnameList = Arrays.copyOf(fnames, fnames.length);
        Arrays.sort(fnames, Utils::compareCharArr);

        char[][] tempSnameList = Arrays.copyOf(snames, snames.length);
        Arrays.sort(snames, Utils::compareCharArr);

        List<Integer>[] tempFnames = new ArrayList[AllLists.fnames.length];
        List<Integer>[] tempSnames = new ArrayList[AllLists.snames.length];

        HashMap<Integer, List<Integer>> tempBirthYears = new HashMap();

        List<List<Integer>> tempIinterestAccounts = new ArrayList<>();

        if(isNewCountry || isNewCity) {
            //recalc recommend city country indexes
            for (int premiumIndex = 0; premiumIndex < AllLists.recommendInteresFilter.length; ++premiumIndex) {
                for (int statusIndex = 0; statusIndex < AllLists.recommendInteresFilter[premiumIndex].length; ++statusIndex) {

                    HashMap<Integer, HashMap<Integer, int[]>>[] newRecommend = new HashMap[countriesList.size()];

                    for (int countryIndex = 0; countryIndex < AllLists.recommendInteresFilter[premiumIndex][statusIndex].length; ++countryIndex) {

                        short newCountryIndex;
                        if (isNewCountry)
                            newCountryIndex = (short) Collections.binarySearch(countriesList, tempCountryList.get(countryIndex));
                        else
                            newCountryIndex = (short) countryIndex;

                        //HashMap<Integer, HashMap<Integer, int[]>> toCopyCountry = recommendInteresFilter[premiumIndex][statusIndex][countryIndex];
                        newRecommend[newCountryIndex] = new HashMap<>();

                        for (Integer cityIndex : AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].keySet()) {

                            int newCityOndex;

                            if (isNewCity)
                                newCityOndex = Collections.binarySearch(citiesList, tempCityList.get(cityIndex));
                            else
                                newCityOndex = cityIndex;

                            newRecommend[newCountryIndex].put(newCityOndex, AllLists.recommendInteresFilter[premiumIndex][statusIndex][countryIndex].get(cityIndex));
                        }
                    }

                    AllLists.recommendInteresFilter[premiumIndex][statusIndex] = newRecommend;
                }
            }

            //recalc group filter
            HashMap<Short, short[][]>[] newGroup = new HashMap[countriesList.size()];
            HashMap<Short, short[][][]>[] newGroupBirth = new HashMap[countriesList.size()];
            HashMap<Short, short[][][]>[] newGroupJoined = new HashMap[countriesList.size()];

            for (int countryIndex = 0; countryIndex < AllLists.groupFilter.length; ++countryIndex) {

                short newCountryIndex;
                if (isNewCountry)
                    newCountryIndex = (short) Collections.binarySearch(countriesList, tempCountryList.get(countryIndex));
                else
                    newCountryIndex = (short) countryIndex;

                newGroup[newCountryIndex] = new HashMap<>();
                newGroupBirth[newCountryIndex] = new HashMap<>();
                newGroupJoined[newCountryIndex] = new HashMap<>();

                for (Short cityIndex : AllLists.groupFilter[countryIndex].keySet()) {

                    int newCityOndex;

                    if (isNewCity)
                        newCityOndex = Collections.binarySearch(citiesList, tempCityList.get(cityIndex));
                    else
                        newCityOndex = cityIndex;

                    newGroup[newCountryIndex].put((short) newCityOndex, AllLists.groupFilter[countryIndex].get(cityIndex));
                    newGroupBirth[newCountryIndex].put((short) newCityOndex, AllLists.groupFilterBirth[countryIndex].get(cityIndex));
                    newGroupJoined[newCountryIndex].put((short) newCityOndex, AllLists.groupFilterJoined[countryIndex].get(cityIndex));
                }
            }

            //[birth][city][interes] = count
            short[][][] newGroupFilterBirthCityInterests = new short[MAX_BIRTH_YEAR - MIN_BIRTH_YEAR + 1][citiesList.size()][];

            for (int i = 0; i < newGroupFilterBirthCityInterests.length; ++i) {
                for (int cityIndex = 0; cityIndex < groupFilterBirthCityInterests[i].length; ++cityIndex) {
                    int newCityIndex;

                    if (isNewCity && cityIndex > 0)
                        newCityIndex = Collections.binarySearch(citiesList, tempCityList.get(cityIndex));
                    else
                        newCityIndex = cityIndex;

                    newGroupFilterBirthCityInterests[i][newCityIndex] = groupFilterBirthCityInterests[i][cityIndex];
                }
            }

            //[birth][country][interes] = count
            short[][][] newGroupFilterBirthCountryInterests = new short[MAX_BIRTH_YEAR - MIN_BIRTH_YEAR + 1][countriesList.size()][];
            //if(isNewCountry) {
                for (int i = 0; i < newGroupFilterBirthCountryInterests.length; ++i) {
                    for (int countryIndex = 0; countryIndex < groupFilterBirthCountryInterests[i].length; ++countryIndex) {
                        int newCountryIndex;

                        if (isNewCountry && countryIndex > 0)
                            newCountryIndex = Collections.binarySearch(countriesList, tempCountryList.get(countryIndex));
                        else
                            newCountryIndex = countryIndex;

                        newGroupFilterBirthCountryInterests[i][newCountryIndex] = groupFilterBirthCountryInterests[i][countryIndex];
                    }
                }
            //}

            AllLists.groupFilter = newGroup;
            AllLists.groupFilterBirth = newGroupBirth;
            AllLists.groupFilterJoined = newGroupJoined;
            AllLists.groupFilterBirthCityInterests = newGroupFilterBirthCityInterests;
            //if(isNewCountry)
                AllLists.groupFilterBirthCountryInterests = newGroupFilterBirthCountryInterests;

        }


        HashMap<Integer, Integer> tempId = new HashMap<>();

        for (com.pk.model.Account account : allAccounts) {
            if (account != null) {

                if (isNewCountry && account.country > 0)
                    account.country = (short) Collections.binarySearch(countriesList, tempCountryList.get(account.country));

                if (isNewCity && account.city > 0)
                    account.city = (short) Collections.binarySearch(citiesList, tempCityList.get(account.city));

                if (account.email > 0) {
                    int temp = account.email;
                    account.email = Collections.binarySearch(allEmailList, tempEmailList.get(account.email));

                    if(account.email == 29999) {
                        int i =0;
                    }


                    if (isNewEmailDomain)
                        account.emailDomain = Collections.binarySearch(domainList, tempDomainList.get(account.emailDomain), Utils::compareCharArr);
                }

                if (account.fname > 0)
                    account.fname = Utils.getFnameIndexBinary(tempFnameList[account.fname]);

                if (account.sname > 0)
                    account.sname = Utils.getSnameIndexBinary(tempSnameList[account.sname]);

                tempId.put(account.email, account.id);

                if (tempFnames[account.fname] == null)
                    tempFnames[account.fname] = new ArrayList<>();
                if (tempSnames[account.sname] == null)
                    tempSnames[account.sname] = new ArrayList<>();

                tempFnames[account.fname].add(account.id);
                tempSnames[account.sname].add(account.id);

                tempBirthYears.computeIfAbsent(getYear(account.birth), p -> new ArrayList<>());
                tempBirthYears.get(getYear(account.birth)).add(account.id);

                if (account.interestsArray != null) {
                    for (int i = 0; i < account.interestsArray.length; i++) {
                        Integer intId = account.interestsArray[i];

                        //set interests index
                        while (tempIinterestAccounts.size() <= intId)
                            tempIinterestAccounts.add(null);

                        if (tempIinterestAccounts.get(intId) == null) {
                            tempIinterestAccounts.set(intId, new ArrayList<>());
                        }

                        tempIinterestAccounts.get(intId).add(account.id);
                    }
                }
            }
        }

        Thread t = new Thread(() -> {
            birthYearsAccount = new int[MAX_BIRTH_YEAR - MIN_BIRTH_YEAR + 1][];
            for (int year : tempBirthYears.keySet()) {
                birthYearsAccount[year - MIN_BIRTH_YEAR] = tempBirthYears.get(year).stream().mapToInt(Integer::intValue).toArray();
            }

            fnameAccounts = new int[tempFnames.length][];
            snameAccounts = new int[tempSnames.length][];
            for (int i = 0; i < tempFnames.length; ++i) {
                if (tempFnames[i] != null)
                    fnameAccounts[i] = tempFnames[i].stream().mapToInt(Integer::intValue).toArray();
            }

            for (int i = 0; i < tempSnames.length; ++i) {
                if (tempSnames[i] != null)
                    snameAccounts[i] = tempSnames[i].stream().mapToInt(Integer::intValue).toArray();
            }

            interestAccounts = new int[tempIinterestAccounts.size()][];
            for (int i = 0; i < tempIinterestAccounts.size(); i++) {
                List<Integer> interest = tempIinterestAccounts.get(i);
                if (interest != null)
                    interestAccounts[i] = interest.stream().mapToInt(Integer::intValue).toArray();
            }

            rebuildEmailBorders(tempId);
        });
        t.start();
    }

    public void clearTempData() {

        PostLists.fnames.clear();
        //PostLists.fnames = null;

        PostLists.snames.clear();
        //PostLists.snames = null;

        PostLists.usedEmailDomain.clear();
        //PostLists.usedEmailDomain = null;


        PostLists.newEmailDomains.clear();
        //PostLists.newEmailDomains = null;

        PostLists.newEmails.clear();
        //PostLists.newEmails = null;

        PostLists.likesToSort.clear();
        PostLists.likesFromSort.clear();

        if(!Runner.isWarm)
            AllLists.usedEmailDomain = null;
    }

    private final Calendar cal = Calendar.getInstance();
    private int getYear(int timestamp) {
        cal.setTimeInMillis((long)timestamp*1000);
        return cal.get(Calendar.YEAR);
    }

    private void sortLikes() {

        for(int i : PostLists.likesFromSort) {

            int[] toSort = AllLists.likesAccounts.get(i);
            List<int[]> temp = new ArrayList<>();

            for(int j=0; j<toSort.length; j+=2) {
                if(toSort[j] == 0)
                    break;
                temp.add(new int[] {toSort[j], toSort[j+1]});
            }

            temp.sort((p1,p2) -> {
                return p2[0] - p1[0];
            });

            AllLists.likesAccounts.set(i, new int[temp.size()*2]);

            for(int j=1; j<temp.size(); ++j) {
                AllLists.likesAccounts.get(i)[j*2] = temp.get(j)[0];
                AllLists.likesAccounts.get(i)[j*2 + 1] = temp.get(j)[1];
            }
        }


        for(int i : PostLists.likesToSort) {
            if(likesTO[i] == null)
                continue;

            int[] arrToSort = likesTO[i];

            int endSortPos = arrToSort.length-1;
            for (int j = arrToSort.length - 1; j >= 0; --j) {
                if (arrToSort[j] != 0) {
                    endSortPos = j + 1;
                    break;
                }
            }
            Arrays.sort(likesTO[i], 0, endSortPos);
        }
    }

}
