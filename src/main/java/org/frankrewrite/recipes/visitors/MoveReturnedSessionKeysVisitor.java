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
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class MoveReturnedSessionKeysVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        if ("Receiver".equalsIgnoreCase(tag.getName())) {
            Optional<Xml.Attribute> attributeOptional = TagHandler.getAttributeFromTagByKey(tag, "returnedSessionKeys");
            if (attributeOptional.isPresent()) {
                Xml.Attribute returnedSessionKeyAttr = attributeOptional.get();

                List<Xml.Attribute> filteredAttributes = tag.getAttributes().stream()
                        .filter(attribute -> !attribute.equals(returnedSessionKeyAttr)).toList();

                Optional<Xml.Tag> listener = tag.getChildren().stream()
                        .filter(child -> "JavaListener".equalsIgnoreCase(child.getName()))
                        .findFirst();

                if (listener.isPresent()) {
                    Xml.Tag listenerTag = listener.get();

                    // Update listener attributes
                    List<Xml.Attribute> updatedListenerAttributes = new ArrayList<>(listenerTag.getAttributes());
                    updatedListenerAttributes.add(returnedSessionKeyAttr);
                    Xml.Tag updatedListenerTag = listenerTag.withAttributes(updatedListenerAttributes);

                    // Update content
                    List<Content> filteredContent = new ArrayList<>(getContent(tag).stream()
                            .filter(Objects::nonNull)
                            .filter(child -> !child.equals(listenerTag))
                            .toList());
                    filteredContent.add(updatedListenerTag);

                    return tag.withAttributes(filteredAttributes).withContent(filteredContent);
                }
            }
        }
        return super.visitTag(tag, executionContext);
    }
}
