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
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class IntroduceReplacerPipeFromFixedResultPipeRecipe extends Recipe {
    private static int amountRefactored = 1;

    @Override
    public String getDisplayName() {
        return "Introduce ReplacerPipe and update path strings";
    }

    @Override
    public String getDescription() {
        return "Replaces replaceFrom/To attributes in FixedResultPipe with an ReplacerPipe and updates path references.";
    }

    @Override
    public XmlIsoVisitor<ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>() {
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
                if (tag.getName().equalsIgnoreCase("pipeline")) {
                    AtomicBoolean changed = new AtomicBoolean(false);
                    List<Content> updatedChildren = getContent(tag).stream().map(content -> {
                        if (content instanceof Xml.Tag child) {
                            if (child.getName().equals("FixedResultPipe") && TagHandler.hasAnyAttributeWithKey(child, "replaceFrom")&& TagHandler.hasAnyAttributeWithKey(child, "replaceTo")) {
                                String replaceFromValue = TagHandler.getAttributeValueFromTagByKey(child, "replaceFrom").orElse("");
                                String replaceToValue = TagHandler.getAttributeValueFromTagByKey(child, "replaceTo").orElse("");
                                String pipeName = "myReplacerPipe"+ (amountRefactored!=1?amountRefactored:"");

                                // Remove returnString attribute
                                child = child.withAttributes(
                                        child.getAttributes().stream()
                                                .filter(attr -> !attr.getKeyAsString().equals("replaceFrom")&&!attr.getKeyAsString().equals("replaceTo"))
                                                .collect(Collectors.toList())
                                );

                                child = TagHandler.getTagWithoutAttribute(child,"replaceFrom");
                                child = TagHandler.getTagWithoutAttribute(child,"replaceTo");
                                List<Content> childContent = getContent(child).stream()
                                        .map(grandchild -> {
                                            if (grandchild instanceof Xml.Tag && ((Xml.Tag) grandchild).getName().equalsIgnoreCase("forward")) {
                                                return ((Xml.Tag) grandchild).withAttributes(
                                                        ((Xml.Tag) grandchild).getAttributes().stream()
                                                                .map(attr -> attr.getKeyAsString().equals("path") ? attr.withValue(attr.getValue().withValue(pipeName)) : attr)
                                                                .collect(Collectors.toList())
                                                );
                                            }
                                            return grandchild;
                                        })
                                        .collect(Collectors.toList());
                                String pathValue =getContent(child).stream()
                                        .filter(grandchild -> grandchild instanceof Xml.Tag && ((Xml.Tag) grandchild).getName().equalsIgnoreCase("forward")).map(t->TagHandler.getAttributeValueFromTagByKey((Xml.Tag)t,"path")).findFirst().get().orElse("");

                                // Update forward path
                                child = child.withContent(
                                        childContent
                                );

                                // Create new EchoPipe element
                                Xml.Tag echoPipe = Xml.Tag.build(
                                        "<ReplacerPipe name=\"" + pipeName + "\" find=\"" + replaceFromValue + "\" replace=\"" + replaceToValue + "\">" +
                                                "<forward name=\"success\" path=\""+pathValue+"\"/>" +
                                                "</ReplacerPipe>"
                                );
                                amountRefactored++;
                                changed.set(true);

                                return List.of(child, echoPipe);
                            }
                        }
                        return List.of(content);
                    }).flatMap(List::stream).collect(Collectors.toList());

                    if (changed.get())
                        return tag.withContent(updatedChildren);
                }
                return super.visitTag(tag, ctx);
            }
        };
    }
}
