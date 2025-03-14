package org.frankrewrite.recipes.util;

import java.util.ArrayList;

public class Logger {
    private static Logger INSTANCE;
    private ArrayList<String> log;

    public Logger() {
        log = new ArrayList<>();
    }

    public static Logger getINSTANCE() {
        if (INSTANCE==null){
            INSTANCE = new Logger();
        }
        return INSTANCE;
    }

    public void log(String msg){
        log.add(msg);
    }

    public ArrayList<String> getLog() {
        return log;
    }
}
