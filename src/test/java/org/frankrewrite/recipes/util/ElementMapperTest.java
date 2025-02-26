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
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ElementMapperTest {

    @BeforeEach
    void setUp() {
        ElementMapper.getDeprecatedClassToNewClassMapInPackage().clear();
    }

    @Test
    void testGetDeprecatedClassToNewClassMapInPackage() {
        Map<Class<?>, Class<?>> result = ElementMapper.getDeprecatedClassToNewClassMapInPackage();
        assertNotNull(result);
    }

    @Test
    void testGetDeprecatedMethodToNewMethodMapForClassWithDeprecatedMethod() {
        Map<Method, Method> result = ElementMapper.getDeprecatedMethodToNewMethodMapForClass("MySecondPipe");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetDeprecatedMethodToNewMethodMapForClassWithDeprecatedMethodWithInvalidWarning() {
        Map<Method, Method> result = ElementMapper.getDeprecatedMethodToNewMethodMapForClass("MyPipeTwo");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetDeprecatedMethodToNewMethodMapForClassWithoutDeprecatedMethods() {
        //Class does not have new
        Map<Method, Method> result = ElementMapper.getDeprecatedMethodToNewMethodMapForClass("MyPipe");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
