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

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ElementMapper {

    // Cache for deprecated class to new class mapping
    private static final Map<Class<?>, Class<?>> deprecatedClassToNewClassMapCache = new ConcurrentHashMap<>();
    // Cache for deprecated method to new method mapping per class
    private static final Map<String, Map<Method, Method>> deprecatedMethodToNewMethodMapCache = new ConcurrentHashMap<>();

    private ElementMapper() {}

    public static Map<Class<?>, Class<?>> getDeprecatedClassToNewClassMapInPackage() {
        // Check cache first
        if (!deprecatedClassToNewClassMapCache.isEmpty()) {
            return deprecatedClassToNewClassMapCache;
        }

        Map<Class<?>, Class<?>> result = new HashMap<>();
        Set<Class<?>> classesIn = PackageScanner.getInstance().getClasses();

        // Precompute lookup map for fast name-based searches
        Map<String, Class<?>> classNameToClassMap = getClassNameToClassMap(classesIn);

        classesIn.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Deprecated.class))
                .forEach(deprecatedClass -> {
                    try {
                        String warning = AnnotationExtractor.getConfigurationWarningValue(deprecatedClass);
                        Class<?> newClass = AnnotationExtractor.extractNewClassFromConfigurationWarning(warning, classNameToClassMap, deprecatedClass);
                        result.put(deprecatedClass, newClass);
                        deprecatedClassToNewClassMapCache.putIfAbsent(deprecatedClass, newClass);
                    } catch (Exception e) {
                        Logger.getInstance().log(e.getMessage()); // Log the issue
                    }
                });

        return result;
    }

    public static Map<Method, Method> getDeprecatedMethodToNewMethodMapForClass(String className) {
        // Check cache first
        if (deprecatedMethodToNewMethodMapCache.containsKey(className)) {
            return deprecatedMethodToNewMethodMapCache.get(className);
        }

        Map<Method, Method> result = new HashMap<>();
        Set<Class<?>> classesIn = PackageScanner.getInstance().getClasses();

        classesIn.stream()
                .filter(clazz -> clazz.getSimpleName().equals(className))
                .forEach(classIn -> {
                    Map<Method, Method> deprecatedMethodNewMethodMap = new HashMap<>();
                    for (Method deprecatedMethod : classIn.getMethods()) {
                        if (deprecatedMethod.isAnnotationPresent(Deprecated.class)) {
                            try {
                                String warning = AnnotationExtractor.getConfigurationWarningValue(deprecatedMethod);
                                Method newMethod = AnnotationExtractor.extractNewAttributesFromConfigurationWarning(warning, classIn, deprecatedMethod);
                                deprecatedMethodNewMethodMap.put(deprecatedMethod, newMethod);
                            } catch (Exception e) {
                                Logger.getInstance().log(e.getMessage());
                            }
                        }
                    }

                    if (!deprecatedMethodNewMethodMap.isEmpty()) {
                        result.putAll(deprecatedMethodNewMethodMap);
                    }
                });

        // Cache the result for future calls
        deprecatedMethodToNewMethodMapCache.put(className, result);
        return result;
    }

    private static Map<String, Class<?>> getClassNameToClassMap(Set<Class<?>> classesIn) {
        //Convert Set<Class<?>> to a Map<String, Class<?>> where map key is the simple name of the map value.
        return classesIn.stream()
                .collect(Collectors.toMap(
                        Class::getSimpleName,
                        clazz -> clazz,
                        (existing, duplicate) -> existing // Keep the first one
                ));
    }
}
