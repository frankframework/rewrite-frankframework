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

import org.frankframework.pipes.WronglyAnnotatedClass;
import org.frankframework.configuration.WrongAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnnotationExtractorTest {

    @BeforeEach
    void setUp() {
        Logger.getINSTANCE().getLog().clear();
    }

    @Test
    void testExtractNewAttributesFromConfigurationWarning_MethodFound() throws Exception {
        class TestClass {
            public void setExampleMethod() {
                //example
            }
            @Deprecated
            public void setExampleMethodDeprecated() {
                //example
            }
        }
        Method deprecatedMethod = TestClass.class.getDeclaredMethod("setExampleMethodDeprecated");
        Method extractedMethod = AnnotationExtractor.extractNewAttributesFromConfigurationWarning("exampleMethod", TestClass.class, deprecatedMethod);

        assertNotNull(extractedMethod);
        assertEquals("setExampleMethod", extractedMethod.getName());
    }

    @Test
    void testExtractNewAttributesFromConfigurationWarning_MethodNotFound() {
        class TestClass {}

        Exception exception = assertThrows(Exception.class, () ->
          AnnotationExtractor.extractNewAttributesFromConfigurationWarning("nonExistentMethod", TestClass.class, null)
        );

        assertTrue(exception.getMessage().contains("No updated method/attribute implementation found"));
    }

    @Test
    void testExtractNewClassFromConfigurationWarning_ClassFound() throws Exception {
        Map<String, Class<?>> classLookup = new HashMap<>();
        classLookup.put("NewClass", String.class);

        Class<?> extractedClass = AnnotationExtractor.extractNewClassFromConfigurationWarning("NewClass", classLookup, Integer.class);

        assertNotNull(extractedClass);
        assertEquals(String.class, extractedClass);
    }

    @Test
    void testExtractNewClassFromConfigurationWarning_ClassNotFound() {
        Map<String, Class<?>> classLookup = new HashMap<>();

        Exception exception = assertThrows(Exception.class, () ->
          AnnotationExtractor.extractNewClassFromConfigurationWarning("UnknownClass", classLookup, Integer.class)
        );

        assertTrue(exception.getMessage().contains("No updated class implementation found"));
    }

    @Test
    void testExtractNewClassFromConfigurationWarning_MultipleClassesFound() {
        Map<String, Class<?>> classLookup = new HashMap<>();
        classLookup.put("DuplicateClass", String.class);
        classLookup.put("Kaas", Boolean.class);

        Exception exception = assertThrows(Exception.class, () ->
          AnnotationExtractor.extractNewClassFromConfigurationWarning("DuplicateClass Kaas", classLookup, Double.class)
        );

        assertTrue(exception.getMessage().contains("Multiple class names found"));
    }

    @Test
    void testGetConfigurationWarningValue_ThrowsIllegalStateException() {
        try (MockedStatic<PackageScanner> staticPackageScanner = mockStatic(PackageScanner.class)) {
            PackageScanner packageScanner = mock(PackageScanner.class);
            // Use doAnswer to mock the method that returns a wildcard Class<? extends Annotation>
            doAnswer(invocation -> WrongAnnotation.class) // Returning the specific annotation class
                    .when(packageScanner).getConfigurationWarningClass();
            List<Class<?>> classes = new ArrayList<>();
            classes.add(WronglyAnnotatedClass.class);

            when(packageScanner.getClasses()).thenReturn(new HashSet<>(classes));
            staticPackageScanner.when(PackageScanner::getInstance).thenReturn(packageScanner);

            Set<Class<?>> getClasses = PackageScanner.getInstance().getClasses();
            Class<?> annotatedClass =
                    getClasses.stream()
                            .filter(clazz -> clazz.getSimpleName().equalsIgnoreCase("WronglyAnnotatedClass"))
                            .findFirst().get();

            Exception exception = assertThrows(RuntimeException.class, () ->
                    AnnotationExtractor.getConfigurationWarningValue(annotatedClass));

            assertEquals("The annotation does not have a 'value()' method.", exception.getMessage());

        }
    }
}
