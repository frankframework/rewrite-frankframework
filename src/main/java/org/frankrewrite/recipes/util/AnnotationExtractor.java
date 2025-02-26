package org.frankrewrite.recipes.util;

import jakarta.el.MethodNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationExtractor {
    public static ArrayList<String> log = new ArrayList<>();

    public static Method extractNewAttributesFromConfigurationWarning(String warning, Class<?> clazz, Method deprecatedMethod) {
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
    public static String getConfigurationWarningValue(Class<?> clazz) {
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
    public static String getConfigurationWarningValue(Method method) {
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

    public static @NotNull Class<?> extractNewClassFromConfigurationWarning(String warning,
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
            log.add(deprecatedClass.getSimpleName()+": Multiple class names found in warning: "+warning);
            throw new ClassNotFoundException(deprecatedClass.getSimpleName()+": Multiple class names found in warning: " + warning);
        }
    }
    public static ArrayList<String> getLog() {
        return log;
    }

}
