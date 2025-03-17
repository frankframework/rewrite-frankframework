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
    private static PackageScanner instance;
    private Set<Class<?>> classes;
    private Class<? extends Annotation> configurationWarningClass;

    public static PackageScanner getInstance() {
        if (instance == null) {
            instance = new PackageScanner();
        }
        return instance;
    }

    public Class<? extends Annotation> getConfigurationWarningClass() {
        return configurationWarningClass;
    }

    private PackageScanner() {
        java.io.InputStream is = this.getClass().getClassLoader().getResourceAsStream("properties-from-pom.properties");
        java.util.Properties p = new Properties();
        try {
            p.load(is);
            String dependency = p.getProperty("package");
            resolveConfigurationWarningClass(dependency);

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .addScanners(Scanners.SubTypes.filterResultsBy(s -> true)) // Override the default behavior which excludes Object class
                    .forPackages(dependency));

            List<String> packages = Arrays.asList("pipes", "receivers", "parameters", "senders", "processors", "util", "jdbc", "http", "compression", "errormessageformatters", "ftp", "scheduler");
            classes = reflections.getSubTypesOf(Object.class);
            packages.forEach(pack -> classes.addAll(Stream.of(dependency + "." + pack)
                    .flatMap(pkg -> {
                        List<Class<?>> pkgClasses = new ArrayList<>(getClassesIn(pkg));
                        return pkgClasses.isEmpty() ? Stream.empty() : pkgClasses.stream();
                    })
                    .toList()));
            this.classes = new HashSet<>(classes);
        } catch (IOException e) {
            Logger.getInstance().log("Could not load properties file in target/classes");
        }
    }

    private void resolveConfigurationWarningClass(String dependency) {
        try {
            this.configurationWarningClass = (Class<? extends Annotation>) (dependency.equals("nl.nn.adapterframework")
                    ? Class.forName("nl.nn.adapterframework.configuration.ConfigurationWarning")
                    : Class.forName("org.frankframework.configuration.ConfigurationWarning"));
        } catch (ClassNotFoundException e) {
            Logger.getInstance().log("Could not find configuration warning class");
        }
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }

    private Set<Class<?>> getClassesIn(String scanTarget){
        return classes.stream().filter(c->c.getPackage().getName().startsWith(scanTarget)).collect(Collectors.toSet()); /*Default filter doesn't work, so we have to make our own filter*/
    }

}
