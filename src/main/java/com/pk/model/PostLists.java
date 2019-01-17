package com.pk.model;

import com.pk.dao.BaseFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostLists {



    {
        fnames.add("");
        snames.add("");
    }

    public static List<String> fnames = new ArrayList<>();
    public static List<String> snames = new ArrayList<>();
    public static List<Integer> accIdAdded = new ArrayList<>();

    public static Set<Integer> usedEmailDomain = new HashSet<>();
    public static Set<Integer> freeEmailDomain = new HashSet<>();
}
