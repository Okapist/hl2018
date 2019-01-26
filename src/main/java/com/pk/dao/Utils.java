package com.pk.dao;

import com.pk.model.AllLists;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.pk.model.AllLists.fnames;
import static com.pk.model.AllLists.snames;

public class Utils {

    public static String getStatusText(Byte status) {

        if(status != null) {
            switch (status) {
                case 1:
                    return "свободны";
                case 2:
                    return "всё сложно";
                case 3:
                    return "заняты";
            }
        }
        return null;
    }

    public static Short findCountryIndexBinary(String name) {

        if(name == null || "".equals(name))
            return null;

        List<String> countryList = AllLists.countriesList;
        int index = Collections.binarySearch(countryList, name);
        return (index>-1?(short)index:null);
    }

    public static Short findCityIndexBinary(String name) {

        if(name == null || "".equals(name))
            return null;

        List<String> cityList = AllLists.citiesList;
        int index = Collections.binarySearch(cityList, name);
        return (index>-1?AllLists.shortCache[index]:null);

    }

    public static Short findCityIndex(String name) {

        if(name == null || "".equals(name))
            return null;

        List<String> citiesList = AllLists.citiesList;
        for (short i = 0; i < citiesList.size(); i++) {
            String city = citiesList.get(i);
            if (city.equals(name)) {
                return AllLists.shortCache[i];
            }
        }
        return null;
    }

    public static Short findCountryIndex(String name) {

        if(name == null || "".equals(name))
            return null;

        List<String> countryList = AllLists.countriesList;
        for (short i = 0; i < countryList.size(); i++) {
            String country = countryList.get(i);
            if (country.equals(name)) {
                return AllLists.shortCache[i];
            }
        }
        return null;
    }

    public static Integer findDomainIndexBinary(char[] name) {

        if(name == null || "".equals(name))
            return null;

        List<char[]> domainList = AllLists.domainList;
        int index = Collections.binarySearch(domainList, name, Utils::compareCharArr);
        return (index>-1?index:null);
    }


    public static Integer findDomainIndex(char[] name) {

        if(name == null || name.length == 0)
            return null;

        List<char[]> domainList = AllLists.domainList;
        for (int i = 0; i < domainList.size(); i++) {
            char[] domain = domainList.get(i);
            if (Utils.compareCharArr(domain, name) == 0) {
                return i;
            }
        }
        return null;
    }

    public static boolean likeToContainsId(int[] ints, int cur) {

        for(int i=0 ;i<ints.length; i+=2) {
            if(ints[i] == cur)
                return true;
        }
        return false;
    }

    public static int compareCharArr(char[] c1, char[] c2) {

        if(c1 == null && c2 == null)
            return 0;

        if(c1 == null || c1.length==0)
            return -1;

        if(c2 == null || c2.length == 0)
            return 1;

        int min = Math.min(c1.length, c2.length);
        for(int i=0; i<min; ++i) {
            if(c1[i] > c2[i])
                return 1;
            if(c1[i] < c2[i])
                return -1;
        }
        if(c1.length == c2.length)
            return 0;

        return c1.length > c2.length?1:-1;
    }

    public static boolean startWith(char[] c1, char[] c2) {

        if(c1 == null || c2 == null || c1.length==0 || c2.length == 0 || c2.length > c1.length)
            return false;

        int min = c2.length;
        for(int i=0; i<min; ++i) {
            if(c1[i] != c2[i])
                return false;
        }
        return true;
    }

    public static int getSnameIndexBinary(String sname) {
        int index = Arrays.binarySearch(AllLists.snames, sname.toCharArray(), Utils::compareCharArr);
        return index>=0?index:-1;
    }

    public static int getSnameIndexBinary2(char[] sname) {
        return Arrays.binarySearch(AllLists.snames, sname, Utils::compareCharArr);
    }

    public static int getSnameIndexBinary(char[] sname) {
        int index = Arrays.binarySearch(AllLists.snames, sname, Utils::compareCharArr);
        return index>=0?index:-1;
    }

    public static int getFnameIndexBinary(String fname) {
        int index = Arrays.binarySearch(AllLists.fnames, fname.toCharArray(), Utils::compareCharArr);
        return index>=0?index:-1;
    }

    public static int getFnameIndexBinary(char[] fname) {
        int index = Arrays.binarySearch(AllLists.fnames, fname, Utils::compareCharArr);
        return index>=0?index:-1;
    }

    public static int getTimestamp(int year) {
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
