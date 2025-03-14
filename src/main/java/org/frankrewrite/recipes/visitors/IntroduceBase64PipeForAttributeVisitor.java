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
        if (tag.getName().equalsIgnoreCase("pipeline")) {
            AtomicBoolean changed = new AtomicBoolean(false);
            List<Content> updatedChildren = getContent(tag).stream().map(content -> {
                if (content instanceof Xml.Tag child
                    && child.getName().equals("LocalFileSystemPipe") && TagHandler.hasAnyAttributeWithKey(child, "base64")
                        && TagHandler.hasAnyAttributeWithKey(child, "storeResultInSessionKey")
                        && TagHandler.hasAnyAttributeWithKey(child, "name")) {
                    String storeResultInSessionKeyValue = TagHandler.getAttributeValueFromTagByKey(child, "storeResultInSessionKey").orElse("");
                    String base64Value = TagHandler.getAttributeValueFromTagByKey(child, "base64").orElse("");
                    String pipeName = TagHandler.getAttributeValueFromTagByKey(child, "name").get()+(base64Value.equals("ENCODE")?"Encoder":"Decoder");

                    child = TagHandler.getTagWithoutAttribute(child,"base64");
                    List<Content> childContent = getContent(child).stream()
                            .map(grandchild -> {
                                if (grandchild instanceof Xml.Tag t && t.getName().equalsIgnoreCase("forward")) {
                                    return t.withAttributes(
                                            t.getAttributes().stream()
                                                .map(attr -> (attr.getKeyAsString().equals("path")&&attr.getValueAsString().equals("success")) ? attr.withValue(attr.getValue().withValue(pipeName)) : attr)
                                                .toList()
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
                    changed.set(true);

                    return List.of(child, echoPipe);

                }
                return new ArrayList<>(List.of(content));
            }).flatMap(List::stream).collect(Collectors.toList());

            if (changed.get())
                return tag.withContent(updatedChildren);
        }
        return super.visitTag(tag, ctx);
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
