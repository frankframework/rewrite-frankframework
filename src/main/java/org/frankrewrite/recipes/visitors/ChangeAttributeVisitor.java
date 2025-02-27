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


    public ChangeAttributeVisitor(String tagName, String oldName, String newName, String oldValue, String newValue) {
        this.tagNameFilter = tagName;
        this.attributeKeyFilter = oldName;
        this.newKey = newName;
        this.attributeValueFilter = oldValue;
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
        if (attributeKeyFilter!=null && attributeValueFilter!=null && !TagHandler.hasAnyAttributeWithKey(tag, attributeKeyFilter)){
            return super.visitTag(tag, executionContext);
        }
        //Check if attribute key/value matches if filter exists
        if (attributeKeyFilter!=null && attributeValueFilter!=null && !TagHandler.hasAnyAttributeWithValue(tag, attributeValueFilter)){
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
