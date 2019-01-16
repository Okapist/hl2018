package com.pk.dao;

import com.pk.Runner;
import com.pk.collectors.AccauntCollector;
import com.pk.model.Account;
import com.pk.model.AllLists;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.pk.model.AllLists.*;

public class NewAccFilter {

    public boolean filter(Boolean sex,
                                char[] email, Boolean emailDomain, Boolean emailLt,
                                Byte status, Boolean statusEq,
                                int[] fname, Boolean fnameExists,
                                int sname, Boolean snameExists, Boolean snameEq,
                                String phoneCode, Boolean phoneExists,
                                Short country, Boolean countryExists,
                                List<Short> city, Boolean cityExists,
                                Integer birth, Boolean birthLt, Boolean birthYear,
                                List<String> interests, Boolean interestsAll,
                                List<String> likes,
                                Boolean premiumNow, Boolean premiumNewer,
                                int limit, StringBuilder buf, char[] snameStartWith) {

        if(limit <1)
            return false;

        Set<Short> citySet = null;
        if(city != null)
            citySet = new HashSet<>(city);

        int[] interestsInt = null;
        if(interests != null) {
            interestsInt = new int[interests.size()];
            for (int i = 0; i < interestsInt.length; i++) {
                String str = interests.get(i);
                if (str != null)
                    interestsInt[i] = AllLists.interests.get(str);
            }
        }

        //BaseFilter filter = new BaseFilter();

        boolean ansSex = false;
        boolean ansStatus = false;
        boolean ansFname = false;
        boolean ansSname = false;
        boolean ansPhone = false;
        boolean ansCountry = false;
        boolean ansCity = false;
        boolean ansBirth = false;
        boolean ansPremium = false;

        int filterSize = Integer.MAX_VALUE;
        List<Integer> filterList = null;
        int[] filterArr = null;
        int filterStartIndex = 0;
        int filterEndIndex = 0;

        //email filter
        Integer emailDomainIndex = null;
        if (email != null && emailDomain != null && emailDomain) {
            emailDomainIndex = Utils.findDomainIndex(email);
            if (emailDomainIndex == null) {
                return true;
            }
            if(AllLists.domainAccounts.get(emailDomainIndex).size() < filterSize) {
                filterStartIndex = 0;
                filterEndIndex = AllLists.domainAccounts.get(emailDomainIndex).size();
                filterArr = null;
                filterList = AllLists.domainAccounts.get(emailDomainIndex);
                filterSize = AllLists.domainAccounts.get(emailDomainIndex).size();
            }
        }
        /*
        if(email != null && emailLt!= null) {
            if(!emailLt) {
                int tempStart = 0;
                int tempEnd = emailAscAccounts[email[0] - 'a'].length;
                if(tempEnd - tempStart < filterSize) {
                    filterStartIndex = tempStart;
                    filterEndIndex = tempEnd;
                    filterList = null;
                    filterArr = emailAscAccounts[email[0] - 'a'];
                    filterSize = tempEnd - tempStart;
                }
            } else {
                int tempStart = 0;
                int tempEnd = emailDescAccounts[email[0] - 'a'].length;
                if(tempEnd - tempStart < filterSize) {
                    filterStartIndex = tempStart;
                    filterEndIndex = tempEnd;
                    filterList = null;
                    filterArr = emailDescAccounts[email[0] - 'a'];
                    filterSize = tempEnd - tempStart;
                }
            }
        }
        */
/*
        if (status != null) {
            ansStatus = true;
            if(statusEq) {
                if(statusAccounts[status-1].length < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = statusAccounts[status-1].length;
                    filterList = null;
                    filterArr = statusAccounts[status-1];
                    filterSize = statusAccounts[status-1].length;
                }
            }
        }
*/

        if (status != null) {
            ansStatus = true;
        }

        if (fname != null) {
            ansFname = true;
            if (fnameExists != null) {
                if(!fnameExists) {
                    int tempStart = 0;
                    int tempEnd = AllLists.fnameAccounts[0].length;
                    if(tempEnd - tempStart < filterSize) {
                        filterStartIndex = 0;
                        filterEndIndex = AllLists.fnameAccounts[0].length;
                        filterList = null;
                        filterArr = fnameAccounts[0];
                        filterSize = tempEnd - tempStart;
                    }
                } else {
                    int i = 0;
                }
            } else {
                if(fname.length == 1) {

                    if(fname[0] == -1) {
                        return true;
                    }

                    int tempStart = 0;
                    int tempEnd = AllLists.fnameAccounts[fname[0]].length;
                    if(tempEnd - tempStart < filterSize) {
                        filterStartIndex = 0;
                        filterEndIndex = AllLists.fnameAccounts[fname[0]].length;
                        filterList = null;
                        filterArr = fnameAccounts[fname[0]];
                        filterSize = tempEnd - tempStart;
                    }
                } else {
                    int i =0;
                }
            }
        }

        if (snameExists != null) {
            if(!snameExists) {
                int tempStart = 0;
                int tempEnd = AllLists.snameAccounts[0].length;
                if(tempEnd - tempStart < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = AllLists.snameAccounts[0].length;
                    filterList = null;
                    filterArr = snameAccounts[0];
                    filterSize = tempEnd - tempStart;
                }
            } else {
                int i =0;
            }
        }

        if (sname > 0) {
            ansSname = true;
            if (snameEq != null && snameEq) {
                int tempStart = 0;
                int tempEnd = AllLists.snameAccounts[sname].length;
                if(tempEnd - tempStart < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = AllLists.snameAccounts[sname].length;
                    filterList = null;
                    filterArr = AllLists.snameAccounts[sname];
                    filterSize = tempEnd - tempStart;
                }
            } else {
                if(snameEq != null && !snameEq) {
                    int i =0;
                }
            }
        } else {
            if(sname == -1)
                return true;
        }

        if (birth != null) {
            ansBirth = true;
            if(birthYear != null) {

                if(birth > MAX_BIRTH_YEAR || birth < MIN_BIRTH_YEAR)
                    return true;

                int tempStart = 0;
                if(AllLists.birthYearsAccount[birth - MIN_BIRTH_YEAR] == null)
                    return true;

                int tempEnd = AllLists.birthYearsAccount[birth - MIN_BIRTH_YEAR].length;

                if (tempEnd - tempStart < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = AllLists.birthYearsAccount[birth - MIN_BIRTH_YEAR].length;
                    filterList = null;
                    filterArr = AllLists.birthYearsAccount[birth - MIN_BIRTH_YEAR];
                    filterSize = tempEnd - tempStart;
                }
            } else {
                /*if(birthLt != null) {
                    if(birthLt) {
                        int tempStart = 0;
                        int tempEnd = Utils.searchBirth(birth-1);
                        if (tempEnd - tempStart < filterSize) {
                            filterStartIndex = 0;
                            filterEndIndex = tempEnd;
                            filterList = null;
                            filterArr = AllLists.birthAccount;
                            filterSize = tempEnd - tempStart;
                        }
                    } else {
                        int tempStart = AllLists.birthAccount.length;
                        int tempEnd =  Utils.searchBirth(birth-1) - 1;
                        if (tempEnd - tempStart < filterSize) {
                            filterStartIndex = 0;
                            filterEndIndex = tempEnd;
                            filterList = null;
                            filterArr = AllLists.birthAccount;
                            filterSize = tempEnd - tempStart;
                        }
                    }
                }*/
            }
        }

        char[] phoneCodeArr = null;

        if (phoneCode != null) {
            ansPhone = true;
            if(phoneExists == null){
                int tempStart = 0;
                if(AllLists.phoneCodeAccounts.get(phoneCode) == null)
                    return true;

                phoneCodeArr = phoneCode.toCharArray();
                int tempEnd =  AllLists.phoneCodeAccounts.get(phoneCode).size();
                if (tempEnd - tempStart < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = tempEnd;
                    filterList = AllLists.phoneCodeAccounts.get(phoneCode);
                    filterArr = null;
                    filterSize = tempEnd - tempStart;
                }
            }
        }
        if (phoneExists != null) {
            ansPhone = true;
            if (!phoneExists) {
                int tempStart = 0;
                int tempEnd =  AllLists.phoneCodeAccounts.get(null).size();
                if (tempEnd - tempStart < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = tempEnd;
                    filterList = AllLists.phoneCodeAccounts.get(null);
                    filterArr = null;
                    filterSize = tempEnd - tempStart;
                }
            }
        }

        if (country != null) {
            ansCountry = true;
            int tempStart = 0;
            if(AllLists.countryAccounts.get(country) == null)
                return true;

            int tempEnd =  AllLists.countryAccounts.get(country).size();
            if (tempEnd - tempStart < filterSize) {
                filterStartIndex = 0;
                filterEndIndex = tempEnd;
                filterList = AllLists.countryAccounts.get(country);
                filterArr = null;
                filterSize = tempEnd - tempStart;
            }
        }
        if (countryExists != null) {
            ansCountry = true;
            if(countryExists == false) {
                int tempStart = 0;
                int tempEnd = AllLists.countryAccounts.get((short) 0).size();
                if (tempEnd - tempStart < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = tempEnd;
                    filterList = AllLists.countryAccounts.get((short) 0);
                    filterArr = null;
                    filterSize = tempEnd - tempStart;
                }
            }
        }

        if (city != null) {
            ansCity = true;
            if (cityExists != null) {
                if (!cityExists) {
                    int tempStart = 0;
                    int tempEnd =  AllLists.cityAccounts.get((short)0).size();
                    if (tempEnd - tempStart < filterSize) {
                        filterStartIndex = 0;
                        filterEndIndex = tempEnd;
                        filterList = AllLists.cityAccounts.get((short)0);
                        filterArr = null;
                        filterSize = tempEnd - tempStart;
                    }
                }
            } else {
                if (city.size() == 1) {
                    int tempStart = 0;
                    int tempEnd =  AllLists.cityAccounts.get(city.get(0)).size();
                    if (tempEnd - tempStart < filterSize) {
                        filterStartIndex = 0;
                        filterEndIndex = tempEnd;
                        filterList = AllLists.cityAccounts.get(city.get(0));
                        filterArr = null;
                        filterSize = tempEnd - tempStart;
                    }
                }
            }
        }
/*
        if (interests != null) {
            if (interestsAll != null) {
                for (int interest : interestsInt) {
                    int tempStart = 0;
                    int tempEnd = AllLists.interestAccounts.get(interest).size();
                    if (tempEnd - tempStart < filterSize) {
                        filterStartIndex = 0;
                        filterEndIndex = tempEnd;
                        filterList = AllLists.interestAccounts.get(interest);
                        filterArr = null;
                        filterSize = tempEnd - tempStart;
                    }
                }
            }
        }
*/
Set<Integer> likesArr = null;
if (likes != null) {
    likesArr = new HashSet<>();
            for (String like : likes) {
                int likeId = Integer.parseInt(like);
                if(likeId >= AllLists.likesTO.size())
                    return true;

                likesArr.add(likeId);
                int tempStart = 0;
                int tempEnd = AllLists.likesTO.get(likeId).size();
                if (tempEnd - tempStart < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = tempEnd;
                    filterList = AllLists.likesTO.get(likeId);
                    filterArr = null;
                    filterSize = tempEnd - tempStart;
                }
            }
        }

        if (premiumNow != null || premiumNewer != null) {
            ansPremium = true;
            if (premiumNow != null && premiumNow) {

                int tempStart = 0;
                int tempEnd = AllLists.premiumNowAccounts.size();
                if (tempEnd - tempStart < filterSize) {
                    filterStartIndex = 0;
                    filterEndIndex = tempEnd;
                    filterList = AllLists.premiumNowAccounts;
                    filterArr = null;
                    filterSize = tempEnd - tempStart;
                }
            }
            if (premiumNewer != null) {
                if (!premiumNewer) {
                    int tempStart = 0;
                    int tempEnd = AllLists.premiumEverAccounts.size();
                    if (tempEnd - tempStart < filterSize) {
                        filterStartIndex = 0;
                        filterEndIndex = tempEnd;
                        filterList = AllLists.premiumEverAccounts;
                        filterArr = null;
                        filterSize = tempEnd - tempStart;
                    }
                } else {
                    int tempStart = 0;
                    int tempEnd = AllLists.premiumNeverAccounts.size();
                    if (tempEnd - tempStart < filterSize) {
                        filterStartIndex = 0;
                        filterEndIndex = tempEnd;
                        filterList = AllLists.premiumNeverAccounts;
                        filterArr = null;
                        filterSize = tempEnd - tempStart;
                    }
                }
            }
        }

        if(snameExists != null) {
            ansSname = true;
        }
        if(snameEq != null) {
            ansSname = true;
        }
        if(fnameExists != null) {
            ansFname = true;
        }

        if(sex !=null)
            ansSex = true;

        boolean isFirst = true;
        int totalAdded = 0;

        if(filterArr == null && filterList == null) {
            filterEndIndex = allAccounts.length;
            filterStartIndex = 0;
        }

        int start = filterStartIndex;

        if(filterEndIndex == allAccounts.length) {
            if (email != null && emailLt != null) {
                if (emailLt) {
                    filterEndIndex = emailHightBorder[email[0] - 'a'][email.length>1?email[1] - 'a':'a'-'a'][email.length>2?email[2] - 'a':'a'-'a'] + 1;
                } else {
                    filterEndIndex = emailLowBorder[email[0] - 'a'][email.length>1?email[1] - 'a':'z'-'a'][email.length>2?email[2] - 'a':'z'-'a'] + 1;
                }
            }
        }

        int index = filterEndIndex-1;
        while (index >= start) {

            Integer possibleId;
            if(filterArr != null)
                possibleId = filterArr[index];
            else {
                if (filterList != null) {
                    possibleId = filterList.get(index);
                } else {
                    while (allAccounts[index] == null && index > 0)
                        --index;

                    if (allAccounts[index] == null)
                        break;

                    possibleId = allAccounts[index].id;
                }
            }
            --index;

            if(possibleId==null || possibleId < 1)
                continue;
            boolean isAdd = true;

            Account possible = AllLists.allAccounts[possibleId];

            if (isAdd && sex != null) {
                if (possible.sex != sex) {
                    continue;
                }
            }

            if(isAdd && email != null && emailLt != null) {
                if(emailLt) {
                    if (Utils.compareCharArr(allEmailList.get(possible.email).toCharArray(), email) >= 0)
                        continue;
                } else {
                    if (Utils.compareCharArr(allEmailList.get(possible.email).toCharArray(), email) <= 0)
                        continue;
                }
            }

            if(isAdd && email != null && emailDomain != null) {
                if(possible.emailDomain == 0 || possible.emailDomain != emailDomainIndex)
                    continue;
            }

            if(isAdd && status != null) {
                if(statusEq) {
                    if(possible.status != status)
                        continue;
                } else {
                    if(possible.status == status)
                        continue;
                }
            }

            if(isAdd && fname != null && fnameExists == null) {
                boolean fnResult = false;
                for (int fn : fname) {
                    if (possible.fname == fn) {
                        fnResult = true;
                        break;
                    }
                }
                if (!fnResult)
                    continue;
            }

            if(fnameExists != null) {
                if(fnameExists) {
                    if(possible.fname == 0)
                        continue;
                } else {
                    if(possible.fname != 0)
                        continue;
                }
            }

            if(snameExists != null) {
                if(snameExists) {
                    if(possible.sname == 0)
                        continue;
                } else {
                    if(possible.sname != 0)
                        continue;
                }
            }

            if(snameEq !=null) {
                if (snameEq) {
                    if (possible.sname == 0 || possible.sname != sname)
                        continue;
                } else {
                    if (possible.sname == 0 || !Utils.startWith(snames[possible.sname], snameStartWith))
                        continue;
                }
            }


            if (isAdd && phoneCode != null) {
                if(phoneExists != null) {
                    if (phoneExists) {
                        if (possible.phone == null) {
                            continue;
                        }
                    } else {
                        if (possible.phone != null) {
                            continue;
                        }
                    }
                } else {
                    if (possible.phone == null || AllLists.phoneCodeAccounts.get(phoneCode)==null
                            || Utils.compareCharArr(phoneCodeArr, possible.phoneCode) != 0)
                        continue;
                }
            }

            if(isAdd && country != null) {
                if (possible.country == 0 || possible.country != country)
                    continue;
            }
            if(isAdd && countryExists != null) {
                if(countryExists) {
                    if(possible.country == 0)
                        continue;
                } else {
                    if(possible.country > 0)
                        continue;
                }
            }

            if(isAdd && city != null) {
                if(cityExists != null) {
                    if(cityExists) {
                        if(possible.city == 0)
                            continue;
                    } else {
                        if(possible.city > 0)
                            continue;
                    }
                } else {
                    if(possible.city ==0 || !citySet.contains(AllLists.shortCache[possible.city]))
                        continue;
                }
            }

            if(isAdd && premiumNow != null && premiumNow){
                if(possible.premiumEnd == 0 || possible.premiumStart == 0 || possible.premiumEnd < Runner.curDate ||
                        possible.premiumStart > Runner.curDate)
                    continue;
            }

            if(isAdd && premiumNewer != null){
                if(premiumNewer == true) {
                    if (possible.premiumEnd != 0 || possible.premiumStart != 0)
                        continue;
                } else {
                    if (possible.premiumEnd == 0 || possible.premiumStart == 0)
                        continue;
                }
            }

            if (isAdd && birth != null) {
                if (birthLt != null) {
                    if (birthLt) {
                        if (possible.birth == 0 || possible.birth > birth)
                            continue;
                    } else {
                        if (possible.birth == 0 || possible.birth < birth)
                            continue;
                    }
                } else {
                    if (birthYear) {
                        int bStart = getTimestamp(birth);
                        int bEnd = getTimestamp(birth + 1) - 1;

                        if (possible.birth == 0 || possible.birth < bStart || possible.birth > bEnd)
                            continue;
                    }
                }
            }

            if (isAdd && interests != null) {
                if(interestsAll != null && interestsAll) {
                    boolean totalContinue = false;
                    for (int interest : interestsInt) {
                        int intId = interest;
                        if (possible.interests == null || !possible.interests.contains(intId)) {
                            totalContinue = true;
                            break;
                        }
                    }
                    if(totalContinue)
                        continue;
                } else {
                    boolean founded = false;
                    for (Integer interest : interestsInt) {
                        int intId = interest;
                        if (possible.interests != null && possible.interests.contains(intId)) {
                            founded = true;
                            break;
                        }
                    }
                    if (!founded)
                        continue;
                }
            }

            if(isAdd && likes != null && likesArr != null) {

                if (AllLists.likesAccounts.get(possible.id) == null) {
                    isAdd = false;
                }
                int[] accLikes = AllLists.likesAccounts.get(possible.id);
                if(accLikes == null)
                    continue;

                for (Integer likeInt : likesArr) {
                    boolean founded = false;
                    for (int i = 0; i < accLikes.length; i++) {
                        int likeId = accLikes[i];
                        if (likeInt == likeId) {
                            founded = true;
                            break;
                        }
                    }
                    if (!founded) {
                        isAdd = false;
                        break;
                    }
                }
            }

            if (isAdd) {
                buildResult(isFirst, possible, ansSex, ansStatus, ansFname, ansSname, ansPhone, ansCountry, ansCity, ansBirth, ansPremium, buf);
                isFirst = false;
                ++totalAdded;

                if (totalAdded >= limit)
                    break;
            }
        }

        return true;
    }

    private void buildResult(boolean isFirst, Account account, boolean ansSex, boolean ansStatus, boolean ansFname,
                             boolean ansSname, boolean ansPhone, boolean ansCountry, boolean ansCity, boolean ansBirth,
                             boolean ansPremium, StringBuilder buf) {

        if (isFirst) {
            buf.append("{\"id\":");
        } else {
            buf.append(",{\"id\":");
        }
        buf.append(account.id);

            buf.append(",\"email\":\"");
            buf.append(allEmailList.get(account.email)).append("@").append(AllLists.domainList.get(account.emailDomain));
            buf.append("\"");

        if (ansSex) {
            buf.append(",\"sex\":\"");
            buf.append(account.sex ? "m" : "f");
            buf.append("\"");
        }
        if (ansStatus) {
            buf.append(",\"status\":\"");
            buf.append(account.getStatusText());
            buf.append("\"");
        }
        if (ansFname && account.fname > 0) {
            char[] fnameRes = AllLists.fnames[account.fname];
            if (fnameRes != null) {
                buf.append(",\"fname\":\"");
                buf.append(fnameRes);
                buf.append("\"");
            }
        }
        if (ansSname&& account.sname > 0) {
            char[] snameRes = AllLists.snames[account.sname];
            if (snameRes != null) {
                buf.append(",\"sname\":\"");
                buf.append(snameRes);
                buf.append("\"");
            }
        }

        if (ansPhone) {
            char[] phoneRes = account.phone;
            if (phoneRes != null && !"".equals(phoneRes)) {
                buf.append(",\"phone\":\"");
                buf.append(phoneRes);
                buf.append("\"");
            }
        }
        if (ansCountry) {
            String countryRes = AllLists.countriesList.get(account.country);
            if (countryRes != null && !"".equals(countryRes)) {
                buf.append(",\"country\":\"");
                buf.append(countryRes);
                buf.append("\"");
            }
        }
        if (ansCity) {
            String cityRes = AllLists.citiesList.get(account.city);
            if (cityRes != null && !"".equals(cityRes)) {
                buf.append(",\"city\":\"");
                buf.append(cityRes);
                buf.append("\"");
            }
        }
        if (ansBirth) {
            buf.append(",\"birth\": ");
            buf.append(account.birth);
        }
        if (ansPremium && account.premiumStart != 0 && account.premiumEnd != 0) {
            int premStart = account.premiumStart;
            int premEnd = account.premiumEnd;
            if (premStart > 0 && premEnd > 0) {
                buf.append(",\"premium\":{\"start\":");
                buf.append(premStart);
                buf.append(",\"finish\":");
                buf.append(premEnd);
                buf.append("}");
            }
        }
        buf.append("}");
    }

    private int getTimestamp(int year) {

        if(year==1970)
            return 0;

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
