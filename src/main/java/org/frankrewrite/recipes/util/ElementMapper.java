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

import jakarta.el.MethodNotFoundException;
import org.jetbrains.annotations.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ElementMapper {

    // Cache for deprecated class to new class mapping
    private static final Map<Class<?>, Class<?>> deprecatedClassToNewClassMapCache = new ConcurrentHashMap<>();
    // Cache for deprecated method to new method mapping per class
    private static final Map<String, Map<Method, Method>> deprecatedMethodToNewMethodMapCache = new ConcurrentHashMap<>();

    // List for debugging
    public static ArrayList<String> log = new ArrayList<>();

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
                        String warning = getConfigurationWarningValue(deprecatedClass);
                        Class<?> newClass = extractNewClassFromConfigurationWarning(warning, classNameToClassMap, deprecatedClass);
                        result.put(deprecatedClass, newClass);
                        deprecatedClassToNewClassMapCache.putIfAbsent(deprecatedClass, newClass);
                    } catch (Exception e) {
                        System.out.println(e.getMessage()); // Log the issue
                    }
                });

        // Cache the result for future calls
//        deprecatedClassToNewClassMapCache.putIfAbsent();
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
                                String warning = getConfigurationWarningValue(deprecatedMethod);
                                Method newMethod = extractNewAttributesFromConfigurationWarning(warning, classIn, deprecatedMethod);
                                deprecatedMethodNewMethodMap.put(deprecatedMethod, newMethod);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }

                    if (!deprecatedMethodNewMethodMap.isEmpty()) {
                        result.putAll(deprecatedMethodNewMethodMap);
                    }
                });

        // Cache the result for future calls
        deprecatedMethodToNewMethodMapCache.put(className, result);
        log.forEach(System.out::println);
        return result;
    }

    private static Map<String, Class<?>> getClassNameToClassMap(Set<Class<?>> classesIn) {
        return classesIn.stream()
                .collect(Collectors.toMap(
                        Class::getSimpleName,
                        clazz -> clazz,
                        (existing, duplicate) -> existing // Keep the first one
                ));
    }

    private static String getConfigurationWarningValue(Class<?> clazz) {
        try {
            Annotation annotation = clazz.getAnnotation(PackageScanner.getInstance().getConfigurationWarningClass());
            // Look up the 'value' method in the actual annotation type.
            Method valueMethod = annotation.annotationType().getMethod("value");
            Object result = valueMethod.invoke(annotation);
            return result.toString(); // or cast to the appropriate type if needed
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("The annotation does not have a 'value()' method.", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke 'value()' on the annotation.", e);
        }
    }

    private static String getConfigurationWarningValue(Method method) {
        try {

            Annotation annotation = method.getAnnotation(PackageScanner.getInstance().getConfigurationWarningClass());
            // Look up the 'value' method in the actual annotation type.
            Method valueMethod = annotation.annotationType().getMethod("value");
            Object result = valueMethod.invoke(annotation);
            return result.toString(); // or cast to the appropriate type if needed
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("The annotation does not have a 'value()' method.", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke 'value()' on the annotation.", e);
        }
    }

    private static @NotNull Class<?> extractNewClassFromConfigurationWarning(String warning,
                                                                             Map<String, Class<?>> classLookup,
                                                                             Class<?> deprecatedClass) throws ClassNotFoundException {
        String[] segments = warning.split(" ");

        List<Class<?>> foundClasses = Arrays.stream(segments)
                .map(segment -> classLookup.get(segment.replace(".", "")))
                .filter(Objects::nonNull)
                .filter(clazz -> !clazz.equals(deprecatedClass))
                .distinct()
                .collect(Collectors.toList());
        if (foundClasses.isEmpty()) {
            foundClasses = Arrays.stream(segments)
                    .map(segment -> classLookup.get(segment.replace(".", "").replace("Pipe", "")))
                    .filter(Objects::nonNull)
                    .filter(clazz -> !clazz.equals(deprecatedClass))
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (foundClasses.isEmpty()) {
            log.add(deprecatedClass.getSimpleName()+": No updated class implementation found in warning: "+warning);
            throw new ClassNotFoundException(deprecatedClass.getSimpleName()+": No updated class implementation found in warning: " + warning);
        } else if (foundClasses.size() == 1) {
            if (warning.toLowerCase().contains("configure")){
                log.add(deprecatedClass.getSimpleName()+": Cant handle configure warnings properly, warning: "+warning);
                throw new ClassNotFoundException(deprecatedClass.getSimpleName()+": Cant handle configure warnings properly, warning: "+warning);
            }
            return foundClasses.get(0);
        } else {
//            System.out.println("Multiple class names found in warning: " + warning);
//            return foundClasses.get(0); // Returning the first match for now
            log.add(deprecatedClass.getSimpleName()+": Multiple class names found in warning: "+warning);
            throw new ClassNotFoundException(deprecatedClass.getSimpleName()+": Multiple class names found in warning: " + warning);
        }
    }

    private static Method extractNewAttributesFromConfigurationWarning(String warning, Class<?> clazz, Method deprecatedMethod) {
        String[] segments = warning.split(" ");

        return Arrays.stream(segments)
                .map(segment -> "set" + segment.replace(".", "").replace("'", "").replace("\"", "").toLowerCase())
                .flatMap(expectedMethodName -> Arrays.stream(clazz.getMethods())
                        .filter(method -> method.getName().equalsIgnoreCase(expectedMethodName) && !method.equals(deprecatedMethod)))
                .findFirst()
                .orElseThrow(() -> {
                    log.add(clazz.getSimpleName()+": No updated method/attribute implementation found in warning: " + warning);
                    return new MethodNotFoundException("No updated method/attribute implementation found in warning: " + warning);
                });
    }
}
