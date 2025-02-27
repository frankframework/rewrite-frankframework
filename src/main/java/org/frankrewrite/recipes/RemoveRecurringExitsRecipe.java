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

import org.frankrewrite.recipes.scanners.ExitScanner;
import org.frankrewrite.recipes.util.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.*;

public class RemoveRecurringExitsRecipe extends ScanningRecipe<ExitScanner> {

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Remove recurring exits";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Removes recurring exit tags with the same code value.";
    }

    @Override
    public @NotNull ExitScanner getInitialValue(ExecutionContext ctx) {
        return new ExitScanner();
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getScanner(ExitScanner acc) {
        return new XmlIsoVisitor<>() {
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                Xml.Document document = getCursor().firstEnclosing(Xml.Document.class);
                if (isAdapterTag(document, tag)) {
                    tag.getChildren().stream()
                            .filter(pipeline -> pipeline.getName().equalsIgnoreCase("pipeline"))
                            .flatMap(pipelineTag -> pipelineTag.getChildren().stream())
                            .filter(exits -> exits.getName().equalsIgnoreCase("exits"))
                            .flatMap(exitsTag -> exitsTag.getChildren().stream())
                            .filter(exitTag -> exitTag.getName().equalsIgnoreCase("exit"))
                            .forEach(exitTag -> acc.addTagToTracking(document, tag, exitTag));
                }
                return super.visitTag(tag, executionContext);
            }
        };
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor(ExitScanner acc) {
        return new XmlIsoVisitor<>() {
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                Xml.Document document = getCursor().firstEnclosing(Xml.Document.class);
                if (!isAdapterTag(document, tag))
                    return super.visitTag(tag, executionContext);

                Map<Xml.Tag, List<Xml.Tag>> recurringExits = acc.foundRecurringExitsPerAdapterPerDocument
                        .getOrDefault(document, Collections.emptyMap());
                List<Xml.Tag> exitsForTag = recurringExits.get(tag);

                int exitCount = countExits(tag);

                if (exitsForTag != null && !exitsForTag.isEmpty()) {
                    List<? extends Content> updatedContent = updateRecurringExits(tag, exitsForTag, document);
                    if (exitCount != countExits(tag.withContent(updatedContent)))
                        return tag.withContent(updatedContent);
                }

                return super.visitTag(tag, executionContext);
            }

            private static int countExits(Xml.Tag tag) {
                return (int) tag.getChildren().stream()
                        .filter(pipeline -> pipeline.getName().equalsIgnoreCase("pipeline"))
                        .flatMap(pipeline -> pipeline.getChildren().stream())
                        .filter(child -> child.getName().equalsIgnoreCase("exits"))
                        .flatMap(exitsTag -> exitsTag.getChildren().stream())
                        .filter(exitTag -> exitTag.getName().equalsIgnoreCase("exit"))
                        .count();
            }

            private List<Content> updateRecurringExits(Xml.Tag adapter, List<Xml.Tag> recurringExits, Xml.Document document) {
                // Filter recurring exits: only keep unique code values
                List<? extends Content> filteredRecurringExits = adapter.getContent().stream()
                        .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                        .flatMap(pipelineContent -> ((Xml.Tag) pipelineContent).getContent().stream())
                        .filter(content -> content instanceof Xml.Tag exitsTag && exitsTag.getName().equalsIgnoreCase("exits"))
                        .flatMap(exitsContent -> ((Xml.Tag) exitsContent).getContent().stream())
                        .filter(exitTag -> {
                            if (!(exitTag instanceof Xml.Tag t)) {
                                return true;
                            }
                            String code = TagHandler.getAttributeValueFromTagByKey(t, "code").orElse(null);
                            return code == null || !recurringExits.contains(t);
                        })
                        .toList();

                Optional<? extends Content> exitsOptional = adapter.getContent().stream()
                        .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                        .flatMap(pipelineContent -> ((Xml.Tag) pipelineContent).getContent().stream())
                        .filter(content -> content instanceof Xml.Tag t && t.getName().equalsIgnoreCase("exits"))
                        .findFirst();

                if (exitsOptional.isPresent()) {
                    // Filter child tags in "exits", keeping only those in filteredRecurringExits
                    List<? extends Content> updatedExitChildren = ((Xml.Tag) exitsOptional.get()).getContent()
                            .stream()
                            .filter(content -> content instanceof Xml.Tag t && filteredRecurringExits.contains(t))
                            .toList();

                    // Remove the existing "exits" tag from the pipeline content
                    List<Content> pipelineContentWithoutExits = new ArrayList<>(adapter.getContent().stream()
                            .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                            .flatMap(pipelineContent -> ((Xml.Tag) pipelineContent).getContent().stream())
                            .filter(content -> !(content instanceof Xml.Tag t && t.getName().equalsIgnoreCase("exits")))
                            .toList());

                    // Create a new "exits" tag with the updated children
                    Xml.Tag updatedExitsTag = ((Xml.Tag) exitsOptional.get()).withContent(updatedExitChildren);

                    // Add the updated "exits" tag back to the pipeline content
                    pipelineContentWithoutExits.add(0, updatedExitsTag);

                    // Update the "pipeline" content in the adapter
                    List<Content> updatedPipelineContent = new ArrayList<>(adapter.getContent().stream()
                            .map(content -> {
                                if (content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline")) {
                                    return ((Xml.Tag) pipelineTag).withContent(pipelineContentWithoutExits);
                                }
                                return content;
                            })
                            .toList());

                    adapter = adapter.withContent(updatedPipelineContent);
                }

                Xml.Tag finalAdapter = adapter;
                return adapter.getContent() == null ? Collections.emptyList() :
                        adapter.getContent().stream()
                                .map(child -> updateTagContent(document, finalAdapter, child))
                                .toList();
            }

            private Content updateTagContent(Xml.Document document, Xml.Tag adapter, Content content) {
                if (!(content instanceof Xml.Tag tag))
                    return content;

                List<Xml.Attribute> updatedAttributes = getUpdatedAttributes(document, adapter, tag);
                List<Content> updatedChildren = tag.getContent() == null ?
                        Collections.emptyList() :
                        tag.getContent().stream()
                                .map(child -> updateTagContent(document, adapter, child))
                                .toList();

                return tag.withAttributes(updatedAttributes).withContent(updatedChildren);
            }

            private List<Xml.Attribute> getUpdatedAttributes(Xml.Document document, Xml.Tag adapter, Xml.Tag tag) {
                List<Xml.Attribute> updatedAttributes = new ArrayList<>(tag.getAttributes());
                TagHandler.getAttributeFromTagByKey(tag, "path")
                        .ifPresent(attr -> updatePathAttribute(document, adapter, updatedAttributes, attr));
                return updatedAttributes;
            }

            private void updatePathAttribute(Xml.Document document, Xml.Tag adapter, List<Xml.Attribute> updatedAttributes, Xml.Attribute oldPathAttr) {
                String newPath = acc.getNewExitNameByOldPathForDocument(document, adapter, oldPathAttr.getValueAsString());
                if (newPath != null) {
                    updatedAttributes.removeIf(attr -> attr.getKeyAsString().equalsIgnoreCase("path"));
                    updatedAttributes.add(oldPathAttr.withValue(oldPathAttr.getValue().withValue(newPath)));
                }
            }
        };
    }

    private boolean isAdapterTag(Xml.Document document, Xml.Tag tag) {
        return document != null && tag.getName().equalsIgnoreCase("adapter");
    }
}

