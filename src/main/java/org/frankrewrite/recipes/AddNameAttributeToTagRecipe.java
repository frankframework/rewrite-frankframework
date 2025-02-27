package org.frankrewrite.recipes;

import org.frankrewrite.recipes.util.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;

public class AddNameAttributeToTagRecipe extends Recipe {
    @Option(displayName = "Tag name",
            description = "The name of the tags of which to add the name attribute to.",
            required = true)
    String tagName;

    private static int elementsWithName=0;

    public AddNameAttributeToTagRecipe(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Add name attribute to xml tag recipe";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Adds dynamic name attribute to the xml tags that have the tag name passed to the recipe.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>(){
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if (tag.getName().equals(tagName)&& !TagHandler.hasAnyAttributeWithKey(tag, "name")){

                    List<Xml.Attribute> attributes = tag.getAttributes();
                    Xml.Attribute attributeToAdd = Xml.Tag.build("<x name=\"my"+tag.getName()+(elementsWithName==0?"":elementsWithName+1)+"\"></x>").getAttributes().get(0);
                    attributes.add(attributeToAdd);
                    //avoid conflicting names
                    elementsWithName++;
                    return tag.withAttributes(attributes);
                }

                return super.visitTag(tag, executionContext);
            }
        };
    }
}
