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

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageScanner {
    private static PackageScanner INSTANCE;
    private Set<Class<?>> classes;
    private Class<? extends Annotation> configurationWarningClass;

    public static PackageScanner getInstance() {
        if (INSTANCE == null){
            INSTANCE = new PackageScanner();
        }
        return INSTANCE;
    }

    public Class<? extends Annotation> getConfigurationWarningClass() {
        return configurationWarningClass;
    }

    public PackageScanner() {
        java.io.InputStream is = this.getClass().getClassLoader().getResourceAsStream("properties-from-pom.properties");
        java.util.Properties p = new Properties();
        try {
            p.load(is);
            String dependency = p.getProperty("package");
            try {
                configurationWarningClass = (Class<? extends Annotation>) (dependency.equals("nl.nn.adapterframework")?Class.forName("nl.nn.adapterframework.configuration.ConfigurationWarning"):Class.forName("org.frankframework.configuration.ConfigurationWarning"));
            } catch (ClassNotFoundException e) {
                Logger.getINSTANCE().log("Could not find configuration warning class");
            }
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .addScanners(Scanners.SubTypes.filterResultsBy(s->true)/*Override the default behavior which exclude Object class*/)
                    .forPackages(dependency));

            List<String> packages = Arrays.asList("pipes", "receivers", "parameters", "senders", "processors", "util", "jdbc", "http", "compression", "errormessageformatters", "ftp", "scheduler");
            classes = reflections.getSubTypesOf(Object.class);
            List<Class<?>> classes = new ArrayList<>();
            packages.forEach(pack -> classes.addAll(Stream.of(dependency+ "." + pack)
                    .flatMap(pkg -> {
                        List<Class<?>> pkgClasses = new ArrayList<>(getClassesIn(pkg));
                        if (!pkgClasses.isEmpty()) {
                            return pkgClasses.stream();
                        }else return Stream.empty();
                    })
                    .toList()));
            this.classes = new HashSet<>(classes);
        }catch (IOException e){
            Logger.getINSTANCE().log("Could not load properties file in target/classes");
        }
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }

    private Set<Class<?>> getClassesIn(String scanTarget){
        return classes.stream().filter(c->c.getPackage().getName().startsWith(scanTarget)).collect(Collectors.toSet()); /*Default filter doesn't work, so we have to make our own filter*/
    }

}
