package com.pk.model;

import java.util.*;

public class AllLists {
    public static final Account[] allAccounts = new Account[1_400_000];

    public static final List<int[]> likesAccounts = new ArrayList<>(); //кого аккаунт лайкал hm<accId, ts>
    public static final int[][] likesTO = new int[1_400_000][]; //кто лайкал данный аккаунт

    public static final List<List<Integer>> domainAccounts = new ArrayList<>(); //updated
    public static final HashMap<String, List<Integer>> phoneCodeAccounts = new HashMap<>();//updated

    public static char[][] fnames;//updated
    public static char[][] snames;//updated

    public static int[][] fnameAccounts;//updated
    public static int[][] snameAccounts;//updated

    public static int[][] birthYearsAccount;//updated

    public static final List<String> citiesList = new ArrayList<>();//updated
    public static final List<String> countriesList = new ArrayList<>();//updated

    public static int[][]cityAccounts;//updated
    public static int[][] countryAccounts;//updated

    public static final List<String> allEmailList = new ArrayList<>();//updated

    public static final HashMap<String, Integer> interests = new HashMap<>(); //прямой и обратный индекс по интересам
    public static final HashMap<Integer,String> interestsById = new HashMap<>();

    public static final List<Integer> premiumNowAccounts = new ArrayList<>();
    public static final List<Integer> premiumEverAccounts = new ArrayList<>();
    public static final List<Integer> premiumNeverAccounts = new ArrayList<>();

    public static int MIN_BIRTH_YEAR = Integer.MAX_VALUE;
    public static int MAX_BIRTH_YEAR = Integer.MAX_VALUE;
    public static int MIN_JOINED_YEAR = Integer.MAX_VALUE;
    public static int MAX_JOINED_YEAR = Integer.MIN_VALUE;

    public static List<Short>[] countryCityList; //лист городов в стране

    public static final List<char[]> domainList = new ArrayList<>();//updated

    public static final int[] yearToTs = new int[100];
    public static final Short shortCache[] = new Short[32000];

    public static int[][] interestAccounts; //аккаунты с указанным интересом [interest][accountId] updated

    //[premium][status][country]<city><interes><accounts>
    public static final HashMap<Integer, HashMap<Integer, int[]>>[][][] recommendInteresFilter = new HashMap[2][3][];

    //[country][city][status][sex]
    public static HashMap<Short, short[][]>[] groupFilter;

    public static HashMap<Short, short[][][]>[] groupFilterBirth;
    public static HashMap<Short, short[][][]>[] groupFilterJoined;


    //bicycle
    //[birth][city][interes] = count
    public static short[][][] groupFilterBirthCityInterests;

    //bicycle
    //[birth][country][interes] = count
    public static short[][][] groupFilterBirthCountryInterests;

    public static final int[][][] emailLowBorder = new int[26][26][26];//updated
    public static final int[][][] emailHightBorder = new int[26][26][26];//updated

    public static int[] usedEmailDomain;

    public static final int[][] topInterests50 = new int[50][]; //interes - count
    public static final int[][] lowInterests50 = new int[50][]; //interes - count
}
