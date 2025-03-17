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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PackageScannerTest {
    private PackageScanner packageScanner;

    @BeforeEach
    void setUp() {
        packageScanner = PackageScanner.getInstance();
    }

    @Test
    void testSingletonInstance() {
        PackageScanner anotherInstance = PackageScanner.getInstance();
        assertSame(packageScanner, anotherInstance, "PackageScanner should be a singleton");
    }

    @Test
    void testGetConfigurationWarningClass() {
        Class<? extends Annotation> warningClass = packageScanner.getConfigurationWarningClass();
        assertNotNull(warningClass, "ConfigurationWarningClass should not be null");
    }

    @Test
    void testGetClasses() {
        Set<Class<?>> classes = packageScanner.getClasses();
        assertNotNull(classes, "Classes set should not be null");
        assertFalse(classes.isEmpty(), "Classes set should not be empty");
    }
}
