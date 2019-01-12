package com.pk.dao;

import com.pk.Runner;
import com.pk.collectors.AccauntCollector;
import com.pk.model.Account;
import com.pk.model.AllLists;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewAccFilter {

    public boolean filter(Boolean sex,
                                char[] email, Boolean emailDomain, Boolean emailLt,
                                Byte status, Boolean statusEq,
                                char[][] fname, Boolean fnameExists,
                                char[] sname, Boolean snameExists, Boolean snameEq,
                                String phoneCode, Boolean phoneExists,
                                Short country, Boolean countryExists,
                                List<Short> city, Boolean cityExists,
                                Integer birth, Boolean birthLt, Boolean birthYear,
                                List<String> interests, Boolean interestsAll,
                                List<String> likes,
                                Boolean premiumNow, Boolean premiumNewer,
                                int limit, StringBuilder buf) {

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

        BaseFilter filter = new BaseFilter();

        boolean ansSex = false;
        boolean ansStatus = false;
        boolean ansFname = false;
        boolean ansSname = false;
        boolean ansPhone = false;
        boolean ansCountry = false;
        boolean ansCity = false;
        boolean ansBirth = false;
        boolean ansPremium = false;

        if (email != null) {
            if (emailDomain != null && emailDomain) {
                Integer emailDomainIndex = Utils.findDomainIndex(email);
                if(emailDomainIndex == null) {
                    return true;
                }
                filter.add(AllLists.domainAccounts.get(emailDomainIndex), 0, AllLists.domainAccounts.get(emailDomainIndex).size()-1, false);
            }
        }
        if (status != null) {
            ansStatus = true;
            if(statusEq)
                filter.add(AllLists.statusAccounts.get(status), 0, AllLists.statusAccounts.get(status).size()-1, false);
        }

        if (fname != null) {
            ansFname = true;
            if (fnameExists != null) {
                if(!fnameExists)
                    filter.add(AllLists.fnameAccounts.get(null), 0, AllLists.fnameAccounts.get(null).size()-1, false);
            } else {
                if(fname.length == 1) {
                    filter.add(AllLists.fnameAccounts.get(new String(fname[0])), 0, AllLists.fnameAccounts.get(new String(fname[0])).size()-1, false);
                } else {
                    List<List<Integer>> toAdd = new ArrayList<>(fname.length);
                    List<Integer> starts = new ArrayList<>(fname.length);
                    List<Integer> ends = new ArrayList<>(fname.length);
                    for(char[] fn : fname) {
                        List<Integer> tempList = AllLists.fnameAccounts.get(new String(fn));
                        if(tempList != null && tempList.size() > 0) {
                            int tempStart = 0;
                            int tempEnd = AllLists.fnameAccounts.get(new String(fn)).size() - 1;

                            toAdd.add(tempList);
                            starts.add(tempStart);
                            ends.add(tempEnd);
                        }
                    }
                    filter.addList(toAdd, starts, ends, false);
                }
            }
        }

        if (sname != null) {
            ansSname = true;
            if (snameExists != null) {
                if (!snameExists)
                    filter.add(AllLists.snameAccounts.get(null), 0, AllLists.snameAccounts.get(null).size()-1, false);
            } else {
                if (snameEq != null && snameEq) {
                    filter.add(AllLists.snameAccounts.get(sname), 0, AllLists.snameAccounts.get(sname).size()-1, false);
                }
            }
        }

        if (phoneCode != null) {
            ansPhone = true;
            if(phoneExists == null)
                filter.add(AllLists.phoneCodeAccounts.get(phoneCode), 0, AllLists.phoneCodeAccounts.get(phoneCode).size()-1, false);
        }
        if (phoneExists != null) {
            ansPhone = true;
            if (!phoneExists)
                filter.add(AllLists.phoneCodeAccounts.get(null), 0, AllLists.phoneCodeAccounts.get(null).size()-1, false);
        }

        if (country != null) {
            ansCountry = true;
                filter.add(AllLists.countryAccounts.get(country), 0, AllLists.countryAccounts.get(country).size()-1, false);
        }
        if (countryExists != null) {
            ansCountry = true;
            if (!countryExists)
                filter.add(AllLists.countryAccounts.get((short)0), 0, AllLists.countryAccounts.get((short)0).size()-1, false);
        }

        if (city != null) {
            ansCity = true;
            if (cityExists != null) {
                if (!cityExists)
                    filter.add(AllLists.cityAccounts.get((short)0), 0, AllLists.cityAccounts.get((short)0).size()-1, false);
            } else {
                if (city.size() == 1) {
                    filter.add(AllLists.cityAccounts.get(city.get(0)), 0, AllLists.cityAccounts.get(city.get(0)).size()-1, false);
                }
            }
        }

        if(email != null && emailLt!= null) {
            if(emailLt) {
                if(email[0]!='z')
                    filter.add(AllLists.emailSortedAccounts, 0, AllLists.emailFirst[email[0] - 'a' + 1] -1, true);
            } else {
                filter.add(AllLists.emailSortedAccounts, AllLists.emailFirst[email[0] - 'a'], AllLists.emailSortedAccounts.size()-1,true);
            }
        }

        if (interests != null) {
            if (interestsAll != null && interestsAll) {
                for (int interest : interestsInt) {
                    filter.add(AllLists.interestAccounts.get(interest),0,AllLists.interestAccounts.get(interest).size() - 1, false);
                }
            }
        }

        if (likes != null) {
            for (String like : likes) {
                int likeId = Integer.parseInt(like);
                if(likeId >= AllLists.likesTO.size())
                    return true;
                List<Integer> accToIds = AllLists.likesTO.get(Integer.parseInt(like));
                filter.add(accToIds, 0, accToIds.size()-1, false);
            }
        }

        if(snameExists != null) {
            ansSname = true;
        }
        if(fnameExists != null) {
            ansFname = true;
        }
        
        if (birth != null) {
            ansBirth = true;
            if(birthYear != null) {
                int min=AllLists.birthYears[birth-1930];
                int max=AllLists.birthYears[birth+1-1930];
                filter.add(AllLists.birthSortedAccounts, min, max-1, true);
            } else {
                if(birthLt != null) {
                    if(birthLt) {
                        int hight = Utils.searchBirth(birth-1);
                        filter.add(AllLists.birthSortedAccounts, 0, hight, true);
                    } else {
                        int low = Utils.searchBirth(birth+1);
                        filter.add(AllLists.birthSortedAccounts, low, AllLists.birthSortedAccounts.size()-1, true);
                    }
                }
            }
        }

        if(sex !=null)
            ansSex = true;

        if (premiumNow != null || premiumNewer != null) {
            ansPremium = true;
            if(premiumNow != null && premiumNow) {
                filter.add(AllLists.premiumNowAccounts, 0, AllLists.premiumNowAccounts.size()-1, false);
            }
            if(premiumNewer != null) {
                if(!premiumNewer)
                    filter.add(AllLists.premiumEverAccounts, 0, AllLists.premiumEverAccounts.size()-1, false);
                else
                    filter.add(AllLists.premiumNeverAccounts, 0, AllLists.premiumNeverAccounts.size()-1, false);
            }
        }

        boolean isFirst = true;
        int totalAdded = 0;
        AccauntCollector accauntCollector = new AccauntCollector(limit);
        while (filter.hasNext()) {

            Integer possibleId = filter.next();
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
                    if (possible.email==null || Utils.compareCharArr(possible.email, email) >= 0)
                        continue;
                } else {
                    if (possible.email==null || Utils.compareCharArr(possible.email, email) <= 0)
                        continue;
                }
            }

            if(isAdd && email != null && emailDomain != null) {
                if(possible.emailDomain == 0 || possible.emailDomain != Utils.findDomainIndex(email))
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

            if(isAdd && fname != null) {
                if (possible.fname == null) {
                    continue;
                } else {
                    boolean fnResult = false;
                    for (char[] fn : fname) {
                        if (Utils.compareCharArr(fn, possible.fname) == 0) {
                            fnResult = true;
                            break;
                        }
                    }
                    if (!fnResult)
                        continue;
                }
            }
            if(fnameExists != null) {
                if(fnameExists) {
                    if(possible.fname == null)
                        continue;
                } else {
                    if(possible.fname != null)
                        continue;
                }
            }


            if(isAdd && sname != null) {
                if(snameExists != null) {
                    if(snameExists) {
                        if(possible.sname == null)
                            continue;
                    } else {
                        if(possible.sname != null)
                            continue;
                    }
                } else {
                    if(snameEq != null && snameEq) {
                        if(possible.sname == null || Utils.compareCharArr(possible.sname, sname) != 0)
                            continue;
                    } else {
                        if(possible.sname == null || !Utils.startWith(possible.sname, sname))
                            continue;
                    }
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
                            || !AllLists.phoneCodeAccounts.get(phoneCode).contains(possible.id))
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

            if(isAdd && likes != null) {
                for(String like : likes) {
                    boolean founded = false;
                    int likeInt = Integer.parseInt(like);
                    for(int likeId : AllLists.likesAccounts.get(possible.id)) {
                        if(likeInt == likeId) {
                            founded = true;
                            break;
                        }
                    }
                    if(!founded) {
                        isAdd = false;
                        break;
                    }
                }
            }

            if (isAdd) {
                if(filter.unsorted) {
                    accauntCollector.add(possible);
                } else {
                    buildResult(isFirst, possible, ansSex, ansStatus, ansFname, ansSname, ansPhone, ansCountry, ansCity, ansBirth, ansPremium, buf);
                    isFirst = false;
                    ++totalAdded;

                    if(totalAdded >= limit)
                        break;
                }
            }
        }
        if(filter.unsorted) {
            int t = accauntCollector.size();
            Account[] list = new Account[t];
            for(int i=0; i<t; ++i) {
                list[i] = accauntCollector.poll();
            }
            for(int i=t-1; i>=0; --i) {
                buildResult(isFirst, list[i], ansSex, ansStatus, ansFname, ansSname, ansPhone, ansCountry, ansCity, ansBirth, ansPremium, buf);
                isFirst = false;
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

        if (account.email != null) {
            buf.append(",\"email\":\"");
            buf.append(account.email).append("@").append(AllLists.domainList.get(account.emailDomain));
            buf.append("\"");
        }

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
        if (ansFname) {
            char[] fnameRes = account.fname;
            if (fnameRes != null) {
                buf.append(",\"fname\":\"");
                buf.append(fnameRes);
                buf.append("\"");
            }
        }
        if (ansSname) {
            char[] snameRes = account.sname;
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
