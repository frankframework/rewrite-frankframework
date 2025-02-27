package org.frankrewrite.recipes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.frankrewrite.recipes.visitors.ChangeAttributeVisitor;
import org.openrewrite.*;

public class ChangeAttributeRecipe extends Recipe {
    @Option(displayName = "Tag name",
            description = "The tag name to replace the attribute for.",
            required = false)
    String tagNameFilter;
    @Option(displayName = "Old attribute key",
            description = "The attribute key to be replaced.",
            required = false)
    String attributeKeyFilter;
    @Option(displayName = "New Attribute key",
            description = "The new attribute key to replace the old name with.",
            required = false)
    String newKey;
    @Option(displayName = "Old Attribute value",
            description = "The old attribute value.",
            required = false)
    String attributeValueFilter;
    @Option(displayName = "New Attribute value",
            description = "The new attribute value to the new attribute with.",
            required = false)
    String newValue;

    @JsonCreator
    public ChangeAttributeRecipe(@JsonProperty("tagName")String tagName, @JsonProperty("oldName")String oldName, @JsonProperty("newName")String newName, @JsonProperty("oldValue")String oldValue, @JsonProperty("newValue")String newValue) {
        this.tagNameFilter = tagName;
        this.attributeKeyFilter = oldName;
        this.newKey = newName;
        this.attributeValueFilter = oldValue;
        this.newValue = newValue;
    }

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Replace tag name recipe";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Recipe that replaces tag names with new ones as defined in the recipe parameters.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ChangeAttributeVisitor(tagNameFilter, attributeKeyFilter, newKey, attributeValueFilter, newValue);
    }
}
