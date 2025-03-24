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

import java.util.*;
import java.util.stream.Collectors;

public class RemoveEtagHandlerPipeVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        if (tag.getName().equalsIgnoreCase("pipeline")
            &&tag.getContent()!=null
        ){
            List<Xml.Tag> etagHandlers = tag.getContent().stream()
                    .filter(c -> c instanceof Xml.Tag t
                    &&t.getName().equals("EtagHandlerPipe")) // Get all the EtagHandlers in the pipeline
                    .map(Xml.Tag.class::cast).toList();

            List<Content> resultContent = new ArrayList<>(tag.getContent());
            Map<Xml.Tag, String> fromToMap = collectPipeForwardPathNameMap(etagHandlers);// return map of EtagHandler tags and forward path values if name is success

            for (var entry : fromToMap.entrySet()) {
                resultContent.remove(entry.getKey()); // Remove etagHandler from pipeline
                resultContent = resultContent.stream()
                        // Update the path of the corresponding forwards referring to the found EtagHandlers or check their children recursively until there's no more children
                        .map(content -> updateForwardTagsRecursively(content, entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());

            }
            if (!fromToMap.isEmpty()) {
                Optional<Xml.Tag> apiListener = resultContent.stream().filter(c -> c instanceof Xml.Tag t &&
                        t.getName().equals("ApiListener")).findFirst().map(Xml.Tag.class::cast);
                if (apiListener.isPresent()) {//update the etag and ApiListener attribute
                    Optional<Xml.Attribute> attribute = TagHandler.getAttributeFromTagByKey(apiListener.get(), "updateEtag");
                    List<Xml.Attribute> attributes = apiListener.get().getAttributes();
                    attribute.ifPresent(attributes::remove);
                    //language=xml
                    attributes.add(Xml.Tag.build("<x updateEtag='true'> </x>").getAttributes().get(0));
                    resultContent.set(resultContent.indexOf(apiListener.get()), apiListener.get().withAttributes(attributes));
                    return tag.withContent(resultContent);
                }
            }
        }
        return super.visitTag(tag, executionContext);
    }
    private Content updateForwardTagsRecursively(Content content, Xml.Tag etagHandler, String newPath) {
        if (content instanceof Xml.Tag tag) {
            // If this is a <forward> tag, check if it needs to be updated
            if (tag.getName().equalsIgnoreCase("forward")) {
                String forwardPath = TagHandler.getAttributeValueFromTagByKey(tag, "path").orElse("nonExistingForwardPath");
                String etagHandlerName = TagHandler.getAttributeValueFromTagByKey(etagHandler, "name").orElse("nonExistingEtagHandlerName");

                // If it's a forward that needs an update, modify its path
                if (etagHandlerName.equals(forwardPath)) {
                    return TagHandler.getTagWithNewAttributeValueByAttributeName(tag, newPath, "path");
                }
            }

            // If this is not a <forward> tag, check its children recursively
            if (tag.getContent()!=null) {
                List<Content> updatedChildren = tag.getContent().stream()
                        .map(child -> updateForwardTagsRecursively(child, etagHandler, newPath))
                        .toList();
                return tag.withContent(updatedChildren);
            }
        }
        return content;
    }

    private Map<Xml.Tag, String> collectPipeForwardPathNameMap(List<Xml.Tag> etagHandlers) {
        return etagHandlers.stream()
                .collect(Collectors.toMap(
                        etagHandler -> etagHandler, // Key: the EtagHandlerPipe tag
                        etagHandler -> etagHandler.getContent().stream()
                                .filter(pipe -> pipe instanceof Xml.Tag t
                                        && t.getName().equalsIgnoreCase("forward")
                                        && TagHandler.getAttributeValueFromTagByKey(t, "name").orElse("notequals").equals("success")
                                )
                                .findFirst()
                                .map(Xml.Tag.class::cast)
                                .flatMap(forwardTag -> TagHandler.getAttributeValueFromTagByKey(forwardTag, "path")) // return map of EtagHandler tags and forward path values if name is success
                                .orElse("NoMatchingForwardPath")
                ));
    }
}
