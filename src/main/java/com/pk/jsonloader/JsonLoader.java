package com.pk.jsonloader;

import com.google.gson.Gson;
import com.pk.Runner;
import com.pk.dao.Utils;
import com.pk.dbproxy.AppProxy;
import com.pk.jsonmodel.Accounts;
import com.pk.model.AllLists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class JsonLoader {

    public void load(String path, AppProxy appProxy) throws IOException, SQLException {

        boolean isNix = path.startsWith("/");

        if (isNix) {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("unzip", path + "data.zip", "-d", path.startsWith("/") ? "/tmp/mydata" : path);
            Process process = pb.start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<String> options = Files.readAllLines(Paths.get(path + "options.txt"));
        Runner.curDate = Integer.parseInt(options.get(0));

        for(int j=0; j< AllLists.yearToTs.length; ++j) {
            AllLists.yearToTs[j] = Utils.getTimestamp(j+1930);
        }
        for(short j=0; j<AllLists.shortCache.length; ++j) {
            AllLists.shortCache[j] = j;
        }

        String basePath = path.startsWith("/") ? "/tmp/mydata" : path;

        System.out.println("start file processing");
        int i = 1;
        while (Files.exists(Paths.get(basePath + "/accounts_" + i + ".json"))) {
            try {
                Path file = Paths.get(basePath + "/accounts_" + i + ".json");

                if (i % 10 == 0)
                    System.out.println(file.getFileName().toString());

                byte[] fileData = Files.readAllBytes(file);
                Gson g = new Gson();
                Accounts myObjects = g.fromJson(new String(fileData), Accounts.class);

                appProxy.load(myObjects);
                fileData = null;
                myObjects = null;
                g = null;
                if (isNix)
                    Files.delete(file);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            ++i;
        }
        System.out.println("all files processed");
        appProxy.tempEmailIndex.clear();
        appProxy.tempEmailIndex = null;
        System.gc();

        appProxy.commitLikes();
        System.gc();

        appProxy.commitUsedEmails();

        appProxy.commitInterests();
        System.gc();

        appProxy.sortEasyIndexes();
        System.gc();
        System.out.println("sorted");

        appProxy.createBirthYearIndex();
        System.out.println("birth index created");

        //appProxy.createEmailIndexes();
        //System.out.println("email index created");

        appProxy.createNameIndexes();
        System.out.println("FSname index created");

        System.gc();

        appProxy.sortAccounts();

        appProxy.buildCountryCityList();

        appProxy.createFilters();
        System.gc();

        appProxy.commitCountryCityAccounts();
    }
}
