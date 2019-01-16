package com.pk.model;

import java.util.*;

public class AllLists {
    public static final Account[] allAccounts = new Account[1_400_000];

    public static final List<int[]> likesAccounts = new ArrayList<>(); //кого аккаунт лайкал hm<accId, ts>
    public static final List<List<Integer>> likesTO = new ArrayList<>(); //кто лайкал данный аккаунт

    public static final List<List<Integer>> domainAccounts = new ArrayList<>();
    public static final List<List<Integer>> cityAccounts = new ArrayList<>();
    public static final List<List<Integer>> countryAccounts = new ArrayList<>();
    public static final HashMap<String, List<Integer>> phoneCodeAccounts = new HashMap<>();

    public static char[][] fnames;
    public static char[][] snames;

    public static int[][] fnameAccounts;
    public static int[][] snameAccounts;

    public static int[][] statusAccounts  = new int[3][];
    public static int[][] birthYearsAccount;

    public static final HashMap<String, Integer> interests = new HashMap<>(); //прямой и обратный индекс по интересам
    public static final HashMap<Integer,String> interestsById = new HashMap<>();

    public static final List<Integer> premiumNowAccounts = new ArrayList<>();
    public static final List<Integer> premiumEverAccounts = new ArrayList<>();
    public static final List<Integer> premiumNeverAccounts = new ArrayList<>();

    public static int MIN_BIRTH_YEAR = Integer.MAX_VALUE;
    public static int MAX_BIRTH_YEAR = Integer.MAX_VALUE;
    public static int MIN_JOINED_YEAR = Integer.MAX_VALUE;
    public static int MAX_JOINED_YEAR = Integer.MAX_VALUE;

    //unsorted
    public static final List<Integer> joinedSortedAccounts = new ArrayList<>();
    public static final int[] joinedYears = new int[100];

    public static List<Short>[] countryCityList; //лист городов в стране
    //public static Ha<Short>[] countryCityListReversed; //set городов в стране

    public static final List<String> citiesList = new ArrayList<>();
    public static final List<String> countriesList = new ArrayList<>();
    public static final List<char[]> domainList = new ArrayList<>();

    public static final int[] yearToTs = new int[100];
    public static final Short shortCache[] = new Short[32000];

    public static int[][] interestAccounts; //аккаунты с указанным интересом [interest][accountId]

    //[premium][status][country]<city><interes><accounts>
    public static final HashMap<Integer, HashMap<Integer, Set<Integer>>>[][][] recommendInteresFilter = new HashMap[2][3][];

    //[country][city][status][sex]
    public static HashMap<Short, int[][]>[] groupFilter;

    public static HashMap<Short, int[][][]>[] groupFilterBirth;
    public static HashMap<Short, int[][][]>[] groupFilterJoined;

/*
Possible 2 groups
city, sex
country, sex
city, status
country, status
*/

/*
Possible filters

Boolean sex, Byte status, String interests, Short country,
Short city, Integer birth, String likes, Integer joined,

Possible filters from CityGroups
    status, interests, birth, likes, joined
2 of them
 */


    //[sex][status][country]<city><birth><joined><like><interests> = count
    //public static final HashMap<Integer, HashMap<Integer, HashMap<Integer,HashMap<Integer,HashMap<Integer,Integer>>>>>[][][] groupFilter = new HashMap[3][4][];
    //public static final HashMap<Integer, HashMap<Integer, Set<Integer>>>[][][] recommendInteresFilter = new HashMap[2][3][];
}
