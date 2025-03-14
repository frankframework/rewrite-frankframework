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
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class IntroduceBase64PipeForAttributeVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
        if (!tag.getName().equalsIgnoreCase("pipeline")) {
            return super.visitTag(tag, ctx);
        }

        //Track if tag changed to prevent unnecessary refactors
        AtomicBoolean changed = new AtomicBoolean(false);

        //Update the children for tag
        List<Content> updatedChildren = getUpdatedChildren(tag, changed);

        if (changed.get())
            return tag.withContent(updatedChildren);

        return super.visitTag(tag, ctx);
    }

    private @NotNull List<Content> getUpdatedChildren(Xml.Tag tag, AtomicBoolean changed) {
        return getContent(tag).stream().map(content -> {
            if (content instanceof Xml.Tag child && shouldIntroduceBase64PipeForChildTag(child)) {
                String storeResultInSessionKeyValue = TagHandler.getAttributeValueFromTagByKey(child, "storeResultInSessionKey").orElse("");
                String base64Value = TagHandler.getAttributeValueFromTagByKey(child, "base64").orElse("");
                String pipeName = TagHandler.getAttributeValueFromTagByKey(child, "name").get()+(base64Value.equals("ENCODE")?"Encoder":"Decoder");

                child = TagHandler.getTagWithoutAttribute(child,"base64"); //Remove attribute
                List<Content> childContent = getContentWithUpdatedForwardToBase64Pipe(child, pipeName); //Update the forward name to the base64pipe to be introduced

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
                Xml.Tag base64Pipe = createBase64Pipe(pipeName, base64Value, storeResultInSessionKeyValue, exceptionPathValue, successPathValue);
                changed.set(true);

                return List.of(child, base64Pipe);

            }
            return new ArrayList<>(List.of(content));
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    private Xml.Tag createBase64Pipe(String pipeName, String base64Value, String storeResultInSessionKeyValue, String exceptionPathValue, String successPathValue) {
        return Xml.Tag.build(
                "<Base64Pipe name=\"" + pipeName + "\" direction=\"" + base64Value + "\" storeResultInSessionKey=\"" + storeResultInSessionKeyValue + "\">" +
                        (exceptionPathValue.isEmpty() ?"":"\n       <forward name=\"exception\" path=\""+exceptionPathValue+"\"/>") +
                        (successPathValue.isEmpty() ?"": "\n       <forward name=\"success\" path=\""+successPathValue+"\"/>") +
                        "\n   </Base64Pipe>"
        );
    }

    private static @NotNull List<Content> getContentWithUpdatedForwardToBase64Pipe(Xml.Tag child, String pipeName) {
        return getContent(child).stream()
                .map(grandchild -> {
                    if (grandchild instanceof Xml.Tag t && t.getName().equalsIgnoreCase("forward")) {
                        return t.withAttributes(
                                t.getAttributes().stream()
                                        .map(attr -> (attr.getKeyAsString().equals("path") && attr.getValueAsString().equals("success")) ? attr.withValue(attr.getValue().withValue(pipeName)) : attr)
                                        .toList()
                        );
                    }
                    return grandchild;
                })
                .collect(Collectors.toList());
    }

    private boolean shouldIntroduceBase64PipeForChildTag(Xml.Tag childTag) {
        return childTag.getName().equals("LocalFileSystemPipe")
                && TagHandler.hasAnyAttributeWithKey(childTag, "base64")
                && TagHandler.hasAnyAttributeWithKey(childTag, "storeResultInSessionKey")
                && TagHandler.hasAnyAttributeWithKey(childTag, "name");
    }

    private static @NotNull String getForwardPathValue(Xml.Tag child, String nameValue) {
        return getContent(child).stream()
                .filter(grandchild -> grandchild instanceof Xml.Tag t
                        && t.getName().equalsIgnoreCase("forward")
                        && TagHandler.getAttributeValueFromTagByKey(t, "name").map(nameValue::equals).orElse(false)) // Avoids unnecessary orElse("")
                .map(t -> TagHandler.getAttributeValueFromTagByKey((Xml.Tag) t, "path"))
                .findFirst()
                .flatMap(path -> path) // Removes double Optional
                .orElse(""); // Return empty string if not found
    }
}
