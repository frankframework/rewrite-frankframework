package org.frankrewrite.recipes.util;

import org.openrewrite.xml.tree.Xml;

import java.util.List;

public class AttributeHandler {
    public static void updateListForAttributeWithNewValue(List<Xml.Attribute> updatedAttributes, Xml.Attribute oldPathAttr, String newValue) {
        if (newValue != null) {
            updatedAttributes.removeIf(attr -> attr.getKeyAsString().equalsIgnoreCase(oldPathAttr.getKeyAsString()));
            updatedAttributes.add(oldPathAttr.withValue(oldPathAttr.getValue().withValue(newValue)));
        }
    }
}
