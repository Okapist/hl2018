package com.pk.model;

import java.util.*;

public class AllLists {
    public static final Account[] allAccounts = new Account[1_400_000];

    public static final List<int[]> likesAccounts = new ArrayList<>(); //кого аккаунт лайкал hm<accId, ts>
    public static final List<List<Integer>> likesTO = new ArrayList<>(); //кто лайкал данный аккаунт

    public static final List<List<Integer>> domainAccounts = new ArrayList<>();
    public static final List<List<Integer>> cityAccounts = new ArrayList<>();
    public static final List<List<Integer>> countryAccounts = new ArrayList<>();
    public static final HashMap<String, List<Integer>> fnameAccounts = new HashMap<>();
    public static final HashMap<String, List<Integer>> snameAccounts = new HashMap<>(); //sorder sname
    public static final HashMap<String, List<Integer>> phoneCodeAccounts = new HashMap<>();

    public static final List<List<Integer>> statusAccounts = new ArrayList<>();

    public static final HashMap<String, Integer> interests = new HashMap<>(); //прямой и обратный индекс по интересам
    public static final HashMap<Integer,String> interestsById = new HashMap<>();

    public static final List<List<Integer>> interestAccounts = new ArrayList<>(); //аккаунты с указанным интересом

    public static final List<Integer> premiumNowAccounts = new ArrayList<>();
    public static final List<Integer> premiumEverAccounts = new ArrayList<>();
    public static final List<Integer> premiumNeverAccounts = new ArrayList<>();

    //unsorted
    public static final List<Integer> emailSortedAccounts = new ArrayList<>();
    public static final int[] emailFirst = new int[26]; //First email letter index

    //unsorted
    public static final List<Integer> birthSortedAccounts = new ArrayList<>();
    public static final int[] birthYears = new int[100];

    //unsorted
    public static final List<Integer> joinedSortedAccounts = new ArrayList<>();
    public static final int[] joinedYears = new int[100];

    public static final List<String> citiesList = new ArrayList<>();
    public static final List<String> countriesList = new ArrayList<>();
    public static final List<char[]> domainList = new ArrayList<>();

    public static final int[] yearToTs = new int[100];
    public static final Short shortCache[] = new Short[32000];
}
