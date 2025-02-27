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

import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.Optional;

public class ChangeAttributeVisitor extends XmlIsoVisitor<ExecutionContext> {
    String tagNameFilter;
    String attributeKeyFilter;
    String newKey;
    String attributeValueFilter;
    String newValue;


    public ChangeAttributeVisitor(String tagNameFilter, String attributeKeyFilter, String newKey, String attributeValueFilter, String newValue) {
        this.tagNameFilter = tagNameFilter;
        this.attributeKeyFilter = attributeKeyFilter;
        this.newKey = newKey;
        this.attributeValueFilter = attributeValueFilter;
        this.newValue = newValue;
    }

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        // FILTERS
        //Check if tag name matches if filter exists
        if (tagNameFilter !=null && !tagNameFilter.equalsIgnoreCase(tag.getName())) {
            return super.visitTag(tag, executionContext);
        }
        //Check if attribute key/value matches if filter exists
        if (attributeKeyFilter!=null && attributeValueFilter!=null && !TagHandler.hasAnyAttributeWithKeyValue(tag, attributeKeyFilter, attributeValueFilter)){
            return super.visitTag(tag, executionContext);
        }
        //Check if attribute key/value matches if filter exists
        if (attributeKeyFilter!=null && !TagHandler.hasAnyAttributeWithKey(tag, attributeKeyFilter)){
            return super.visitTag(tag, executionContext);
        }
        //Check if attribute key/value matches if filter exists
        if (attributeValueFilter!=null && !TagHandler.hasAnyAttributeWithValue(tag, attributeValueFilter)){
            return super.visitTag(tag, executionContext);
        }

        // GET SPECIFIED ATTRIBUTE
        Optional<Xml.Attribute> toChangeOptional = (attributeKeyFilter != null) ?
                TagHandler.getAttributeFromTagByKey(tag, attributeKeyFilter) :
                (attributeValueFilter != null) ?
                        TagHandler.getAttributeFromTagByValue(tag, attributeValueFilter) :
                        Optional.empty();

        //MAKE ATTRIBUTE CHANGES
        if (toChangeOptional.isPresent()&&(newKey!=null||newValue!=null)) {
            Xml.Attribute toChange = toChangeOptional.get();
            tag = TagHandler.getTagWithoutAttribute(tag, toChange.getKey().getName());
            if (newKey!=null){
                toChange = toChange.withKey(toChange.getKey().withName(newKey));
            }
            if (newValue!=null){
                toChange = toChange.withValue(toChange.getValue().withValue(newValue));
            }
            List<Xml.Attribute> resultAttributes = tag.getAttributes();
            resultAttributes.add(toChange);
            return tag.withAttributes(resultAttributes);
        }

        return super.visitTag(tag, executionContext);
    }
}
