package com.pk.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostLists {


    public static boolean isNewCountry = false;
    public static boolean isNewCity = false;
    public static boolean isNewEmailDomain = false;

    public static final List<String> fnames = new ArrayList<>();
    public static final List<String> snames = new ArrayList<>();

    public static final List<String> newEmails = new ArrayList<>();
    public static final List<String> newEmailDomains = new ArrayList<>();

    public static final Set<Integer> usedEmailDomain = new HashSet<>();

    public static final List<int[]> newLikes = new ArrayList<>();

    static {
        fnames.add("");
        snames.add("");
        newEmails.add("");
        newEmailDomains.add("");
    }
}
