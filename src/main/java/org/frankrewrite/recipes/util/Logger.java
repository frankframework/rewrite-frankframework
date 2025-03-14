package org.frankrewrite.recipes.util;

import java.util.ArrayList;
import java.util.List;

public class Logger {
    private static Logger instance;
    private ArrayList<String> log;

    public Logger() {
        log = new ArrayList<>();
    }

    public static Logger getInstance() {
        if (instance ==null){
            instance = new Logger();
        }
        return instance;
    }

    public void log(String msg){
        log.add(msg);
    }

    public List<String> getLog() {
        return log;
    }
}
