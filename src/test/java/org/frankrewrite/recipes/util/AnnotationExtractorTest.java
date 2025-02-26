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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationExtractorTest {

    @BeforeEach
    void setUp() {
        AnnotationExtractor.getLog().clear();
    }

    @Test
    void testExtractNewAttributesFromConfigurationWarning_MethodFound() throws NoSuchMethodException {
        class TestClass {
            public void setExampleMethod() {}
            @Deprecated
            public void setExampleMethodDeprecated() {}
        }
        Method deprecatedMethod = TestClass.class.getDeclaredMethod("setExampleMethodDeprecated");
        Method extractedMethod = AnnotationExtractor.extractNewAttributesFromConfigurationWarning("exampleMethod", TestClass.class, deprecatedMethod);

        assertNotNull(extractedMethod);
        assertEquals("setExampleMethod", extractedMethod.getName());
    }

    @Test
    void testExtractNewAttributesFromConfigurationWarning_MethodNotFound() {
        class TestClass {}

        MethodNotFoundException exception = assertThrows(MethodNotFoundException.class, () ->
          AnnotationExtractor.extractNewAttributesFromConfigurationWarning("nonExistentMethod", TestClass.class, null)
        );

        assertTrue(exception.getMessage().contains("No updated method/attribute implementation found"));
    }

    @Test
    void testExtractNewClassFromConfigurationWarning_ClassFound() throws ClassNotFoundException {
        Map<String, Class<?>> classLookup = new HashMap<>();
        classLookup.put("NewClass", String.class);

        Class<?> extractedClass = AnnotationExtractor.extractNewClassFromConfigurationWarning("NewClass", classLookup, Integer.class);

        assertNotNull(extractedClass);
        assertEquals(String.class, extractedClass);
    }

    @Test
    void testExtractNewClassFromConfigurationWarning_ClassNotFound() {
        Map<String, Class<?>> classLookup = new HashMap<>();

        ClassNotFoundException exception = assertThrows(ClassNotFoundException.class, () ->
          AnnotationExtractor.extractNewClassFromConfigurationWarning("UnknownClass", classLookup, Integer.class)
        );

        assertTrue(exception.getMessage().contains("No updated class implementation found"));
    }

    @Test
    void testExtractNewClassFromConfigurationWarning_MultipleClassesFound() {
        Map<String, Class<?>> classLookup = new HashMap<>();
        classLookup.put("DuplicateClass", String.class);
        classLookup.put("Kaas", Boolean.class);

        ClassNotFoundException exception = assertThrows(ClassNotFoundException.class, () ->
          AnnotationExtractor.extractNewClassFromConfigurationWarning("DuplicateClass Kaas", classLookup, Double.class)
        );

        assertTrue(exception.getMessage().contains("Multiple class names found"));
    }
}
