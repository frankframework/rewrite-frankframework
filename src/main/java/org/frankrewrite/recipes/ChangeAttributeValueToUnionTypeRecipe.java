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
