package org.frankrewrite.recipes;

import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MoveReturnedSessionKeysRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Move returned session keys";
    }
    @Override
    public String getDescription() {
        return "Move returned session keys to the top of the file.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>(){
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if ("Receiver".equalsIgnoreCase(tag.getName())) {
                    Optional<Xml.Attribute> attributeOptional = TagHandler.getAttributeFromTagByKey(tag, "returnedSessionKeys");
                    if (attributeOptional.isPresent()) {
                        Xml.Attribute returnedSessionKeyAttr = attributeOptional.get();

                        List<Xml.Attribute> filteredAttributes = tag.getAttributes().stream()
                                .filter(attribute -> !attribute.equals(returnedSessionKeyAttr)).toList();

                        Optional<Xml.Tag> listener = tag.getChildren().stream()
                                .filter(child -> "JavaListener".equalsIgnoreCase(child.getName()))
                                .findFirst();

                        if (listener.isPresent()) {
                            listener.get();
                            Xml.Tag listenerTag = listener.get();

                            // Update listener attributes
                            List<Xml.Attribute> updatedListenerAttributes = new ArrayList<>(listenerTag.getAttributes());
                            updatedListenerAttributes.add(returnedSessionKeyAttr);
                            Xml.Tag updatedListenerTag = listenerTag.withAttributes(updatedListenerAttributes);

                            // Update content
                            List<Content> filteredContent = new ArrayList<>(tag.getContent().stream()
                                    .filter(Objects::nonNull)
                                    .map(Content.class::cast)
                                    .filter(child -> !child.equals(listenerTag))
                                    .toList());
                            filteredContent.add(updatedListenerTag);

                            return tag.withAttributes(filteredAttributes).withContent(filteredContent);
                        }
                    }
                }
                return super.visitTag(tag, executionContext);
            }
        };
    }
}
