/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
