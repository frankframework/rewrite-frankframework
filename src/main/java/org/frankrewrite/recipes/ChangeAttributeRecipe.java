/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public ChangeAttributeRecipe(@JsonProperty("tagNameFilter")String tagNameFilter, @JsonProperty("attributeKeyFilter")String attributeKeyFilter, @JsonProperty("newKey")String newKey, @JsonProperty("attributeValueFilter")String attributeValueFilter, @JsonProperty("newValue")String newValue) {
        this.tagNameFilter = tagNameFilter;
        this.attributeKeyFilter = attributeKeyFilter;
        this.newKey = newKey;
        this.attributeValueFilter = attributeValueFilter;
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
