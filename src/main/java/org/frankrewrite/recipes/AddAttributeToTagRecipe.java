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
import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.*;

import java.util.ArrayList;
import java.util.List;

public class AddAttributeToTagRecipe extends Recipe {

    @Option(displayName = "Tag name",
            description = "The tag name to add the attribute for.",
            required = true)
    String tagNameFilter;
    @Option(displayName = "Attribute key",
            description = "The attribute key to decide if the new attribute should be added.",
            required = true)
    String attributeKeyFilter;
    @Option(displayName = "New Attribute key",
            description = "The new attribute key to be added.",
            required = true)
    String newKey;
    @Option(displayName = "New Attribute value",
            description = "The new attribute value to the new attribute with.",
            required = true)
    String newValue;

    @JsonCreator
    public AddAttributeToTagRecipe(@JsonProperty("tagNameFilter") String tagNameFilter, @JsonProperty("attributeKeyFilter") String attributeKeyFilter, @JsonProperty("newKey") String newKey, @JsonProperty("newValue") String newValue) {
        this.tagNameFilter = tagNameFilter;
        this.attributeKeyFilter = attributeKeyFilter;
        this.newKey = newKey;
        this.newValue = newValue;
    }

    @Override
    public String getDisplayName() {
        return "Add attribute to <" + tagNameFilter + "> tag";
    }

    @Override
    public String getDescription() {
        return "Adds an attribute '" + newKey + "' with value '" + newValue + "' to <" + tagNameFilter + "> tags.";
    }

    @Override
    public XmlIsoVisitor<ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>() {
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if (tag.getName().equals(tagNameFilter)&& TagHandler.hasAnyAttributeWithKey(tag, attributeKeyFilter) && !TagHandler.hasAnyAttributeWithKey(tag, newKey)){
                    List<Xml.Attribute> attributes = new ArrayList<>(tag.getAttributes());
                    attributes.add(Xml.Tag.build("<x " + newKey + "=\"" + newValue + "\"/>").getAttributes().get(0));
                    return tag.withAttributes(attributes);
                }

                return super.visitTag(tag, executionContext);
            }
        };
    }
}
