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
package org.frankrewrite.recipes.visitors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class ChangeChildAttributeVisitor extends XmlIsoVisitor<ExecutionContext> {
    String tagNameFilter;
    String childTagNameFilter;
    String attributeKeyFilter;
    String newKey;
    String attributeValueFilter;
    String newValue;

    public ChangeChildAttributeVisitor(String tagNameFilter, String childTagNameFilter, String attributeKeyFilter, String newKey, String attributeValueFilter, String newValue) {
        this.tagNameFilter = tagNameFilter;
        this.childTagNameFilter = childTagNameFilter;
        this.attributeKeyFilter = attributeKeyFilter;
        this.newKey = newKey;
        this.attributeValueFilter = attributeValueFilter;
        this.newValue = newValue;
    }

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        if (tagNameFilter != null
                && !tag.getName().equals(tagNameFilter)) {
            return super.visitTag(tag, executionContext);
        } else {
            AtomicBoolean changed = new AtomicBoolean(false);
            // Create a new list with updated child tags
            List<Content> updatedChildren = getContent(tag).stream()
                    .map(child -> {
                        if (child instanceof Xml.Tag t && t.getName().equals(childTagNameFilter)) {
                            Xml.Tag result = new ChangeAttributeVisitor(
                                    childTagNameFilter, attributeKeyFilter, newKey, attributeValueFilter, newValue
                            ).visitTag(t, executionContext);

                            // Compare attributes: Only replace if the attributes have actually changed
                            if (!areAttributesEqual(t, result)) {
                                changed.set(true);
                                return result; // Replace with updated tag
                            }
                        }
                        return child;
                    })
                    .toList(); // Ensure we create a new immutable list

            return changed.get()?tag.withContent(updatedChildren):super.visitTag(tag, executionContext);
        }
    }
    // Helper method to compare attributes between the original and modified tag
    private boolean areAttributesEqual(Xml.Tag original, Xml.Tag modified) {
        List<Xml.Tag.Attribute> originalAttributes = original.getAttributes(); // Get the list of original attributes
        List<Xml.Tag.Attribute> modifiedAttributes = modified.getAttributes(); // Get the list of modified attributes

        // Compare each attribute
        for (int i = 0; i < originalAttributes.size(); i++) {
            String originalAttributeKey = originalAttributes.get(i).getKeyAsString();
            String modifiedAttributeKey = modifiedAttributes.get(i).getKeyAsString();
            String originalAttributeValue = originalAttributes.get(i).getValueAsString();
            String modifiedAttributeValue = modifiedAttributes.get(i).getValueAsString();

            // If any attribute differs, return false
            if ((!originalAttributeKey.equals(modifiedAttributeKey))||(!originalAttributeValue.equals(modifiedAttributeValue))) {
                return false;
            }
        }

        return true; // If no differences were found, the attributes are equal
    }

}
