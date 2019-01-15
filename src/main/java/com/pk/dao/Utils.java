package com.pk.dao;

import com.pk.model.AllLists;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
        if(c1 == null || c2 == null || c1.length==0 || c2.length == 0)
            return -1;

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


    public static int getSnameIndex(String sname) {
        for (int i = 0; i < snames.length; i++) {
            char[] sn = snames[i];
            if (new String(sn).equals(sname))
                return i;

        }
        return -1;
    }

    public static int getFnameIndex(String fname) {
        for (int i = 0; i < fnames.length; i++) {
            char[] fn = fnames[i];
            if (new String(fn).equals(fname))
                return i;

        }
        return -1;
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
/*
    public static int searchBirth(int birth) {
        int low = 0;
        int high = AllLists.birthAccount.length-1;
        int[] list = AllLists.birthAccount;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = list[mid];

            int midToCompare = AllLists.allAccounts[midVal].birth;

            int cmp = midToCompare - birth;

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return low;  // key not found
    }
*/
}
