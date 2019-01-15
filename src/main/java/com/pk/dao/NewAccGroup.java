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

        BaseFilter filters = new BaseFilter();

        boolean groupSex = false;
        boolean groupStatus = false;
        boolean groupInterests = false;
        boolean groupCountry = false;
        boolean groupCity = false;
        for(String g : group) { //sex, status, interests, country, city.
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

        //new code based on new groupFilter no filter for now
        if((birth == null && likes == null && joined==null) ||
                (likes == null && birth!=null && joined==null) ||
                (likes == null && birth==null && joined!=null)) {

            int birthYear = Integer.MAX_VALUE;
            if (birth != null) {
                birthYear = birth;
                if(birthYear > MAX_BIRTH_YEAR)
                    return true;
            }

            int joinedYear = Integer.MAX_VALUE;
            if (joined != null) {
                joinedYear = joined;
                if(joinedYear > MAX_JOINED_YEAR)
                    return true;
            }

            int lastCountry = countriesList.size() - 1;
            int lastSex = 1;
            int lastStatus = 2;

            int startSex = 0;
            if (sex != null) {
                startSex = sex?1:0;
                lastSex = startSex;
            }

            int startStatus = 0;
            if (status != null) {
                startStatus = status-1;
                lastStatus = status-1;
            }

            int startCountry = 0;
            if (country != null) {
                startCountry = country;
                lastCountry = country;
            }

            int startCity = 0;
            if (city != null) {
                startCity = city;
            }

            TreeSet<int[]> groupData;
            if(order) {
                groupData = new TreeSet<>(
                        (p1, p2) -> {
                            for (int i = 0; i < 6; ++i) {
                                if (p1[i] != p2[i])
                                    return p1[i] - p2[i];
                            }
                            return 0;
                        });
            } else {
                groupData = new TreeSet<>(
                        (p2, p1) -> {
                            for (int i = 0; i < 6; ++i) {
                                if (p1[i] != p2[i])
                                    return p1[i] - p2[i];
                            }
                            return 0;
                        });
            }

            int tempCountry = 0;
            int tempCity = 0;
            int tempStatus = 0;
            int tempSex = 0;

            int curSum = 0;
            int newStartCity;

            if(startCity != 0)
                newStartCity = countryCityList[startCountry].indexOf(startCity);
            else
                newStartCity = 0;

            int lastCity;
            if(city != null)
                lastCity = newStartCity;
            else
                lastCity = countryCityList[tempCountry].size() -1;

            while(true) {

                if(tempCity != -1 && tempCity<=lastCity) {
                    if (birth != null) {
                        curSum += groupFilterBirth[tempCountry][tempCity][tempStatus][tempSex][birthYear - MIN_BIRTH_YEAR];
                    } else {
                        if (joined != null) {
                            curSum += groupFilterJoined[tempCountry][tempCity][tempStatus][tempSex][joinedYear - MIN_JOINED_YEAR];
                        } else {
                            curSum += groupFilter[tempCountry][tempCity][tempStatus][tempSex];
                        }
                    }
                }

                if(!groupSex && tempSex < lastSex) {
                    ++tempSex;
                    continue;
                }
                if(!groupStatus && tempStatus<lastStatus) {
                    if(!groupSex)
                        tempSex = startSex;

                    ++tempStatus;
                    continue;
                }

                if(!groupCity && tempCity<lastCity) {
                    ++tempCity;

                    if(!groupSex)
                        tempSex = startSex;
                    if(!groupStatus)
                        tempStatus = startStatus;
                    continue;
                }

                if(!groupCountry && tempCountry<lastCountry) {
                    ++tempCountry;
                    lastCity = countryCityList[tempCountry].size() -1;
                    if(startCity != 0)
                        newStartCity = countryCityList[startCountry].indexOf(startCity);

                    if(!groupSex)
                        tempSex = startSex;
                    if(!groupStatus)
                        tempStatus = startStatus;
                    if(!groupCity)
                        tempCity = newStartCity;
                    continue;
                }

                if(curSum >0) {
                    int[] valToInsert = new int[6];
                    valToInsert[0] = curSum;
                    valToInsert[1] = tempSex;
                    valToInsert[2] = tempStatus;
                    valToInsert[3] = tempCountry;
                    valToInsert[4] = AllLists.countryCityList[tempCountry].get(tempCity);
                    valToInsert[5] = 0;
                    groupData.add(valToInsert);
                    curSum = 0;
                }

                if(groupSex && tempSex < lastSex) {
                    ++tempSex;

                    if(!groupSex)
                        tempSex = startSex;
                    if(!groupStatus)
                        tempStatus = startStatus;
                    if(!groupCity)
                        tempCity = newStartCity;
                    if(!groupCountry) {
                        tempCountry = startCountry;
                        lastCity = countryCityList[tempCountry].size() -1;
                        if(startCity != 0)
                            newStartCity = countryCityList[startCountry].indexOf(startCity);
                    }

                    continue;
                }
                if(groupStatus && tempStatus<lastStatus) {
                    if(groupSex)
                        tempSex = startSex;

                    if(!groupSex)
                        tempSex = startSex;
                    if(!groupStatus)
                        tempStatus = startStatus;
                    if(!groupCity)
                        tempCity = startCity;
                    if(!groupCountry) {
                        tempCountry = startCountry;
                        lastCity = countryCityList[tempCountry].size() -1;
                        if(startCity != 0)
                            newStartCity = countryCityList[startCountry].indexOf(startCity);
                    }

                    ++tempStatus;
                    continue;
                }

                if(groupCity && tempCity<lastCity) {
                    ++tempCity;

                    while (groupFilter[tempCountry] == null)

                    if(groupSex)
                        tempSex = startSex;
                    if(groupStatus)
                        tempStatus = startStatus;

                    if(!groupSex)
                        tempSex = startSex;
                    if(!groupStatus)
                        tempStatus = startStatus;
                    if(!groupCity)
                        tempCity = newStartCity;
                    if(!groupCountry) {
                        tempCountry = startCountry;
                        lastCity = countryCityList[tempCountry].size() -1;
                        if(startCity != 0)
                            newStartCity = countryCityList[startCountry].indexOf(startCity);
                    }

                    continue;
                }

                if(groupCountry && tempCountry<lastCountry) {
                    ++tempCountry;
                    lastCity = countryCityList[tempCountry].size() -1;
                    if(startCity != 0)
                        newStartCity = countryCityList[startCountry].indexOf(startCity);

                    if(groupSex)
                        tempSex = startSex;
                    if(groupStatus)
                        tempStatus = startStatus;
                    if(groupCity)
                        tempCity = newStartCity;

                    if(!groupSex)
                        tempSex = startSex;
                    if(!groupStatus)
                        tempStatus = startStatus;
                    if(!groupCity)
                        tempCity = newStartCity;
                    if(!groupCountry) {
                        tempCountry = startCountry;
                        lastCity = countryCityList[tempCountry].size() -1;
                        if(startCity != 0)
                            newStartCity = countryCityList[startCountry].indexOf(startCity);
                    }

                    continue;
                }

                break;
            }
            buildAnswer(groupData, limit, groupSex, groupStatus, groupCountry, groupCity, groupInterests, buf);
            return true;
        }

        if (status != null) {
            //filters.add(AllLists.statusAccounts.get(status), 0, AllLists.statusAccounts.get(status).size()-1, false);
        }

        if (country != null) {
            filters.add(AllLists.countryAccounts.get(country), 0, AllLists.countryAccounts.get(country).size()-1, false);
        }

        if (city != null) {
            filters.add(AllLists.cityAccounts.get(city), 0, AllLists.cityAccounts.get(city).size()-1, false);
        }

        Integer interestId = null;
        if (interests != null) {
            interestId = AllLists.interests.get(interests);
            //filters.add(AllLists.interestAccounts.get(interestId), 0, AllLists.interestAccounts.get(interestId).size()-1, false);
        }

        int bStart = 0;
        int bEnd = 0;
        if(birth != null) {
            bStart = getTimestamp(birth);
            bEnd = getTimestamp(birth + 1) - 1;
/*
            int min=AllLists.birthYears[birth-1930];
            int max=AllLists.birthYears[birth+1-1930];

            int i = 1;
            while (max == 0 && birth + 1 - 1930 + i < AllLists.birthYears.length) {
                max = AllLists.birthYears[birth + 1 - 1930 + i];
                ++i;
            }

            filters.add(AllLists.birthSortedAccounts, min, max==0?AllLists.birthSortedAccounts.size()-1:max-1, false);
            */
        }

        int jStart = 0;
        int jEnd = 0;
        if(joined != null) {

            jStart = getTimestamp(joined);
            jEnd = getTimestamp(joined + 1) - 1;

            int min=AllLists.joinedYears[joined-1930];
            int max=AllLists.joinedYears[joined+1-1930];
            int i = 1;
            while (max == 0 && joined + 1 - 1930 + i < AllLists.joinedYears.length) {
                max = AllLists.joinedYears[joined + 1 - 1930 + i];
                ++i;
            }

            filters.add(AllLists.joinedSortedAccounts, min, max==0?AllLists.joinedSortedAccounts.size()-1:max-1, false);
        }
        Integer likeId = null;
        Set<Integer> searchLikesSet = null;
        if (likes != null) {
            likeId = Integer.parseInt(likes);
            if(likeId > AllLists.likesTO.size()) {
                return true;
            }
            List<Integer> accToIds = AllLists.likesTO.get(likeId);
            searchLikesSet = new HashSet<>(accToIds);
            filters.add(accToIds, 0, accToIds.size()-1, false);
        }

        HashMap<Integer, Group> formedGroups = new HashMap<>(50);
        while (filters.hasNext()) {
            Integer possibleId = filters.next();
            if (possibleId == null || possibleId < 1)
                continue;

            Account possible = AllLists.allAccounts[possibleId];

            if (city != null) {
                if (possible.city == 0 || possible.city != city)
                    continue;
            }

            if (country != null) {
                if (possible.country == 0 || possible.country != country)
                    continue;
            }

            if (sex != null) {
                if (possible.sex != sex)
                    continue;
            }

            if (status != null) {
                if (possible.status != status)
                    continue;
            }

            if (birth != null) {
                if (possible.birth == 0 || possible.birth < bStart || possible.birth > bEnd)
                    continue;
            }

            if (joined != null) {
                if (possible.joined < jStart || possible.joined > jEnd)
                    continue;
            }

            if (interests != null) {
                if (possible.interests == null || !possible.interests.contains(interestId))
                    continue;
            }

            if (likes != null) {
                if (likeId > AllLists.likesTO.size()) {
                    return true;
                }
                if(!searchLikesSet.contains(possible.id)) {
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

    private void buildAnswer(TreeSet<int[]> groups, int limit,
                             boolean groupSex, boolean groupStatus, boolean groupCountry, boolean groupCity, boolean groupInterests, StringBuilder buf) {
        int pointer = 0;
        boolean isFirst = true;
        while (!groups.isEmpty() && pointer<limit) {
            //Group group = sorted[pointer];
            int[] group = groups.pollFirst();
            if(isFirst) {
                buf.append("{\"count\":");
                isFirst = false;
            }else
                buf.append(",{\"count\":");

/*
                int[] valToInsert = new int[6];
                valToInsert[0] = curSum;
                valToInsert[1] = tempSex;
                valToInsert[2] = tempStatus;
                valToInsert[3] = tempCountry;
                valToInsert[4] = AllLists.countryCityList[tempCountry].get(tempCity);
                valToInsert[5] = 0;
*/

            buf.append(group[0]);

            if(groupCountry && group[3] != 0) {
                buf.append(",\"country\":\"");
                buf.append(AllLists.countriesList.get(group[3]));
                buf.append("\"");
            }
            if(groupCity && group[4] != 0) {
                buf.append(",\"city\":\"");
                buf.append(AllLists.citiesList.get(group[4]));
                buf.append("\"");
            }
            if(groupStatus) {
                buf.append(",\"status\":\"");
                buf.append(Utils.getStatusText((byte) (group[2] +1)));
                buf.append("\"");
            }
            if(groupSex ) {
                String sexStr = (group[1]==1?"m":"f");
                buf.append(",\"sex\":\"");
                buf.append(sexStr);
                buf.append("\"");
            }
/*
            if(groupInterests && group.interest != 0) {
                buf.append(",\"interests\":\"");
                buf.append(AllLists.interestsById.get(group.interest));
                buf.append("\"");
            }
*/
            buf.append("}");
            ++pointer;
        }
    }



    private void buildAnswer(Group[] sorted, int limit,
                                      boolean groupSex, boolean groupStatus, boolean groupCountry, boolean groupCity, boolean groupInterests, StringBuilder buf) {
        int pointer = 0;
        boolean isFirst = true;
        while (pointer<sorted.length && pointer<limit) {
            Group group = sorted[pointer];
            if(isFirst) {
                buf.append("{\"count\":");
                isFirst = false;
            }else
                buf.append(",{\"count\":");

            buf.append(sorted[pointer].count);

            if(groupCountry && group.country != 0) {
                buf.append(",\"country\":\"");
                buf.append(AllLists.countriesList.get(group.country));
                buf.append("\"");
            }
            if(groupCity && group.city != 0) {
                buf.append(",\"city\":\"");
                buf.append(AllLists.citiesList.get(group.city));
                buf.append("\"");
            }
            if(groupStatus && group.status != 0) {
                buf.append(",\"status\":\"");
                buf.append(Utils.getStatusText(group.status));
                buf.append("\"");
            }
            if(groupSex ) {
                String sexStr = group.sex?"m":"f";
                buf.append(",\"sex\":\"");
                buf.append(sexStr);
                buf.append("\"");
            }
            if(groupInterests && group.interest != 0) {
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

        if(formedGroups != null) {
            if (order) {
                Arrays.sort(formedGroups, (Group o1, Group o2) -> {
                    if (o1.count != o2.count)
                        return o1.count - o2.count;
                    if (o1.country != o2.country)
                        return AllLists.countriesList.get(o1.country).compareTo(AllLists.countriesList.get(o2.country));
                    if (o1.city != o2.city)
                        return AllLists.citiesList.get(o1.city).compareTo(AllLists.citiesList.get(o2.city));
                    if (o1.status != o2.status)
                        return Utils.getStatusText(o1.status).compareTo(Utils.getStatusText(o2.status));
                    if (o1.sex != o2.sex)
                        return (o1.sex ? "m" : "f").compareTo(o2.sex ? "m" : "f");

                    return AllLists.interestsById.get(o1.interest).compareTo(AllLists.interestsById.get(o2.interest));
                });
            } else {
                Arrays.sort(formedGroups, (Group o2, Group o1) -> {
                    if (o1.count != o2.count)
                        return o1.count - o2.count;
                    if (o1.country != o2.country)
                        return AllLists.countriesList.get(o1.country).compareTo(AllLists.countriesList.get(o2.country));
                    if (o1.city != o2.city)
                        return AllLists.citiesList.get(o1.city).compareTo(AllLists.citiesList.get(o2.city));
                    if (o1.status != o2.status)
                        return Utils.getStatusText(o1.status).compareTo(Utils.getStatusText(o2.status));
                    if (o1.sex != o2.sex)
                        return (o1.sex ? "m" : "f").compareTo(o2.sex ? "m" : "f");

                    return AllLists.interestsById.get(o1.interest).compareTo(AllLists.interestsById.get(o2.interest));
                });
            }
        }
        return formedGroups;
    }

    private void addToGroup(HashMap<Integer, Group> groups, Account account, boolean groupSex, boolean groupStatus,
                            boolean groupCountry, boolean groupCity, boolean groupInterests) {

        if (groupInterests && account.interests == null)
            return;

        int hash = 0;
        if(groupSex && account.sex) {
            hash = 1;
        }
        if(groupStatus) {
            hash |= account.status << 1;
        }
        int moveCityIndex = 3;
        if(groupCountry) {
            hash |= (int) account.country << 3;
            moveCityIndex += 10;
        }

        int moveInterestsIndex = moveCityIndex;
        if (groupCity) {
            hash |= (int) account.city << moveCityIndex;
            moveInterestsIndex += 10;
        }

        if (!groupInterests) {
            if (groups.get(hash) != null) {
                ++groups.get(hash).count;
            } else {
                Group g = new Group();
                g.count = 1;
                g.sex = groupSex && account.sex;
                g.status = groupStatus?account.status:0;
                g.country = groupCountry?account.country:0;
                g.city = groupCity?account.city:0;
                groups.put(hash, g);
            }
        } else {
            int[] accInterests = account.interestsArray;
            final int originalHash = hash;
            for (Integer interest : accInterests) {
                hash = originalHash | (int) interest << moveInterestsIndex;

                if (groups.get(hash) != null) {
                    ++groups.get(hash).count;
                } else {
                    Group g = new Group();
                    g.count = 1;
                    g.sex = groupSex && account.sex;
                    g.status = groupStatus?account.status:0;
                    g.country = groupCountry?account.country:0;
                    g.city = groupCity?account.city:0;
                    g.interest = interest;
                    groups.put(hash, g);
                }
            }
        }
    }

    private int getTimestamp(int year) {
        if(AllLists.yearToTs[year-1930] != 0)
            return AllLists.yearToTs[year-1930];
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;
        try {
            date = dateFormat.parse("01/01/" + year);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time = date.getTime();
        AllLists.yearToTs[year-1930] = (int) (time/1000);
        return (int) (time/1000);
    }
}
