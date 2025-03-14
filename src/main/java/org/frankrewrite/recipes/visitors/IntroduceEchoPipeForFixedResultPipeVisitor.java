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

public class IntroduceEchoPipeForFixedResultPipeVisitor extends AbstractPipeIntroducer {
    private static int amountRefactored = 1;
    @Override
    protected @NotNull List<Content> getUpdatedChildren(Xml.Tag tag, AtomicBoolean changed) {
        return getContent(tag).stream().map(content -> {
            if (content instanceof Xml.Tag child
                    && shouldHandleReturnStringAttributeForChild(child)) {

                String returnStringValue = TagHandler.getAttributeValueFromTagByKey(child, "returnString").orElse("");
                String pipeName = "myEchoPipe"+ (amountRefactored!=1?amountRefactored:"");

                // Remove returnString attribute
                child = child.withAttributes(
                        child.getAttributes().stream()
                                .filter(attr -> !attr.getKeyAsString().equals("returnString"))
                                .toList()
                );

                child = TagHandler.getTagWithoutAttribute(child,"returnString");

                // Change forward path value with echoPipe name attribute value
                List<Content> childContent = updateForwardPathAttributeWithNewPipename(child, pipeName);

                //Get path value for new EchoPipe forward
                String pathValue =getContent(child).stream()
                        .filter(grandchild ->
                                grandchild instanceof Xml.Tag t
                                        && t.getName().equalsIgnoreCase("forward"))
                        .map(t->
                                TagHandler.getAttributeValueFromTagByKey((Xml.Tag)t,"path"))
                        .findFirst().get().orElse("");

                // Update forward path
                child = child.withContent(
                        childContent
                );

                // Create new EchoPipe element
                Xml.Tag echoPipe = Xml.Tag.build(
                        "<EchoPipe name=\"" + pipeName + "\" getInputFromFixedValue='" + returnStringValue + "'>" +
                                "<forward name=\"success\" path=\""+pathValue+"\"/>" +
                                "</EchoPipe>"
                );
                increaseAmountRefactored();
                changed.set(true);

                return List.of(child, echoPipe);
            }

            return new ArrayList<>(List.of(content));
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    private static @NotNull List<Content> updateForwardPathAttributeWithNewPipename(Xml.Tag child, String pipeName) {
        return getContent(child).stream()
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
    }

    private static boolean shouldHandleReturnStringAttributeForChild(Xml.Tag child) {
        return child.getName().equals("FixedResultPipe")
                && TagHandler.hasAnyAttributeWithKey(child, "returnString");
    }

    public static void increaseAmountRefactored(){
        amountRefactored++;
    }

}