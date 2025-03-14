package org.frankrewrite.recipes.visitors;

import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class HandleReturnStringAttributeVisitor extends XmlIsoVisitor<ExecutionContext> {
    private static int amountRefactored = 1;
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
        if (tag.getName().equalsIgnoreCase("pipeline")) {
            AtomicBoolean changed = new AtomicBoolean(false);
            List<Content> updatedChildren = getContent(tag).stream().map(content -> {
                if (content instanceof Xml.Tag child) {
                    if (child.getName().equals("FixedResultPipe") && TagHandler.hasAnyAttributeWithKey(child, "returnString")) {
                        String returnStringValue = TagHandler.getAttributeValueFromTagByKey(child, "returnString").orElse("");
                        String pipeName = "myEchoPipe"+ (amountRefactored!=1?amountRefactored:"");

                        // Remove returnString attribute
                        child = child.withAttributes(
                                child.getAttributes().stream()
                                        .filter(attr -> !attr.getKeyAsString().equals("returnString"))
                                        .collect(Collectors.toList())
                        );

                        child = TagHandler.getTagWithoutAttribute(child,"returnString");
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
                                "<EchoPipe name=\"" + pipeName + "\" getInputFromFixedValue='" + returnStringValue + "'>" +
                                        "<forward name=\"success\" path=\""+pathValue+"\"/>" +
                                        "</EchoPipe>"
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
}