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

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationExtractor {
    public static ArrayList<String> log = new ArrayList<>();

    public static Method extractNewAttributesFromConfigurationWarning(String warning, Class<?> clazz, Method deprecatedMethod) throws Exception {
        //Split segments into a list of words
        String[] segments = warning.split(" ");

        List<Method> matchedMethods = Arrays.stream(segments)
                // Replace invalid keys from warning segment and format it to match setter methods
                .map(segment -> "set" + segment.replace(".", "").replace("'", "").replace("\"", "").toLowerCase())
                // Lookup methods referring to the segment
                .flatMap(expectedMethodName -> Arrays.stream(clazz.getMethods())
                        .filter(method -> method.getName().equalsIgnoreCase(expectedMethodName) && !method.equals(deprecatedMethod)))
                .toList(); // Collect results into a list

        if (matchedMethods.size() == 1) {
            return matchedMethods.get(0); // Java 16+ or matchedMethods.get(0);
        } else {
            log.add(clazz.getSimpleName() + ": No updated method/attribute implementation found in warning: " + warning);
            throw new Exception("No updated method/attribute implementation found in warning: " + warning);
        }
    }

    private static String getConfigurationWarningValue(AnnotatedElement element) {
        try {
            // Retrieve the annotation using the PackageScanner instance
            Annotation annotation = element.getAnnotation(PackageScanner.getInstance().getConfigurationWarningClass());

            // Look up the 'value' method in the actual annotation type
            Method valueMethod = annotation.annotationType().getMethod("value");

            // Invoke the 'value' method and return its result as a string
            Object result = valueMethod.invoke(annotation);
            return result.toString(); // or cast to the appropriate type if needed
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("The annotation does not have a 'value()' method.", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke 'value()' on the annotation.", e);
        }
    }

    // Overloaded methods to handle Class and Method specifically
    public static String getConfigurationWarningValue(Class<?> clazz) {
        return getConfigurationWarningValue((AnnotatedElement) clazz);
    }
    public static String getConfigurationWarningValue(Method method) {
        return getConfigurationWarningValue((AnnotatedElement) method);
    }

    public static @NotNull Class<?> extractNewClassFromConfigurationWarning(String warning,
                                                                            Map<String, Class<?>> classLookup,
                                                                            Class<?> deprecatedClass) throws Exception {
        // Split the warning message into individual words
        String[] segments = warning.split(" ");

        // First attempt: Find a matching class in the lookup map
        List<Class<?>> foundClasses = Arrays.stream(segments)
                .map(segment -> classLookup.get(segment.replace(".", "")))
                .filter(Objects::nonNull)
                .filter(clazz -> !clazz.equals(deprecatedClass)) // Exclude the deprecated class itself
                .distinct()
                .collect(Collectors.toList());

        // Second attempt: Retry with additional filtering (removing "Pipe" from segments)
        if (foundClasses.isEmpty()) {
            foundClasses = Arrays.stream(segments)
                    .map(segment -> classLookup.get(segment.replace(".", "").replace("Pipe", "")))
                    .filter(Objects::nonNull)
                    .filter(clazz -> !clazz.equals(deprecatedClass))
                    .distinct()
                    .collect(Collectors.toList());
        }

        // If no class is found, log and throw an exception
        if (foundClasses.isEmpty()) {
            log.add(deprecatedClass.getSimpleName() + ": No updated class implementation found in warning: " + warning);
            throw new Exception(deprecatedClass.getSimpleName() + ": No updated class implementation found in warning: " + warning);
        }
        // If exactly one class is found, check for specific warning cases
        else if (foundClasses.size() == 1) {
            if (warning.toLowerCase().contains("configure")) {
                log.add(deprecatedClass.getSimpleName() + ": Can't handle configure warnings properly, warning: " + warning);
                throw new Exception(deprecatedClass.getSimpleName() + ": Can't handle configure warnings properly, warning: " + warning);
            }
            return foundClasses.get(0); // Return the found class
        }
        // If multiple classes are found, log and throw an exception
        else {
            log.add(deprecatedClass.getSimpleName() + ": Multiple class names found in warning: " + warning);
            throw new Exception(deprecatedClass.getSimpleName() + ": Multiple class names found in warning: " + warning);
        }
    }

    // Make log public for ElementMapper to use it
    public static ArrayList<String> getLog() {
        return log;
    }

}
