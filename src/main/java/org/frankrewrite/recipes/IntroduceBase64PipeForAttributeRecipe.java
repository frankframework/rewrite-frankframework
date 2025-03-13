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
package org.frankrewrite.recipes;

import org.frankrewrite.recipes.util.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class IntroduceBase64PipeForAttributeRecipe extends Recipe {
    private static int amountRefactored = 1;

    @Override
    public String getDisplayName() {
        return "Introduce Base64Pipe and update path strings";
    }

    @Override
    public String getDescription() {
        return "Replaces base64 attributes in LocalFileSystemPipe with an Base64Pipe and updates path references.";
    }

    @Override
    public XmlIsoVisitor<ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>() {
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
                if (tag.getName().equalsIgnoreCase("pipeline")) {
                    AtomicBoolean changed = new AtomicBoolean(false);
                    List<Content> updatedChildren = tag.getContent().stream().map(content -> {
                        if (content instanceof Xml.Tag child) {
                            if (child.getName().equals("LocalFileSystemPipe") && TagHandler.hasAnyAttributeWithKey(child, "base64")
                                    && TagHandler.hasAnyAttributeWithKey(child, "storeResultInSessionKey")
                                    &&TagHandler.hasAnyAttributeWithKey(child, "name")) {
                                String storeResultInSessionKeyValue = TagHandler.getAttributeValueFromTagByKey(child, "storeResultInSessionKey").orElse("");
                                String base64Value = TagHandler.getAttributeValueFromTagByKey(child, "base64").orElse("");
                                String pipeName = TagHandler.getAttributeValueFromTagByKey(child, "name").get()+(base64Value.equals("ENCODE")?"Encoder":"Decoder");

                                child = TagHandler.getTagWithoutAttribute(child,"base64");
                                List<Content> childContent = child.getContent().stream()
                                        .map(grandchild -> {
                                            if (grandchild instanceof Xml.Tag && ((Xml.Tag) grandchild).getName().equalsIgnoreCase("forward")) {
                                                return ((Xml.Tag) grandchild).withAttributes(
                                                        ((Xml.Tag) grandchild).getAttributes().stream()
                                                                .map(attr -> (attr.getKeyAsString().equals("path")&&attr.getValueAsString().equals("success")) ? attr.withValue(attr.getValue().withValue(pipeName)) : attr)
                                                                .collect(Collectors.toList())
                                                );
                                            }
                                            return grandchild;
                                        })
                                        .collect(Collectors.toList());

                                // Get path attribute value with corresponding forward tag in child
                                String exceptionPathValue = getForwardPathValue(child, "exception");
                                String successPathValue = getForwardPathValue(child, "success");

                                if (!successPathValue.isEmpty()) {
                                    childContent.stream()
                                            .filter(grandChild -> grandChild instanceof Xml.Tag t
                                                    && TagHandler.hasAnyAttributeWithKeyValue(t, "name", "success"))
                                            .findFirst()
                                            .map(Xml.Tag.class::cast)
                                            .ifPresent(successForwardTag -> {
                                                Xml.Tag updatedSuccessTag = TagHandler.getTagWithNewAttributeValueByAttributeName(successForwardTag, pipeName, "path");
                                                if (updatedSuccessTag != null) {
                                                    childContent.set(childContent.indexOf(successForwardTag), updatedSuccessTag);
                                                }
                                            });
                                }

                                // Update forward path
                                child = child.withContent(childContent);

                                // Create new EchoPipe element
                                Xml.Tag echoPipe = Xml.Tag.build(
                            "<Base64Pipe name=\"" + pipeName + "\" direction=\"" + base64Value + "\" storeResultInSessionKey=\"" + storeResultInSessionKeyValue + "\">" +
                                    (exceptionPathValue.isEmpty() ?"":"\n       <forward name=\"exception\" path=\""+exceptionPathValue+"\"/>") +
                                    (successPathValue.isEmpty() ?"": "\n       <forward name=\"success\" path=\""+successPathValue+"\"/>") +
                                    "\n   </Base64Pipe>"
                                );
                                amountRefactored++;
                                changed.set(true);

                                return List.of(child, echoPipe);
                            }
                        }
                        return new ArrayList<>(List.of(content));
                    }).flatMap(List::stream).collect(Collectors.toList());

                    if (changed.get())
                        return tag.withContent(updatedChildren);
                }
                return super.visitTag(tag, ctx);
            }

            private static @NotNull String getForwardPathValue(Xml.Tag child, String nameValue) {
                return child.getContent().stream()
                        .filter(grandchild -> grandchild instanceof Xml.Tag t
                                && t.getName().equalsIgnoreCase("forward")
                                && TagHandler.getAttributeValueFromTagByKey(t, "name").map(nameValue::equals).orElse(false)) // Avoids unnecessary orElse("")
                        .map(t -> TagHandler.getAttributeValueFromTagByKey((Xml.Tag) t, "path"))
                        .findFirst()
                        .flatMap(path -> path) // Removes double Optional
                        .orElse(""); // Return empty string if not found
            }
        };
    }
}
