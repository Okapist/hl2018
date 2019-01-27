package com.pk.dbproxy;

import com.pk.Runner;
import com.pk.dao.*;
import com.pk.jsonmodel.Account;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class Warmer {

    public Warmer() {
        Runner.isWarm = true;
    }

    public void warmIndexes() {
        Runner.isWarm = true;
        IndexCalculator indexCalculator = new IndexCalculator();
        for(int i=0; i<10; ++i) {
            indexCalculator.calculateIndexes();
        }
        indexCalculator.clearTempData();
    }

    public void warmGet() {
        System.out.println("warm get start ");
        warmFilter();
        warmGroup();
        warmRecommend();
        warmSuggest();
        System.out.println("warm get end ");
    }

    private void warmSuggest() {
        NewSuggest newSuggest = new NewSuggest();
        for(int i=0; i<10000; ++i) {
            newSuggest.suggest(1000, null,null, 50, new StringBuilder());
            newSuggest.suggest(10000, (short)1,null, 50, new StringBuilder());
            newSuggest.suggest(20000, null,(short)1, 50, new StringBuilder());
        }
    }

    private void warmRecommend() {
        NewRecommend newRecommend = new NewRecommend();

        for(int i=0; i<10000; ++i) {
            newRecommend.recommend(2000, null, null, 50, new StringBuilder());
            newRecommend.recommend(15000, (short)2, null, 50, new StringBuilder());
            newRecommend.recommend(23000, null, (short)2, 50, new StringBuilder());
        }
    }

    private void warmGroup() {
        NewAccGroup newAccGroup = new NewAccGroup();
        for(int i=0; i<10000; ++i) {
            newAccGroup.getGroups(Collections.singletonList("sex"), null, null, null, null, null
                    , null, null, null, 50, true, new StringBuilder());

            newAccGroup.getGroups(Collections.singletonList("status"), null, (byte)1, null, null, null
                    , null, null, null, 50, true, new StringBuilder());

            newAccGroup.getGroups(Collections.singletonList("country"), null, null, null, null, null
                    , null, null, null, 50, true, new StringBuilder());

            newAccGroup.getGroups(Collections.singletonList("interests"), null, null, null, null, (short)2
                    , null, null, null, 50, true, new StringBuilder());

            newAccGroup.getGroups(Collections.singletonList("city"), null, null, null, (short)1, null
                    , null, null, null, 50, true, new StringBuilder());
        }

    }

    private void warmFilter() {
        NewAccFilter filter = new NewAccFilter();
        System.out.println("WARM filter start " + Calendar.getInstance().getTimeInMillis());

        for(int i=0; i<10000; ++i) {
            filter.filter(null, null, null, null,
                    null, null, null, null,
                    -10, null, null,
                    null, null,
                    null, null,
                    null, null,
                    null, null, null,
                    null, null,
                    null,
                    null, null,
                    50, new StringBuilder(),  null);

            filter.filter(true, "email.com".toCharArray(), true, null,
                    null, null, null, null,
                    -10, null, null,
                    null, null,
                    null, null,
                    null, null,
                    null, null, null,
                    null, null,
                    null,
                    null, null,
                    50, new StringBuilder(),  null);

            filter.filter(true, null, null, null,
                    null, null, null, null,
                    -10, null, null,
                    null, false,
                    null, false,
                    null, null,
                    null, null, null,
                    null, null,
                    null,
                    null, null,
                    50, new StringBuilder(),  null);
            filter.filter(true, null, null, null,
                    null, null, null, null,
                    -10, null, null,
                    "903", null,
                    null, null,
                    null, null,
                    null, null, null,
                    null, null,
                    null,
                    null, null,
                    50, new StringBuilder(),  null);

            filter.filter(true, null, null, null,
                    null, null, null, null,
                    -10, null, null,
                    null, null,
                    null, null,
                    null, null,
                    null, null, null,
                    null, null,
                    null,
                    true, true,
                    50, new StringBuilder(),  "А".toCharArray());

        }
        System.out.println("WARM filter end " + Calendar.getInstance().getTimeInMillis());

    }

    public void warmPost() {
        Runner.isWarm = true;
        System.out.println("warm post start " + Calendar.getInstance().getTimeInMillis());
        warmNew();
        warmEdit();
        warmLikes();
        System.out.println("warm post end " + Calendar.getInstance().getTimeInMillis());
        Runner.isWarm = false;

    }

    private void warmLikes() {
        AddLikes addLikes = new AddLikes();

        List<int[]> data = new ArrayList<>();
        data.add(new int[] {10,20,10000});
        data.add(new int[] {110,210,100000});
        data.add(new int[] {120,220,190000});
        data.add(new int[] {130,240,100800});
        data.add(new int[] {160,260,1670000});
        data.add(new int[] {310,320,1076000});
        data.add(new int[] {410,420,104000});
        data.add(new int[] {610,620,1007400});
        data.add(new int[] {910,920,1450000});
        data.add(new int[] {310,620,1083000});

        for(int i=0; i<100000; ++i) {
            addLikes.addLikes(data);
        }
    }

    private void warmEdit() {
        EditAccount editAccount = new EditAccount();

        List<int[]> data = new ArrayList<>();
        data.add(new int[] {10,20,10000});
        data.add(new int[] {110,210,100000});
        data.add(new int[] {120,220,190000});
        data.add(new int[] {130,240,100800});
        data.add(new int[] {160,260,1670000});
        data.add(new int[] {310,320,1076000});
        data.add(new int[] {410,420,104000});
        data.add(new int[] {610,620,1007400});
        data.add(new int[] {910,920,1450000});
        data.add(new int[] {310,620,1083000});

        Account jsonAccount = new Account();
        jsonAccount.setPhone("8(903)1234567");
        jsonAccount.setCity("Город");
        jsonAccount.setCountry("Страна");
        jsonAccount.setFname("Имя");
        jsonAccount.setSname("Фамилия");
        jsonAccount.setSex("m");

        for(int i=0; i<100000; ++i) {
            editAccount.edit(i, jsonAccount, (byte)1, new String[] {"asddfst", "email.com"}, data, null, null);
        }
    }

    private void warmNew() {
        NewAccount newAccount = new NewAccount();

        List<int[]> data = new ArrayList<>();
        data.add(new int[] {10,20,10000});
        data.add(new int[] {110,210,100000});
        data.add(new int[] {120,220,190000});
        data.add(new int[] {130,240,100800});
        data.add(new int[] {160,260,1670000});
        data.add(new int[] {310,320,1076000});
        data.add(new int[] {410,420,104000});
        data.add(new int[] {610,620,1007400});
        data.add(new int[] {910,920,1450000});
        data.add(new int[] {310,620,1083000});

        Account jsonAccount = new Account();
        jsonAccount.setId(1_399_000);
        jsonAccount.setPhone("8(903)1234567");
        jsonAccount.setCity("Город");
        jsonAccount.setCountry("Страна");
        jsonAccount.setFname("Имя");
        jsonAccount.setSname("Фамилия");
        jsonAccount.setSex("m");

        for(int i=0; i<100000; ++i) {
            newAccount.create(jsonAccount, (byte)1, new String[] {"asddfst", "email.com"}, data, null, null);
        }
    }
}
