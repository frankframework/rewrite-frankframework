package org.frankrewrite.recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class RemoveInvalidChildrenRecipe extends Recipe {
    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Remove random children";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Remove random children from element only accepting elements.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>(){
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
                        .collect(Collectors.toList());
            }

            private boolean isInvalidContent(Content content, List<Content>[] invalidLists) {
                for (List<Content> list : invalidLists) {
                    if (list.contains(content)) return true;
                }
                return false;
            }
        };
    }
}
