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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openrewrite.xml.tree.Xml;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TagUpdaterTest {

    @Mock
    private Xml.Tag mockTag;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUpdatedTagWithNewName_WhenTagNameIsDeprecated_ShouldReturnUpdatedTag() {
        Map<Class<?>, Class<?>> mockMap = Map.of(OldClass.class, NewClass.class);
        when(mockTag.getName()).thenReturn("OldClass");
        when(mockTag.withName("NewClass")).thenReturn(mockTag);

        try (var mocked = mockStatic(ElementMapper.class)) {
            mocked.when(ElementMapper::getDeprecatedClassToNewClassMapInPackage).thenReturn(mockMap);
            Xml.Tag result = TagUpdater.getUpdatedTagWithNewName(mockTag);
            assertNotNull(result);
            verify(mockTag).withName("NewClass");
        }
    }

    @Test
    void testGetUpdatedTagWithNewName_WhenTagNameIsNotDeprecated_ShouldReturnSameTag() {
        when(mockTag.getName()).thenReturn("UnchangedClass");
        Xml.Tag result = TagUpdater.getUpdatedTagWithNewName(mockTag);
        assertEquals(mockTag, result);
    }

    @Test
    void testGetTagWithNewAttributes_WhenAttributesAreDeprecated_ShouldReturnUpdatedTag() throws NoSuchMethodException {
        Method oldMethod = OldClass.class.getMethod("setOldAttribute", String.class);
        Method newMethod = NewClass.class.getMethod("setNewAttribute", String.class);

        Map<Method, Method> mockAttributeMap = Map.of(oldMethod, newMethod);
        when(mockTag.getName()).thenReturn("NewClass");
        when(mockTag.getAttributes()).thenReturn(Collections.emptyList());
        when(mockTag.withAttributes(anyList())).thenReturn(mockTag);

        try (var mocked = mockStatic(ElementMapper.class)) {
            mocked.when(() -> ElementMapper.getDeprecatedMethodToNewMethodMapForClass("NewClass"))
              .thenReturn(mockAttributeMap);
            Xml.Tag result = TagUpdater.getTagWithNewAttributes(mockTag);
            assertNotNull(result);
        }
    }

    static class OldClass {
        public void setOldAttribute(String value) {}
    }

    static class NewClass {
        public void setNewAttribute(String value) {}
    }
}
