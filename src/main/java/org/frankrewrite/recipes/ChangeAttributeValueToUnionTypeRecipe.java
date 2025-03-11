package org.frankrewrite.recipes;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.*;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Optional;

public class ChangeAttributeValueToUnionTypeRecipe extends Recipe {
    @Option(displayName = "Attribute key",
            description = "The attribute key of which to capitalize the value for.",
            required = true)
    String attributeKey;
    @Option(displayName = "Tag name",
            description = "The tag name of which to capitalize the attribute value for.",
            required = false)
    String tagName;

    public ChangeAttributeValueToUnionTypeRecipe(@JsonProperty("attributeKey")String attributeKey, @JsonProperty("tagName")String tagName) {
        this.attributeKey = attributeKey;
        this.tagName = tagName;
    }

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Capitalize attribute value and seperate spaces with underscores";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Recipe to capitalize the value for the given attribute.";
    }
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>(){
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if (tagName != null && !tagName.equals(tag.getName())) {
                    return super.visitTag(tag, executionContext);
                }
                Optional<Xml.Attribute> attr= TagHandler.getAttributeFromTagByKey(tag, attributeKey);
                if (attr.isPresent()) {
                    String newValue = attr.get().getValueAsString().toUpperCase()
                            .replace(" ","_");
                    if (newValue.equals(attr.get().getValueAsString())) {
                        return super.visitTag(tag, executionContext);
                    }
                    return TagHandler.getTagWithUpdatedAttributeValue(
                            tag,
                            attr.get(),
                            newValue
                        );
            }

                return super.visitTag(tag, executionContext);
            }
        };
    }
}
