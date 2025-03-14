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
package org.frankrewrite.recipes.visitors;

import org.frankrewrite.recipes.util.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;

public class RemoveAttributeVisitor extends XmlIsoVisitor<ExecutionContext> {
    final String attributeName;
    final String tagName;
    final String value;
    final String tagType;

    public RemoveAttributeVisitor(String attributeName, String tagName, String value, String tagType) {
        this.attributeName = attributeName;
        this.tagName = tagName;
        this.value = value;
        this.tagType = tagType;
    }

    @Override
    public Xml.@NotNull Tag visitTag(Xml.@NotNull Tag tag, @NotNull ExecutionContext ctx) {
        //Check if tagType is present and valid
        if (tagType != null && !tag.getName().contains(tagType)) {
            return super.visitTag(tag, ctx);
        }
        //Check if tagName is present and valid
        if (tagName != null && !tag.getName().equalsIgnoreCase(tagName)) {
            return super.visitTag(tag, ctx);
        }
        //Check if attributeName exists in tag
        if (value != null && tag.getAttributes().stream()
                .noneMatch(attr -> attr.getKeyAsString().equalsIgnoreCase(attributeName) &&
                        attr.getValueAsString().equalsIgnoreCase(value))) {
            return super.visitTag(tag, ctx);
        }
        //Lookup attribute to remove
        if (TagHandler.hasAnyAttributeWithKey(tag, attributeName)){
            List<Xml.Attribute> attributes = tag.getAttributes().stream()
                    .filter(attr -> !attr.getKeyAsString().equalsIgnoreCase(attributeName))
                    .toList();

            //Return tag without filtered attribute
            return tag.withAttributes(attributes);
        }
        //Return the original tag if no changes were made
        return super.visitTag(tag, ctx);
    }
}
