package org.frankrewrite.recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.stream.Collectors;

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
                    List<? extends Content> textChildren = getInvalidTextChildren(tag);

                    if (!textChildren.isEmpty()) {
                        if (tag.getName().equalsIgnoreCase("adapter")) {
                            List<? extends Content> invalidContent = getInvalidAdapterChildren(tag);
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

            private List<? extends Content> getInvalidTextChildren(Xml.Tag tag) {
                return tag.getContent().stream()
                        .filter(child -> child instanceof Xml.CharData charData && !charData.getText().startsWith("&"))
                        .toList();
            }

            private List<? extends Content> getInvalidAdapterChildren(Xml.Tag tag) {
                return tag.getContent().stream()
                        .filter(it -> it instanceof Xml.Tag t &&
                                !(t.getName().equalsIgnoreCase("pipeline") ||
                                        t.getName().equalsIgnoreCase("receiver") ||
                                        t.getName().contains("ormatter")))
                        .toList();
            }

            private List<Content> filterValidContent(Xml.Tag tag, List<? extends Content>... invalidLists) {
                return tag.getContent().stream()
                        .filter(it -> !isInvalidContent(it, invalidLists))
                        .collect(Collectors.toList());
            }

            private boolean isInvalidContent(Content content, List<? extends Content>[] invalidLists) {
                for (List<? extends Content> list : invalidLists) {
                    if (list.contains(content)) return true;
                }
                return false;
            }
        };
    }
}
