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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class IntroduceReplacerPipeForFixedResultPipeVisitor extends XmlIsoVisitor<ExecutionContext>{
    private static int amountRefactored = 1;

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
        if (!tag.getName().equalsIgnoreCase("pipeline")) {
            return super.visitTag(tag, ctx);
        }

        //Track if tag changed to prevent unnecessary refactors
        AtomicBoolean changed = new AtomicBoolean(false);

        //Update pipeline children with new ReplacerPipe and remove replaceFrom/-To attributes from FixedResultPipe
        List<Content> updatedChildren = getUpdatedChildren(tag, changed);

        if (changed.get())
            return tag.withContent(updatedChildren);

        return super.visitTag(tag, ctx);
    }

    private static @NotNull List<Content> getUpdatedChildren(Xml.Tag tag, AtomicBoolean changed) {
        return getContent(tag).stream().map(content -> {
            if (content instanceof Xml.Tag child
                    && shouldIntroduceReplacerPipeForChild(child)) {

                String replaceFromValue = TagHandler.getAttributeValueFromTagByKey(child, "replaceFrom").orElse("");
                String replaceToValue = TagHandler.getAttributeValueFromTagByKey(child, "replaceTo").orElse("");
                String pipeName = "myReplacerPipe"+ (amountRefactored!=1?amountRefactored:"");

                // Remove returnString attribute
                child = child.withAttributes(
                        child.getAttributes().stream()
                                .filter(attr -> !attr.getKeyAsString().equals("replaceFrom")&&!attr.getKeyAsString().equals("replaceTo"))
                                .toList()
                );

                //Remove replaceFrom/-To attributes and update the success forward path value
                child = TagHandler.getTagWithoutAttribute(child,"replaceFrom");
                child = TagHandler.getTagWithoutAttribute(child,"replaceTo");
                List<Content> childContent = updateForwardPathAttributeValueWithNewReplacerPipeName(child, pipeName);

                String pathValue =getContent(child).stream()
                        .filter(grandchild -> grandchild instanceof Xml.Tag t && t.getName().equalsIgnoreCase("forward")).map(t->TagHandler.getAttributeValueFromTagByKey((Xml.Tag)t,"path")).findFirst().get().orElse("");

                // Update forward path
                child = child.withContent(
                        childContent
                );

                // Create new EchoPipe element
                Xml.Tag replacerPipe = createReplacerPipe(pipeName, replaceFromValue, replaceToValue, pathValue);

                increaseAmountRefactored();
                changed.set(true);

                return List.of(child, replacerPipe);
            }

            return List.of(content);
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    private static Xml.Tag createReplacerPipe(String pipeName, String replaceFromValue, String replaceToValue, String pathValue) {
        return Xml.Tag.build(
                "<ReplacerPipe name=\"" + pipeName + "\" find=\"" + replaceFromValue + "\" replace=\"" + replaceToValue + "\">" +
                        "<forward name=\"success\" path=\"" + pathValue + "\"/>" +
                        "</ReplacerPipe>"
        );
    }

    private static @NotNull List<Content> updateForwardPathAttributeValueWithNewReplacerPipeName(Xml.Tag child, String pipeName) {
        List<Content> childContent = getContent(child).stream()
                .map(grandchild -> {
                    if (grandchild instanceof Xml.Tag t && t.getName().equalsIgnoreCase("forward")) {
                        return t.withAttributes(
                                t.getAttributes().stream()
                                        .map(attr -> attr.getKeyAsString().equals("path") ? attr.withValue(attr.getValue().withValue(pipeName)) : attr)
                                        .toList()
                        );
                    }
                    return grandchild;
                })
                .toList();
        return childContent;
    }

    private static boolean shouldIntroduceReplacerPipeForChild(Xml.Tag child) {
        return child.getName().equals("FixedResultPipe")
                && TagHandler.hasAnyAttributeWithKey(child, "replaceFrom")
                && TagHandler.hasAnyAttributeWithKey(child, "replaceTo");
    }

    public static void increaseAmountRefactored(){
        amountRefactored++;
    }
}
