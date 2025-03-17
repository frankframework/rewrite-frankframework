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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TagHandlerTest {
    @Test
    void testHasMatchingAttribute() {
        Xml.Tag tag = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute = Mockito.mock(Xml.Attribute.class);
        when(attribute.getKeyAsString()).thenReturn("id");
        when(attribute.getValueAsString()).thenReturn("123");
        when(tag.getAttributes()).thenReturn(List.of(attribute));

        assertTrue(TagHandler.hasMatchingAttribute(tag, "id", "123"));
        assertFalse(TagHandler.hasMatchingAttribute(tag, "name", "abc"));
    }

    @Test
    void testGetAttributeFromTagByKey() {
        Xml.Tag tag = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute = Mockito.mock(Xml.Attribute.class);
        when(attribute.getKeyAsString()).thenReturn("name");
        when(tag.getAttributes()).thenReturn(List.of(attribute));

        Optional<Xml.Attribute> result = TagHandler.getAttributeFromTagByKey(tag, "name");
        assertTrue(result.isPresent());
        assertEquals("name", result.get().getKeyAsString());
    }

    @Test
    void testGetAttributeValueFromTagByKey() {
        Xml.Tag tag = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute = Mockito.mock(Xml.Attribute.class);
        when(attribute.getKeyAsString()).thenReturn("class");
        when(attribute.getValueAsString()).thenReturn("header");
        when(tag.getAttributes()).thenReturn(List.of(attribute));

        Optional<String> result = TagHandler.getAttributeValueFromTagByKey(tag, "class");
        assertTrue(result.isPresent());
        assertEquals("header", result.get());
    }

    @Test
    void testHaveMatchingAttribute() {
        Xml.Tag tag1 = Mockito.mock(Xml.Tag.class);
        Xml.Tag tag2 = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute = Mockito.mock(Xml.Attribute.class);
        when(attribute.getKeyAsString()).thenReturn("id");
        when(attribute.getValueAsString()).thenReturn("123");

        when(tag1.getAttributes()).thenReturn(List.of(attribute));
        when(tag2.getAttributes()).thenReturn(List.of(attribute));

        assertTrue(TagHandler.haveMatchingAttribute(tag1, tag2, "id"));
        assertTrue(TagHandler.haveMatchingAttribute(tag2, tag2, "id"));
    }

    @Test
    void testHasAnyAttributeWithKey() {
        Xml.Tag tag = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute = Mockito.mock(Xml.Attribute.class);
        when(attribute.getKeyAsString()).thenReturn("name");
        when(tag.getAttributes()).thenReturn(List.of(attribute));

        assertTrue(TagHandler.hasAnyAttributeWithKey(tag, "name"));
        assertFalse(TagHandler.hasAnyAttributeWithKey(tag, "id"));
    }
    @Test
    void getTagByAttributeValue_tagFound() {
        Xml.Tag tag1 = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute1 = Mockito.mock(Xml.Attribute.class);
        when(attribute1.getKeyAsString()).thenReturn("id");
        when(attribute1.getValueAsString()).thenReturn("123");
        when(tag1.getAttributes()).thenReturn(List.of(attribute1));

        Xml.Tag tag2 = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute2 = Mockito.mock(Xml.Attribute.class);
        when(attribute2.getKeyAsString()).thenReturn("id");
        when(attribute2.getValueAsString()).thenReturn("456");
        when(tag2.getAttributes()).thenReturn(List.of(attribute2));


        List<Xml.Tag> tags = new ArrayList<>();
        tags.add(tag1);
        tags.add(tag2);

        Optional<Xml.Tag> result = TagHandler.getTagByAttributeValue(tags, "id", "456");
        assertTrue(result.isPresent());
        assertEquals("456", result.get().getAttributes().get(0).getValueAsString());
    }

    @Test
    void getTagByAttributeValue_tagNotFound() {
        Xml.Tag tag1 = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute1 = Mockito.mock(Xml.Attribute.class);
        when(attribute1.getKeyAsString()).thenReturn("id");
        when(attribute1.getValueAsString()).thenReturn("123");
        when(tag1.getAttributes()).thenReturn(List.of(attribute1));

        Optional<Xml.Tag> result = TagHandler.getTagByAttributeValue(List.of(tag1), "id", "456");
        assertFalse(result.isPresent());
    }

    @Test
    void getTagByAttributeValue_emptyList() {
        Optional<Xml.Tag> result = TagHandler.getTagByAttributeValue(new ArrayList<>(), "id", "123");
        assertFalse(result.isPresent());
    }

    @Test
    void getTagByAttributeValue_nullList() {
        Optional<Xml.Tag> result = TagHandler.getTagByAttributeValue(null, "id", "123");
        assertFalse(result.isPresent());
    }

    @Test
    void getTagWithNewAttributeValueByAttributeName_attributeFound() {
        Xml.Tag tag1 = Mockito.mock(Xml.Tag.class);

        Xml.Attribute attribute1 = Mockito.mock(Xml.Attribute.class);
        when(attribute1.getKeyAsString()).thenReturn("id");
        when(attribute1.getValueAsString()).thenReturn("123");

        Xml.Attribute.Value value1 = Mockito.mock(Xml.Attribute.Value.class);
        when(value1.getValue()).thenReturn("123");
        when(attribute1.getValue()).thenReturn(value1);

        Xml.Attribute attribute2 = Mockito.mock(Xml.Attribute.class);
        when(attribute2.getKeyAsString()).thenReturn("name");
        when(attribute2.getValueAsString()).thenReturn("oldName");

        Xml.Attribute newAttribute2 = Mockito.mock(Xml.Attribute.class);
        when(newAttribute2.getKeyAsString()).thenReturn("name");
        when(newAttribute2.getValueAsString()).thenReturn("newName");

        Xml.Attribute.Value value2 = Mockito.mock(Xml.Attribute.Value.class);
        when(value2.getValue()).thenReturn("oldName");
        when(attribute2.getValue()).thenReturn(value2);

        when(tag1.getAttributes()).thenReturn(List.of(attribute1, attribute2));

        Xml.Tag newTag = Mockito.mock(Xml.Tag.class);
        when(newTag.getAttributes()).thenReturn(List.of(attribute1, newAttribute2));
        when(tag1.withAttributes(Mockito.anyList())).thenReturn(newTag);

        Xml.Tag result = TagHandler.getTagWithNewAttributeValueByAttributeName(tag1, "newName", "name");

        assertNotNull(result);
        assertEquals("newName", result.getAttributes().stream()
          .filter(attribute -> attribute.getKeyAsString().equalsIgnoreCase("name"))
          .findFirst().get().getValueAsString());

        assertEquals("123", result.getAttributes().stream()
          .filter(attribute -> attribute.getKeyAsString().equalsIgnoreCase("id"))
          .findFirst().get().getValueAsString());
    }

    @Test
    void getTagWithNewAttributeValueByAttributeName_attributeNotFound() {
        Xml.Tag tag1 = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute1 = Mockito.mock(Xml.Attribute.class);
        when(attribute1.getKeyAsString()).thenReturn("id");
        when(attribute1.getValueAsString()).thenReturn("123");
        when(tag1.getAttributes()).thenReturn(List.of(attribute1));

        Xml.Tag result = TagHandler.getTagWithNewAttributeValueByAttributeName(tag1, "newName", "name");

        assertNull(result);
    }

    @Test
    void getTagWithNewAttributeValueByAttributeName_caseInsensitive() {
        Xml.Tag tag1 = Mockito.mock(Xml.Tag.class);
        Xml.Attribute attribute1 = Mockito.mock(Xml.Attribute.class);
        when(attribute1.getKeyAsString()).thenReturn("ID");
        when(attribute1.getValueAsString()).thenReturn("123");
        Xml.Attribute.Value value1 = Mockito.mock(Xml.Attribute.Value.class);
        when(value1.getValue()).thenReturn("123");
        when(attribute1.getValue()).thenReturn(value1);

        when(tag1.getAttributes()).thenReturn(List.of(attribute1));

        Xml.Attribute newAttribute1 = Mockito.mock(Xml.Attribute.class);
        when(newAttribute1.getKeyAsString()).thenReturn("id");
        when(newAttribute1.getValueAsString()).thenReturn("456");

        Xml.Tag newTag = Mockito.mock(Xml.Tag.class);
        when(newTag.getAttributes()).thenReturn(List.of(newAttribute1));
        when(tag1.withAttributes(Mockito.anyList())).thenReturn(newTag);

        Xml.Tag result = TagHandler.getTagWithNewAttributeValueByAttributeName(tag1, "456", "id");

        assertNotNull(result);
        assertEquals("456", result.getAttributes().stream()
          .filter(attribute -> attribute.getKeyAsString().equalsIgnoreCase("id"))
          .findFirst().get().getValueAsString());
    }

    @Test
    void testHasAnyAttributeWithValue_String() {
        Xml.Tag tag = mock(Xml.Tag.class);
        Xml.Attribute attribute = mock(Xml.Attribute.class);
        when(attribute.getValueAsString()).thenReturn("testValue");
        when(tag.getAttributes()).thenReturn(List.of(attribute));

        assertTrue(TagHandler.hasAnyAttributeWithValue(tag, "testValue"));
        assertFalse(TagHandler.hasAnyAttributeWithValue(tag, "nonMatchingValue"));
    }

    @Test
    void testHasAnyAttributeWithValue_Optional() {
        Xml.Tag tag = mock(Xml.Tag.class);
        Xml.Attribute attribute = mock(Xml.Attribute.class);
        when(attribute.getValueAsString()).thenReturn("testValue");
        when(tag.getAttributes()).thenReturn(List.of(attribute));

        assertTrue(TagHandler.hasAnyAttributeWithValue(tag, Optional.of("testValue")));
        assertFalse(TagHandler.hasAnyAttributeWithValue(tag, Optional.of("nonMatchingValue")));
        assertFalse(TagHandler.hasAnyAttributeWithValue(tag, Optional.empty()));
    }

    @Test
    void testGetFilteredAttributeOptional() {
        Xml.Tag tag = mock(Xml.Tag.class);
        Xml.Attribute attribute1 = mock(Xml.Attribute.class);
        Xml.Attribute attribute2 = mock(Xml.Attribute.class);
        when(tag.getAttributes()).thenReturn(List.of(attribute1, attribute2));

        assertEquals(Optional.of(attribute2), TagHandler.getFilteredAttributeOptional(tag, attr -> attr == attribute2));
        assertEquals(Optional.empty(), TagHandler.getFilteredAttributeOptional(tag, attr -> false));
    }

    @Test
    void testHasAnyFilteredAttribute() {
        Xml.Tag tag = mock(Xml.Tag.class);
        Xml.Attribute attribute = mock(Xml.Attribute.class);
        when(tag.getAttributes()).thenReturn(List.of(attribute));

        assertTrue(TagHandler.hasAnyFilteredAttribute(tag, attr -> true));
        assertFalse(TagHandler.hasAnyFilteredAttribute(tag, attr -> false));
    }

    @Test
    void testGetAttributeFromTagByValue() {
        Xml.Tag tag = mock(Xml.Tag.class);
        Xml.Attribute attribute1 = mock(Xml.Attribute.class);
        Xml.Attribute attribute2 = mock(Xml.Attribute.class);
        when(attribute1.getValueAsString()).thenReturn("value1");
        when(attribute2.getValueAsString()).thenReturn("value2");
        when(tag.getAttributes()).thenReturn(List.of(attribute1, attribute2));

        assertEquals(Optional.of(attribute2), TagHandler.getAttributeFromTagByValue(tag, "value2"));
        assertEquals(Optional.empty(), TagHandler.getAttributeFromTagByValue(tag, "nonMatchingValue"));
    }

    @Test
    void testGetContentWithNonTagReturnsEmptyList() {
        Xml.Comment comment = mock(Xml.Comment.class);
        assertTrue(TagHandler.getContent(comment).isEmpty());
    }

    @Test
    void testGetTagWithUpdatedAttributeValue() {
        // Arrange: Create an XML tag with an attribute
        Xml.Tag originalTag = Xml.Tag.build("<myTag key=\"oldValue\" anotherKey=\"value\"/>");

        // Get the specific attribute that needs to be updated
        Optional<Xml.Attribute> optionalAttribute = originalTag.getAttributes().stream()
          .filter(attr -> attr.getKeyAsString().equals("key"))
          .findFirst();

        assertTrue(optionalAttribute.isPresent()); // Ensure the attribute exists
        Xml.Attribute attribute = optionalAttribute.get();

        // Act: Update the attribute's value
        Xml.Tag updatedTag = TagHandler.getTagWithUpdatedAttributeValue(originalTag, attribute, "newValue");

        // Assert: Verify the attribute is updated while others remain unchanged
        assertNotNull(updatedTag);
        assertTrue(TagHandler.hasAnyAttributeWithKeyValue(updatedTag, "key", "newValue"));
        assertTrue(TagHandler.hasAnyAttributeWithKeyValue(updatedTag, "anotherKey", "value"));
    }

}