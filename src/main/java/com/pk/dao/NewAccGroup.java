package com.pk.dao;

import com.pk.model.Account;
import com.pk.model.AllLists;
import com.pk.model.Group;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.pk.model.AllLists.*;

public class NewAccGroup {

    public boolean getGroups(List<String> group, Boolean sex, Byte status, String interests, Short country,
                             Short city, Integer birth, String likes, Integer joined,
                             int limit, boolean order, StringBuilder buf) {

        boolean groupSex = false;
        boolean groupStatus = false;
        boolean groupInterests = false;
        boolean groupCountry = false;
        boolean groupCity = false;
        for (String g : group) { //sex, status, interests, country, city.
            switch (g) {
                case "sex":
                    groupSex = true;
                    break;
                case "status":
                    groupStatus = true;
                    break;
                case "interests":
                    groupInterests = true;
                    break;
                case "country":
                    groupCountry = true;
                    break;
                case "city":
                    groupCity = true;
                    break;
                default:
                    return false;
            }
        }

        //bicycle for only interests
        if (groupInterests && !groupCity && !groupSex && !groupCountry && !groupStatus) {
            if (likes == null && interests == null && birth == null && joined == null && sex == null && status == null && country == null && city == null) {
                calcReturnPreCalcInterests(limit, order, buf);
                return true;
            }
        }

        //bicycle for birth city interests
        if(likes==null && joined==null && sex==null && status==null && country==null &&
            groupCountry==false && groupSex==false && groupStatus==false && (groupInterests || interests != null)) {

            int birthYear = Integer.MAX_VALUE;
            if (birth != null) {
                birthYear = birth;
                if (birthYear > MAX_BIRTH_YEAR)
                    return true;
                if (birthYear < MIN_BIRTH_YEAR)
                    return true;
            }

            buildBirthCityInterestAnwer(birthYear, city, interests, groupCity, groupInterests, limit, buf, order);
            return true;
        }


        //bicycle for birth country interests
        if(likes==null && joined==null && sex==null && status==null && city==null &&
                groupCity==false && groupSex==false && groupStatus==false && (groupInterests || interests != null)) {

            int birthYear = Integer.MAX_VALUE;
            if (birth != null) {
                birthYear = birth;
                if (birthYear > MAX_BIRTH_YEAR)
                    return true;
                if (birthYear < MIN_BIRTH_YEAR)
                    return true;
            }

            buildBirthCountryInterestAnwer(birthYear, country, interests, groupCountry, groupInterests, limit, buf, order);
            return true;
        }


        //new code based on new groupFilter no filter for now
        if (likes == null && interests == null && !groupInterests && ((birth == null && joined == null) ||
                (birth != null && joined == null) ||
                (birth == null && joined != null))) {

            int birthYear = Integer.MAX_VALUE;
            if (birth != null) {
                birthYear = birth;
                if (birthYear > MAX_BIRTH_YEAR)
                    return true;
                if (birthYear < MIN_BIRTH_YEAR)
                    return true;
            }

            int joinedYear = Integer.MAX_VALUE;
            if (joined != null) {
                joinedYear = joined;
                if (joinedYear > MAX_JOINED_YEAR)
                    return true;
                if (joinedYear < MIN_JOINED_YEAR)
                    return true;
            }

            int lastCountry = countriesList.size() - 1;
            int lastCity = citiesList.size() - 1;
            int lastSex = 1;
            int lastStatus = 2;

            int startSex = 0;
            if (sex != null) {
                startSex = sex ? 1 : 0;
                lastSex = startSex;
            }

            int startStatus = 0;
            if (status != null) {
                startStatus = status - 1;
                lastStatus = status - 1;
            }

            int startCountry = 0;
            if (country != null) {
                startCountry = country;
                lastCountry = country;
            }

            int startCity = 0;
            if (city != null) {
                startCity = city;
                lastCity = city;
            }

            TreeSet<int[]> groupData;
            if (order) {
                groupData = new TreeSet<>(
                        (p1, p2) -> {
                            if (p1[0] != p2[0])
                                return p1[0] - p2[0];

                            if (p1[3] != p2[3])
                                return p1[3] - p2[3];

                            if (p1[4] != p2[4])
                                return p1[4] - p2[4];

                            if (p1[2] != p2[2])
                                return Utils.getStatusText((byte) (p1[2] + 1)).compareTo(Utils.getStatusText((byte) (p2[2] + 1)));

                            if (p1[1] != p2[1])
                                return p1[1] - p2[1];
                            return 0;
                        });
            } else {
                groupData = new TreeSet<>(
                        (p2, p1) -> {
                            if (p1[0] != p2[0])
                                return p1[0] - p2[0];

                            if (p1[3] != p2[3])
                                return p1[3] - p2[3];

                            if (p1[4] != p2[4])
                                return p1[4] - p2[4];

                            if (p1[2] != p2[2])
                                return Utils.getStatusText((byte) (p1[2] + 1)).compareTo(Utils.getStatusText((byte) (p2[2] + 1)));

                            if (p1[1] != p2[1])
                                return p1[1] - p2[1];
                            return 0;
                        });
            }

            int tempCountry = startCountry;
            int tempCity = startCity;
            int tempStatus = startStatus;
            int tempSex = startSex;

            int curSum = 0;

            if (!groupCity) {
                generateNonCityGroup(birth, joined, groupSex, groupStatus, groupCountry, birthYear, joinedYear, lastCountry,
                        lastSex, lastStatus, startSex, startStatus, startCountry, (startCity == 0 ? -1 : startCity), groupData, tempCountry,
                        tempStatus, tempSex, curSum);
            } else {
                if (groupCity && !groupCountry)
                    generateCityNonCountry(birth, joined, groupSex, groupStatus, birthYear, joinedYear, lastCountry,
                            lastSex, lastStatus, startSex, startStatus, startCountry, (startCity == 0 ? -1 : startCity), groupData, tempCountry,
                            tempStatus, tempSex, curSum);
                else
                    generateCommon(birth, joined, groupSex, groupStatus, groupCountry, groupCity, birthYear, joinedYear, lastCountry, lastCity,
                            lastSex, lastStatus, startSex, startStatus, startCountry, startCity, groupData, tempCountry, tempCity, tempStatus,
                            tempSex, curSum);
            }
            buildAnswer(groupData, limit, groupSex, groupStatus, groupCountry, groupCity, buf);
            return true;
        }

        //OLD CODE
        Integer interestId = null;
        int[] list = null;
        List<Integer> listList = null;
        if (interests != null) {
            interestId = AllLists.interests.get(interests);
            list = AllLists.interestAccounts[interestId];
        }

        if (city != null) {
            if (list == null || list.length > AllLists.cityAccounts[city].length) {
                list = AllLists.cityAccounts[city];
            }
        }

        if (country != null) {
            if (list == null || list.length > AllLists.countryAccounts[country].length) {
                list = AllLists.countryAccounts[country];
            }
        }

        if (birth != null) {
            if (birth > MAX_BIRTH_YEAR || birth < MIN_BIRTH_YEAR)
                return true;

            if (birthYearsAccount[birth - MIN_BIRTH_YEAR] == null)
                return true;

            if (list == null || list.length >= birthYearsAccount[birth - MIN_BIRTH_YEAR].length) {
                list = birthYearsAccount[birth - MIN_BIRTH_YEAR];
            }
        }

        Integer likeId = null;
        Set<Integer> searchLikesSet = null;
        if (likes != null) {
            likeId = Integer.parseInt(likes);
            if (AllLists.likesTO[likeId] == null)
                return true;

            if (likeId > AllLists.likesTO.length) {
                return true;
            }
            list = AllLists.likesTO[likeId];
            searchLikesSet = new HashSet<>(AllLists.likesTO[likeId].length);
            for (int like : AllLists.likesTO[likeId]) {
                searchLikesSet.add(like);
            }
        }

        if (listList == null && list == null)
            return true;

        int bStart = 0;
        int bEnd = 0;
        if (birth != null) {
            bStart = getTimestamp(birth);
            bEnd = getTimestamp(birth + 1) - 1;
        }

        int jStart = 0;
        int jEnd = 0;
        if (joined != null) {
            jStart = getTimestamp(joined);
            jEnd = getTimestamp(joined + 1) - 1;
        }

        HashMap<Integer, Group> formedGroups = new HashMap<>(100);
        int start = 0;
        int end = list == null ? listList.size() : list.length;
        int cur = start;

        boolean isCity = city != null;
        boolean isCountry = country != null;
        boolean isSex = sex != null;
        boolean isStatus = status != null;
        boolean isBirth = birth != null;
        boolean isJoined = joined != null;
        boolean isInterests = interests != null;
        boolean isLikes = likes != null;

        while (cur < end) {
            Integer possibleId = list == null ? listList.get(cur) : list[cur];
            ++cur;

            if (possibleId == null || possibleId == 0)
                continue;

            Account possible = AllLists.allAccounts[possibleId];

            if (isCity) {
                if (possible.city == 0 || possible.city != city)
                    continue;
            }

            if (isCountry) {
                if (possible.country == 0 || possible.country != country)
                    continue;
            }

            if (isSex) {
                if (possible.sex != sex)
                    continue;
            }

            if (isStatus) {
                if (possible.status != status)
                    continue;
            }

            if (isBirth) {
                if (possible.birth == 0 || possible.birth < bStart || possible.birth > bEnd)
                    continue;
            }

            if (isJoined) {
                if (possible.joined < jStart || possible.joined > jEnd)
                    continue;
            }

            if (isInterests) {
                if (possible.interests == null || !possible.interests.contains(interestId))
                    continue;
            }

            if (isLikes && listList == null) {
                if (searchLikesSet != null && !searchLikesSet.contains(possible.id)) {
                    continue;
                }
            }

            //sex, status, interests, country, city
            addToGroup(formedGroups, possible, groupSex, groupStatus, groupCountry, groupCity, groupInterests);
        }

        Group[] sorted = sortGroups(formedGroups, order);
        buildAnswer(sorted, limit, groupSex, groupStatus, groupCountry, groupCity, groupInterests, buf);
        return true;
    }

    private void buildBirthCityInterestAnwer(int birthYear, Short city, String interests, boolean groupCity, boolean groupInterests,
                                             int limit, StringBuilder buf, boolean order) {

        TreeSet<int[]> groupData;
        if (order) {
            groupData = new TreeSet<>(
                    (p1, p2) -> {
                        if (p1[0] != p2[0])
                            return p1[0] - p2[0];

                        if (p1[1] != p2[1])
                            return p1[1] - p2[1];

                        if (p1[2]!= 0 && p2[2] != 0 && AllLists.interestsById.get(p1[2]).compareTo(AllLists.interestsById.get(p2[2])) != 0)
                            return AllLists.interestsById.get(p1[2]).compareTo(AllLists.interestsById.get(p2[2]));

                        return 0;
                    });
        } else {
            groupData = new TreeSet<>(
                    (p2, p1) -> {
                        if (p1[0] != p2[0])
                            return p1[0] - p2[0];

                        if (p1[1] != p2[1])
                            return p1[1] - p2[1];

                        if (p1[2]!= 0 && p2[2] != 0 && AllLists.interestsById.get(p1[2]).compareTo(AllLists.interestsById.get(p2[2])) != 0)
                            return AllLists.interestsById.get(p1[2]).compareTo(AllLists.interestsById.get(p2[2]));

                        return 0;
                    });
        }

        int curCity = city==null?0:city;
        int curYear = birthYear==Integer.MAX_VALUE?0:birthYear-MIN_BIRTH_YEAR;
        int curInterest = interests==null?0:AllLists.interests.get(interests);

        int lastCity = city==null?citiesList.size()-1:city;
        int lastInterest = interests==null?interestsById.size():AllLists.interests.get(interests);
        int lastYear = birthYear==Integer.MAX_VALUE?MAX_BIRTH_YEAR-MIN_BIRTH_YEAR-1:birthYear-MIN_BIRTH_YEAR;

        int startInterest = curInterest;
        int startYear = curYear;
        int startCity = curCity;

        int curSum = 0;
        while (true) {

            if (groupFilterBirthCityInterests[curYear][curCity] != null) {
                curSum += groupFilterBirthCityInterests[curYear][curCity][curInterest];
            }

            if(curYear < lastYear) {
                ++curYear;

                while(curYear < lastYear && groupFilterBirthCityInterests[curYear][curCity] == null)
                    ++curYear;

                continue;
            }

            if (!groupInterests && curInterest < lastInterest) {
                ++curInterest;

                curYear = startYear;

                continue;
            }

            if (!groupCity && curCity < lastCity) {
                ++curCity;

                curYear = startYear;

                while(curCity < lastCity && groupFilterBirthCityInterests[curYear][curCity] == null)
                    ++curCity;

                if (!groupInterests)
                    curInterest = startInterest;
                continue;
            }

            if (curSum > 0) {
                int[] valToInsert = new int[3];
                valToInsert[0] = curSum;
                valToInsert[1] = groupCity ? curCity : 0;
                valToInsert[2] = groupInterests ? curInterest : 0;
                groupData.add(valToInsert);
                curSum = 0;
            }

            if (groupInterests && curInterest < lastInterest) {
                ++curInterest;

                if (!groupCity)
                    curCity = startCity;

                curYear = startYear;

                continue;
            }

            if (groupCity && curCity < lastCity) {
                ++curCity;

                if (!groupInterests)
                    curInterest = startInterest;

                curYear = startYear;

                continue;
            }
            break;
        }

        buildCityInteresAnwer(groupData, limit, groupCity, groupInterests, buf);
    }

    private void buildBirthCountryInterestAnwer(int birthYear, Short country, String interests, boolean groupCountry, boolean groupInterests,
                                             int limit, StringBuilder buf, boolean order) {

        TreeSet<int[]> groupData;
        if (order) {
            groupData = new TreeSet<>(
                    (p1, p2) -> {
                        if (p1[0] != p2[0])
                            return p1[0] - p2[0];

                        if (p1[1] != p2[1])
                            return p1[1] - p2[1];

                        if (p1[2]!= 0 && p2[2] != 0 && AllLists.interestsById.get(p1[2]).compareTo(AllLists.interestsById.get(p2[2])) != 0)
                            return AllLists.interestsById.get(p1[2]).compareTo(AllLists.interestsById.get(p2[2]));

                        return 0;
                    });
        } else {
            groupData = new TreeSet<>(
                    (p2, p1) -> {
                        if (p1[0] != p2[0])
                            return p1[0] - p2[0];

                        if (p1[1] != p2[1])
                            return p1[1] - p2[1];

                        if (p1[2]!= 0 && p2[2] != 0 && AllLists.interestsById.get(p1[2]).compareTo(AllLists.interestsById.get(p2[2])) != 0)
                            return AllLists.interestsById.get(p1[2]).compareTo(AllLists.interestsById.get(p2[2]));

                        return 0;
                    });
        }

        int curCountry = country==null?0:country;
        int curYear = birthYear==Integer.MAX_VALUE?0:birthYear-MIN_BIRTH_YEAR;
        int curInterest = interests==null?0:AllLists.interests.get(interests);

        int lastCountry = country==null?countriesList.size()-1:country;
        int lastInterest = interests==null?interestsById.size():AllLists.interests.get(interests);
        int lastYear = birthYear==Integer.MAX_VALUE?MAX_BIRTH_YEAR-MIN_BIRTH_YEAR-1:birthYear-MIN_BIRTH_YEAR;

        int startInterest = curInterest;
        int startYear = curYear;
        int startCountry = curCountry;

        int curSum = 0;
        while (true) {

            if (groupFilterBirthCountryInterests[curYear][curCountry] != null) {
                curSum += groupFilterBirthCountryInterests[curYear][curCountry][curInterest];
            }

            if(curYear < lastYear) {
                ++curYear;

                while(curYear < lastYear && groupFilterBirthCountryInterests[curYear][curCountry] == null)
                    ++curYear;

                continue;
            }

            if (!groupInterests && curInterest < lastInterest) {
                ++curInterest;

                curYear = startYear;

                continue;
            }

            if (!groupCountry && curCountry < lastCountry) {
                ++curCountry;

                curYear = startYear;

                while(curCountry < lastCountry && groupFilterBirthCountryInterests[curYear][curCountry] == null)
                    ++curCountry;

                if (!groupInterests)
                    curInterest = startInterest;
                continue;
            }

            if (curSum > 0) {
                int[] valToInsert = new int[3];
                valToInsert[0] = curSum;
                valToInsert[1] = groupCountry ? curCountry : 0;
                valToInsert[2] = groupInterests ? curInterest : 0;
                groupData.add(valToInsert);
                curSum = 0;
            }

            if (groupInterests && curInterest < lastInterest) {
                ++curInterest;

                if (!groupCountry)
                    curCountry = startCountry;

                curYear = startYear;

                continue;
            }

            if (groupCountry && curCountry < lastCountry) {
                ++curCountry;

                if (!groupInterests)
                    curInterest = startInterest;

                curYear = startYear;

                continue;
            }
            break;
        }

        buildCountryInteresAnwer(groupData, limit, groupCountry, groupInterests, buf);
    }

    private void buildCountryInteresAnwer(TreeSet<int[]> groups, int limit, boolean groupCountry, boolean groupInterests, StringBuilder buf) {
        int pointer = 0;
        boolean isFirst = true;
        while (!groups.isEmpty() && pointer < limit) {

            int[] group = groups.pollFirst();
            if (isFirst) {
                buf.append("{\"count\":");
                isFirst = false;
            } else
                buf.append(",{\"count\":");

            buf.append(group[0]);

            if (groupCountry && group[1] != 0) {
                buf.append(",\"country\":\"");
                buf.append(AllLists.countriesList.get(group[1]));
                buf.append("\"");
            }
            if (groupInterests && group[2] != 0) {
                buf.append(",\"interests\":\"");
                buf.append(AllLists.interestsById.get(group[2]));
                buf.append("\"");
            }

            buf.append("}");
            ++pointer;
        }
    }



    private void buildCityInteresAnwer(TreeSet<int[]> groups, int limit, boolean groupCity, boolean groupInterests, StringBuilder buf) {
        int pointer = 0;
        boolean isFirst = true;
        while (!groups.isEmpty() && pointer < limit) {

            int[] group = groups.pollFirst();
            if (isFirst) {
                buf.append("{\"count\":");
                isFirst = false;
            } else
                buf.append(",{\"count\":");

            buf.append(group[0]);

            if (groupCity && group[1] != 0) {
                buf.append(",\"city\":\"");
                buf.append(AllLists.citiesList.get(group[1]));
                buf.append("\"");
            }
            if (groupInterests && group[2] != 0) {
                buf.append(",\"interests\":\"");
                buf.append(AllLists.interestsById.get(group[2]));
                buf.append("\"");
            }

            buf.append("}");
            ++pointer;
        }
    }

    private void generateCommon(Integer birth, Integer joined, boolean groupSex, boolean groupStatus, boolean groupCountry, boolean groupCity,
                                int birthYear, int joinedYear, int lastCountry, int lastCity, int lastSex, int lastStatus, int startSex,
                                int startStatus, int startCountry, int startCity, TreeSet<int[]> groupData, int tempCountry, int tempCity,
                                int tempStatus, int tempSex, int curSum) {
        while (true) {

            if (groupFilterBirth[tempCountry].get(shortCache[tempCity]) != null) {
                if (birth != null) {
                    curSum += groupFilterBirth[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex][birthYear - MIN_BIRTH_YEAR];
                } else {
                    if (joined != null) {
                        curSum += groupFilterJoined[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex][joinedYear - MIN_JOINED_YEAR];
                    } else {
                        curSum += groupFilter[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex];
                    }
                }
            }

            if (!groupSex && tempSex < lastSex) {
                ++tempSex;
                continue;
            }
            if (!groupStatus && tempStatus < lastStatus) {
                if (!groupSex)
                    tempSex = startSex;

                ++tempStatus;
                continue;
            }

            if (!groupCity && tempCity < lastCity) {
                ++tempCity;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                continue;
            }

            if (!groupCountry && tempCountry < lastCountry) {
                ++tempCountry;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                if (!groupCity)
                    tempCity = startCity;
                continue;
            }

            if (curSum > 0) {
                int[] valToInsert = new int[6];
                valToInsert[0] = curSum;
                valToInsert[1] = groupSex ? tempSex : 0;
                valToInsert[2] = groupStatus ? tempStatus : 0;
                valToInsert[3] = groupCountry ? tempCountry : 0;
                valToInsert[4] = groupCity ? tempCity : 0;
                valToInsert[5] = 0;
                groupData.add(valToInsert);
                curSum = 0;
            }

            if (groupSex && tempSex < lastSex) {
                ++tempSex;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                if (!groupCity)
                    tempCity = startCity;
                if (!groupCountry) {
                    tempCountry = startCountry;
                }

                continue;
            }
            if (groupStatus && tempStatus < lastStatus) {
                if (groupSex)
                    tempSex = startSex;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                if (!groupCity)
                    tempCity = startCity;
                if (!groupCountry) {
                    tempCountry = startCountry;
                }

                ++tempStatus;
                continue;
            }

            if (groupCity && tempCity < lastCity) {
                ++tempCity;

                if (groupSex)
                    tempSex = startSex;
                if (groupStatus)
                    tempStatus = startStatus;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                if (!groupCity)
                    tempCity = startCity;
                if (!groupCountry) {
                    tempCountry = startCountry;
                }

                continue;
            }

            if (groupCountry && tempCountry < lastCountry) {
                ++tempCountry;

                if (groupSex)
                    tempSex = startSex;
                if (groupStatus)
                    tempStatus = startStatus;
                if (groupCity)
                    tempCity = startCity;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                if (!groupCity)
                    tempCity = startCity;
                if (!groupCountry) {
                    tempCountry = startCountry;
                }

                continue;
            }

            break;
        }
    }

    private void generateCityNonCountry(Integer birth, Integer joined, boolean groupSex, boolean groupStatus,
                                        int birthYear, int joinedYear, int lastCountry, int lastSex, int lastStatus, int startSex,
                                        int startStatus, int startCountry, int filterCity, TreeSet<int[]> groupData, int tempCountry,
                                        int tempStatus, int tempSex, int curSum) {

        int lastCity = citiesList.size() - 1;
        int tempCity = 0;
        while (true) {

            if ((filterCity == -1 || filterCity == tempCity) && groupFilterBirth[tempCountry].get(shortCache[tempCity]) != null) {
                if (birth != null) {
                    curSum += groupFilterBirth[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex][birthYear - MIN_BIRTH_YEAR];
                } else {
                    if (joined != null) {
                        curSum += groupFilterJoined[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex][joinedYear - MIN_JOINED_YEAR];
                    } else {
                        curSum += groupFilter[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex];
                    }
                }
            }

            if (!groupSex && tempSex < lastSex) {
                ++tempSex;
                continue;
            }
            if (!groupStatus && tempStatus < lastStatus) {
                if (!groupSex)
                    tempSex = startSex;

                ++tempStatus;
                continue;
            }

            if (tempCountry < lastCountry) {
                ++tempCountry;

                while (tempCountry < lastCountry && groupFilterBirth[tempCountry].get(shortCache[tempCity]) == null)
                    ++tempCountry;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                continue;
            }

            if (curSum > 0) {
                int[] valToInsert = new int[6];
                valToInsert[0] = curSum;
                valToInsert[1] = groupSex ? tempSex : 0;
                valToInsert[2] = groupStatus ? tempStatus : 0;
                valToInsert[3] = 0;
                valToInsert[4] = tempCity;
                valToInsert[5] = 0;
                groupData.add(valToInsert);
                curSum = 0;
            }

            if (groupSex && tempSex < lastSex) {
                ++tempSex;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                tempCountry = startCountry;

                while (tempCountry < lastCountry && groupFilterBirth[tempCountry].get(shortCache[tempCity]) == null)
                    ++tempCountry;

                continue;
            }
            if (groupStatus && tempStatus < lastStatus) {
                if (groupSex)
                    tempSex = startSex;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;

                tempCountry = startCountry;

                while (tempCountry < lastCountry && groupFilterBirth[tempCountry].get(shortCache[tempCity]) == null)
                    ++tempCountry;

                ++tempStatus;
                continue;
            }

            if (tempCity < lastCity) {
                ++tempCity;

                if (groupSex)
                    tempSex = startSex;
                if (groupStatus)
                    tempStatus = startStatus;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                tempCountry = startCountry;

                while (tempCountry < lastCountry && groupFilterBirth[tempCountry].get(shortCache[tempCity]) == null)
                    ++tempCountry;

                continue;
            }

            break;
        }
    }

    private void generateNonCityGroup(Integer birth, Integer joined, boolean groupSex, boolean groupStatus, boolean groupCountry, int birthYear,
                                      int joinedYear, int lastCountry, int lastSex, int lastStatus, int startSex, int startStatus,
                                      int startCountry, int filterCity, TreeSet<int[]> groupData, int tempCountry, int tempStatus,
                                      int tempSex, int curSum) {

        Set<Short> curCountryCityList = groupFilterBirth[tempCountry].keySet();
        Iterator<Short> cityIterator = curCountryCityList.iterator();
        int tempCity = cityIterator.next();
        while (true) {

            while (filterCity != -1 && tempCity != filterCity && cityIterator.hasNext())
                tempCity = cityIterator.next();

            if ((filterCity == -1 || tempCity == filterCity)) {

                if (groupFilterBirth[tempCountry].get(shortCache[tempCity]) != null) {
                    if (birth != null) {
                        curSum += groupFilterBirth[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex][birthYear - MIN_BIRTH_YEAR];
                    } else {
                        if (joined != null) {
                            curSum += groupFilterJoined[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex][joinedYear - MIN_JOINED_YEAR];
                        } else {
                            curSum += groupFilter[tempCountry].get(shortCache[tempCity])[tempStatus][tempSex];
                        }
                    }
                }

                if (!groupSex && tempSex < lastSex) {
                    ++tempSex;
                    continue;
                }
                if (!groupStatus && tempStatus < lastStatus) {
                    if (!groupSex)
                        tempSex = startSex;

                    ++tempStatus;
                    continue;
                }
            }

            if (cityIterator.hasNext()) {
                tempCity = cityIterator.next();

                while (filterCity != -1 && tempCity != filterCity && cityIterator.hasNext())
                    tempCity = cityIterator.next();

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                continue;
            }

            if (!groupCountry && tempCountry < lastCountry) {
                ++tempCountry;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;

                curCountryCityList = groupFilter[tempCountry].keySet();
                cityIterator = curCountryCityList.iterator();
                tempCity = cityIterator.next();
                continue;
            }

            if (curSum > 0) {
                int[] valToInsert = new int[6];
                valToInsert[0] = curSum;
                valToInsert[1] = groupSex ? tempSex : 0;
                valToInsert[2] = groupStatus ? tempStatus : 0;
                valToInsert[3] = groupCountry ? tempCountry : 0;
                valToInsert[4] = 0;
                valToInsert[5] = 0;
                groupData.add(valToInsert);
                curSum = 0;
            }

            if (groupSex && tempSex < lastSex) {
                ++tempSex;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                if (!groupCountry) {
                    tempCountry = startCountry;
                }

                curCountryCityList = groupFilter[tempCountry].keySet();
                cityIterator = curCountryCityList.iterator();
                tempCity = cityIterator.next();

                continue;
            }
            if (groupStatus && tempStatus < lastStatus) {
                if (groupSex)
                    tempSex = startSex;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;
                if (!groupCountry) {
                    tempCountry = startCountry;
                }

                curCountryCityList = groupFilter[tempCountry].keySet();
                cityIterator = curCountryCityList.iterator();
                tempCity = cityIterator.next();

                ++tempStatus;
                continue;
            }

            if (groupCountry && tempCountry < lastCountry) {
                ++tempCountry;

                if (groupSex)
                    tempSex = startSex;
                if (groupStatus)
                    tempStatus = startStatus;

                if (!groupSex)
                    tempSex = startSex;
                if (!groupStatus)
                    tempStatus = startStatus;

                curCountryCityList = groupFilter[tempCountry].keySet();
                cityIterator = curCountryCityList.iterator();
                tempCity = cityIterator.next();

                continue;
            }

            break;
        }
    }


    private void buildAnswer(TreeSet<int[]> groups, int limit,
                             boolean groupSex, boolean groupStatus, boolean groupCountry, boolean groupCity, StringBuilder buf) {
        int pointer = 0;
        boolean isFirst = true;
        while (!groups.isEmpty() && pointer < limit) {
            //Group group = sorted[pointer];
            int[] group = groups.pollFirst();
            if (isFirst) {
                buf.append("{\"count\":");
                isFirst = false;
            } else
                buf.append(",{\"count\":");

            buf.append(group[0]);

            if (groupCountry && group[3] != 0) {
                buf.append(",\"country\":\"");
                buf.append(AllLists.countriesList.get(group[3]));
                buf.append("\"");
            }
            if (groupCity && group[4] != 0) {
                buf.append(",\"city\":\"");
                buf.append(AllLists.citiesList.get(group[4]));
                buf.append("\"");
            }
            if (groupStatus) {
                buf.append(",\"status\":\"");
                buf.append(Utils.getStatusText((byte) (group[2] + 1)));
                buf.append("\"");
            }
            if (groupSex) {
                String sexStr = (group[1] == 1 ? "m" : "f");
                buf.append(",\"sex\":\"");
                buf.append(sexStr);
                buf.append("\"");
            }

            buf.append("}");
            ++pointer;
        }
    }


    private void buildAnswer(Group[] sorted, int limit,
                             boolean groupSex, boolean groupStatus, boolean groupCountry, boolean groupCity, boolean groupInterests, StringBuilder buf) {
        int pointer = 0;
        boolean isFirst = true;
        while (pointer < sorted.length && pointer < limit) {
            Group group = sorted[pointer];
            if (isFirst) {
                buf.append("{\"count\":");
                isFirst = false;
            } else
                buf.append(",{\"count\":");

            buf.append(sorted[pointer].count);

            if (groupCountry && group.country != 0) {
                buf.append(",\"country\":\"");
                buf.append(AllLists.countriesList.get(group.country));
                buf.append("\"");
            }
            if (groupCity && group.city != 0) {
                buf.append(",\"city\":\"");
                buf.append(AllLists.citiesList.get(group.city));
                buf.append("\"");
            }
            if (groupStatus && group.status != 0) {
                buf.append(",\"status\":\"");
                buf.append(Utils.getStatusText(group.status));
                buf.append("\"");
            }
            if (groupSex) {
                String sexStr = group.sex ? "m" : "f";
                buf.append(",\"sex\":\"");
                buf.append(sexStr);
                buf.append("\"");
            }
            if (groupInterests && group.interest != 0) {
                buf.append(",\"interests\":\"");
                buf.append(AllLists.interestsById.get(group.interest));
                buf.append("\"");
            }
            buf.append("}");
            ++pointer;
        }
    }

    private Group[] sortGroups(HashMap<Integer, Group> hm, boolean order) {

        Group[] formedGroups = hm.values().toArray(new Group[0]);

        if (order) {
            Arrays.sort(formedGroups, (Group o1, Group o2) -> {
                if (o1.count != o2.count)
                    return o1.count - o2.count;

                if (o1.country != o2.country)
                    return o1.country - o2.country;

                if (o1.city != o2.city)
                    return o1.city - o2.city;

                if (o1.status != o2.status)
                    return Utils.getStatusText(o1.status).compareTo(Utils.getStatusText(o2.status));

                if (o1.sex != o2.sex)
                    return (o1.sex ? 1 : 0) - (o2.sex ? 1 : 0);

                return AllLists.interestsById.get(o1.interest).compareTo(AllLists.interestsById.get(o2.interest));
            });
        } else {
            Arrays.sort(formedGroups, (Group o2, Group o1) -> {
                if (o1.count != o2.count)
                    return o1.count - o2.count;
                if (o1.country != o2.country)
                    return o1.country - o2.country;
                if (o1.city != o2.city)
                    return o1.city - o2.city;
                if (o1.status != o2.status)
                    return Utils.getStatusText(o1.status).compareTo(Utils.getStatusText(o2.status));
                if (o1.sex != o2.sex)
                    return (o1.sex ? 1 : 0) - (o2.sex ? 1 : 0);

                return AllLists.interestsById.get(o1.interest).compareTo(AllLists.interestsById.get(o2.interest));
            });
        }
        return formedGroups;
    }

    private void addToGroup(HashMap<Integer, Group> groups, Account account, boolean groupSex, boolean groupStatus,
                            boolean groupCountry, boolean groupCity, boolean groupInterests) {

        if (groupInterests && account.interests == null)
            return;

        int hash = 0;
        if (groupSex && account.sex) {
            hash = 1;
        }
        if (groupStatus) {
            hash |= account.status << 1;
        }
        int moveCityIndex = 3;
        if (groupCountry) {
            hash |= (int) account.country << 3;
            moveCityIndex += 10;
        }

        int moveInterestsIndex = moveCityIndex;
        if (groupCity) {
            hash |= (int) account.city << moveCityIndex;
            moveInterestsIndex += 10;
        }

        if (!groupInterests) {
            if (groups.containsKey(hash)) {
                ++groups.get(hash).count;
            } else {
                Group g = new Group();
                g.count = 1;
                g.sex = groupSex && account.sex;
                g.status = groupStatus ? account.status : 0;
                g.country = groupCountry ? account.country : 0;
                g.city = groupCity ? account.city : 0;
                groups.put(hash, g);
            }
        } else {
            for (int interest : account.interestsArray) {
                int interestHash = hash | interest << moveInterestsIndex;

                if (groups.containsKey(interestHash)) {
                    ++groups.get(interestHash).count;
                } else {
                    Group g = new Group();
                    g.count = 1;
                    g.sex = groupSex && account.sex;
                    g.status = groupStatus ? account.status : 0;
                    g.country = groupCountry ? account.country : 0;
                    g.city = groupCity ? account.city : 0;
                    g.interest = interest;
                    groups.put(interestHash, g);
                }
            }
        }
    }

    private int getTimestamp(int year) {
        if (AllLists.yearToTs[year - 1930] != 0)
            return AllLists.yearToTs[year - 1930];
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;
        try {
            date = dateFormat.parse("01/01/" + year);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time = date.getTime();
        AllLists.yearToTs[year - 1930] = (int) (time / 1000);
        return (int) (time / 1000);
    }

    private void calcReturnPreCalcInterests(int limit, boolean order, StringBuilder buf) {
        if (order) {
            int pointer = 0;
            boolean isFirst = true;
            while (pointer < limit) {
                if (isFirst) {
                    buf.append("{\"count\":");
                    isFirst = false;
                } else
                    buf.append(",{\"count\":");

                buf.append(topInterests50[pointer][1]);
                buf.append(",\"interests\":\"");
                buf.append(AllLists.interestsById.get(topInterests50[pointer][0]));
                buf.append("\"");
                buf.append("}");
                ++pointer;
            }
        } else {
            int pointer = 0;
            boolean isFirst = true;
            while (pointer < limit) {
                if (isFirst) {
                    buf.append("{\"count\":");
                    isFirst = false;
                } else
                    buf.append(",{\"count\":");

                buf.append(lowInterests50[pointer][1]);
                buf.append(",\"interests\":\"");
                buf.append(AllLists.interestsById.get(lowInterests50[pointer][0]));
                buf.append("\"");
                buf.append("}");
                ++pointer;
            }
        }
    }
}
