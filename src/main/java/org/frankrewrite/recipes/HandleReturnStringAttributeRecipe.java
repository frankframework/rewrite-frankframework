package org.frankrewrite.recipes;

import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.XmlIsoVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class HandleReturnStringAttributeRecipe extends Recipe {
    private static int amountRefactored = 1;

    @Override
    public String getDisplayName() {
        return "Introduce EchoPipe and update path strings";
    }

    @Override
    public String getDescription() {
        return "Replaces returnString attribute in FixedResultPipe with an EchoPipe and updates path references.";
    }

    @Override
    public XmlIsoVisitor<ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<ExecutionContext>() {
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
                if (tag.getName().equalsIgnoreCase("pipeline")) {
                    AtomicBoolean changed = new AtomicBoolean(false);
                    List<Content> updatedChildren = tag.getContent().stream().map(content -> {
                        if (content instanceof Xml.Tag child) {
                            if (child.getName().equals("FixedResultPipe") && TagHandler.hasAnyAttributeWithKey(child, "returnString")) {
                                String returnStringValue = TagHandler.getAttributeValueFromTagByKey(child, "returnString").orElse("");
                                String pipeName = "myEchoPipe"+ (amountRefactored!=1?amountRefactored:"");

                                // Remove returnString attribute
                                child = child.withAttributes(
                                        child.getAttributes().stream()
                                                .filter(attr -> !attr.getKey().equals("returnString"))
                                                .collect(Collectors.toList())
                                );

                                child = TagHandler.getTagWithoutAttribute(child,"returnString");
                                List<Content> childContent = child.getContent().stream()
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
                                String pathValue =child.getContent().stream()
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
        };
    }
}