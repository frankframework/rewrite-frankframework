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
import org.frankrewrite.recipes.visitors.RemoveAttributeVisitor;
import org.openrewrite.*;

public class RemoveAttributeRecipe extends Recipe {
    @Option(displayName = "Attribute name",
            description = "The name of the attribute to be removed globally.",
            required = true)
    String attributeName;
    @Option(displayName = "Tag name",
            description = "The name tag of which to delete an attribute.",
            required = false)
    String tagName;
    @Option(displayName = "Tag type",
            description = "The tag type of which to delete an attribute.",
            required = false)
    String tagType;
    @Option(displayName = "Value",
            description = "The value for which to delete an attribute.",
            required = false)
    String value;

    @JsonCreator
    public RemoveAttributeRecipe(@JsonProperty("attributeName") String attributeName, @JsonProperty("tagName") String tagName, @JsonProperty("value") String value, @JsonProperty("tagType") String tagType) {
        this.attributeName = attributeName;
        this.tagName = tagName;
        this.value = value;
        this.tagType = tagType;
    }

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Remove global attribute by name";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Removes the specified attribute from all xml tags.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveAttributeVisitor(attributeName, tagName, value, tagType);
    }
}

