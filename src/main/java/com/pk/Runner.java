package com.pk;

import com.pk.dbproxy.AppProxy;
import com.pk.jsonloader.JsonLoader;
import com.pk.webserver.Server;


public class Runner {

    public static int curDate;

    public static void main(String[] args) throws Exception {
        {
            JsonLoader loader = new JsonLoader();

            try {
                loader.load("/tmp/data/", new AppProxy());
            } catch (Exception ex) {
                //loader.load("d:/hl/bigdata/", new AppProxy());
                //loader.load("d:/hl/data/", new AppProxy());
                loader.load("C:\\JavaProjects\\external\\hl\\bigdata\\", new AppProxy());
                //loader.load("C:\\JavaProjects\\external\\hl\\data\\", new AppProxy());
            }
            loader = null;
        }
        System.gc();

        Server server = new Server();
        server.start();
    }
}
