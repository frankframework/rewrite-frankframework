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
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagHandler {
    private TagHandler() {}
    public static @NotNull Optional<Xml.Attribute> getAttributeFromTagByKeyAndValue(Xml.Tag tag, String attributeKey, String attributeValue) {
        return tag.getAttributes().stream()
                .filter(attribute -> attribute.getKeyAsString().equalsIgnoreCase(attributeKey)&&attribute.getValueAsString().equalsIgnoreCase(attributeValue)).findFirst();
    }
    public static @NotNull Optional<Xml.Attribute> getAttributeFromTagByKey(Xml.Tag tag, String attributeKey) {
        return tag.getAttributes().stream()
                .filter(attribute -> attribute.getKeyAsString().equalsIgnoreCase(attributeKey)).findFirst();
    }

    public static @NotNull Optional<String> getAttributeValueFromTagByKey(Xml.Tag tag, String attributeKey) {
        return getAttributeFromTagByKey(tag, attributeKey).flatMap(attr->Optional.ofNullable(attr.getValueAsString()));
    }
    public static @NotNull Optional<Xml.Attribute> getAttributeFromTagByValue(Xml.Tag tag, String attributeValue) {
        return tag.getAttributes().stream()
                .filter(attribute -> attribute.getValueAsString().equalsIgnoreCase(attributeValue)).findFirst();
    }
    //Checks if tag1 has an attribute that has the attribute name (key) with the expected value (expectedValue)
    public static boolean hasMatchingAttribute(Xml.Tag tag, String key, String expectedValue) {
        return getAttributeValueFromTagByKey(tag, key)
                .map(value -> value.equalsIgnoreCase(expectedValue))
                .orElse(false);
    }

    //Checks if tag1 and tag2 have a matching attribute value by key (attribute name)
    public static boolean haveMatchingAttribute(Xml.Tag tag1, Xml.Tag tag2, String key) {
        return getAttributeValueFromTagByKey(tag1, key)
                .flatMap(value1 -> getAttributeValueFromTagByKey(tag2, key)
                        .map(value1::equalsIgnoreCase))
                .orElse(false);
    }

    public static @NotNull Optional<Xml.Attribute> getFilteredAttributeOptional(Xml.Tag tag, Function<Xml.Attribute, Boolean> funFilter) {
        return tag.getAttributes().stream().filter(funFilter::apply).findFirst();
    }
    public static @NotNull boolean hasAnyFilteredAttribute(Xml.Tag tag, Function<Xml.Attribute, Boolean> funFilter) {
        return tag.getAttributes().stream().anyMatch(funFilter::apply);
    }
    public static @NotNull boolean hasAnyAttributeWithKey(Xml.Tag tag, String attributeValue) {
        return tag.getAttributes().stream().anyMatch(attribute -> attribute.getKeyAsString().equalsIgnoreCase(attributeValue));
    }
    public static @NotNull boolean hasAnyAttributeWithValue(Xml.Tag tag, String attributeValue) {
        return tag.getAttributes().stream().anyMatch(attribute -> attribute.getValueAsString().equalsIgnoreCase(attributeValue));
    }
    public static @NotNull boolean hasAnyAttributeWithKeyValue(Xml.Tag tag, String attributeKey, String attributeValue) {
        return tag.getAttributes().stream().anyMatch(attribute -> attribute.getValueAsString().equalsIgnoreCase(attributeValue)&&attribute.getKeyAsString().equalsIgnoreCase(attributeKey));
    }
    public static @NotNull boolean hasAnyAttributeWithValue(Xml.Tag tag, Optional<String> attributeValue) {
        return attributeValue.filter(s -> tag.getAttributes().stream().anyMatch(attribute -> attribute.getValueAsString().equalsIgnoreCase(s))).isPresent();
    }

    public static boolean isTagAttributeValueEqual(Xml.Tag tag, String attributeKey, String expectedValue){
        return TagHandler.getAttributeValueFromTagByKey(tag, attributeKey)
                .map(value -> value.equalsIgnoreCase(expectedValue))
                .orElse(false);
    }
    // Helper: Extract a tag list by matching name and value
    public static Optional<Xml.Tag> getTagByAttributeValue(List<Xml.Tag> tags, String key, String value) {
        return (tags == null ? Stream.<Xml.Tag>empty() : tags.stream())
                .filter(tag -> TagHandler.isTagAttributeValueEqual(tag, key, value))
                .findFirst();
    }

    public static Xml.Tag getTagWithNewAttributeValueByAttributeName(Xml.Tag tag, String newValue, String attributeName) {
        List<Xml.Attribute> attributes = tag.getAttributes();
        Optional<Xml.Attribute> attributeOptional = attributes.stream().filter(attribute -> attribute.getKeyAsString().equalsIgnoreCase(attributeName)).findFirst();
        if (attributeOptional.isPresent()) {
            Xml.Attribute foundAttribute = attributeOptional.get();
            foundAttribute = foundAttribute.withValue(foundAttribute.getValue().withValue(newValue));
            attributes = attributes.stream().filter(attribute-> !attribute.getKeyAsString().equalsIgnoreCase(attributeName)).collect(Collectors.toList());
            attributes.add(foundAttribute);

            // Return a new Xml.Tag with the updated attributes
            return tag.withAttributes(attributes);
        }
        return null;
    }

    public static Xml.@NotNull Tag getTagWithoutAttribute(Xml.Tag updatedTag, String attributeName) {
        updatedTag = updatedTag.withAttributes(
                updatedTag.getAttributes().stream()
                        .filter(attr -> !attr.getKey().getName().equals(attributeName))
                        .toList()
        );
        return updatedTag;
    }
    public static Xml.Tag getTagWithUpdatedAttributeValue(Xml.Tag updatedTag, Xml.Attribute attribute, String attributeValue) {
        List<Xml.Attribute> attributeList = new ArrayList<>(updatedTag.getAttributes().stream().filter(attr -> attr!=attribute).toList());
        attributeList.add(attribute.withValue(attribute.getValue().withValue(attributeValue)));
        return updatedTag.withAttributes(attributeList);
    }

    public static List<Content> getContent(Content tag) {//Get content from tags if they are a Tag instance and return empty list if tag is self-closing
        return tag instanceof Xml.Tag xmlTag && xmlTag.getContent() != null
                ? new ArrayList<>(xmlTag.getContent())
                : new ArrayList<>();
    }
}
