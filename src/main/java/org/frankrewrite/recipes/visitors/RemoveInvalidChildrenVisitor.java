package org.frankrewrite.recipes.visitors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.List;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class RemoveInvalidChildrenVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        if (isTargetTag(tag.getName())) {
            List<Content> textChildren = getInvalidTextChildren(tag);

            if (!textChildren.isEmpty()) {
                if (tag.getName().equalsIgnoreCase("adapter")) {
                    List<Content> invalidContent = getInvalidAdapterChildren(tag);
                    return tag.withContent(filterValidContent(tag, textChildren, invalidContent));
                }
                return tag.withContent(filterValidContent(tag, textChildren));
            }
        }
        return super.visitTag(tag, executionContext);
    }

    private boolean isTargetTag(String tagName) {
        return tagName.equalsIgnoreCase("Configuration") ||
                tagName.equalsIgnoreCase("adapter") ||
                tagName.equalsIgnoreCase("pipeline");
    }

    private List<Content> getInvalidTextChildren(Xml.Tag tag) {
        return getContent(tag).stream()
                .filter(child -> child instanceof Xml.CharData charData && !charData.getText().startsWith("&"))
                .toList();
    }

    private List<Content> getInvalidAdapterChildren(Xml.Tag tag) {
        return getContent(tag).stream()
                .filter(it -> it instanceof Xml.Tag t &&
                        !(t.getName().equalsIgnoreCase("pipeline") ||
                                t.getName().equalsIgnoreCase("receiver") ||
                                t.getName().contains("ormatter")))
                .toList();
    }

    private List<Content> filterValidContent(Xml.Tag tag, List<Content>... invalidLists) {
        return getContent(tag).stream()
                .filter(it -> !isInvalidContent(it, invalidLists))
                .toList();
    }

    private boolean isInvalidContent(Content content, List<Content>[] invalidLists) {
        for (List<Content> list : invalidLists) {
            if (list.contains(content)) return true;
        }
        return false;
    }
}