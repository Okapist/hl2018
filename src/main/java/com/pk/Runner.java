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
                System.out.println("START LINUX LOAD");
                loader.load("/tmp/data/", new AppProxy());
                System.out.println("END LINUX LOAD");
            } catch (Exception ex) {
                System.out.println("START WINDOWS LOAD");
                //loader.load("d:/hl/bigdata/", new AppProxy());
                loader.load("d:/hl/data/", new AppProxy());
                //loader.load("C:\\JavaProjects\\external\\hl\\bigdata\\", new AppProxy());
                //loader.load("C:\\JavaProjects\\external\\hl\\data\\", new AppProxy());
                System.out.println("END WINDOWS LOAD");
            }
            loader = null;
        }
        System.gc();
        System.out.println("LAST GC CALLED");

        Server server = new Server();
        server.start();
        System.out.println("SERVER STARTED");
    }
}
