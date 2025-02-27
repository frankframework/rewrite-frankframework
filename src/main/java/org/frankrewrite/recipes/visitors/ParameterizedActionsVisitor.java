package org.frankrewrite.recipes.visitors;

import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParameterizedActionsVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        //Find actions attribute
        Optional<Xml.Attribute> actionsAttributeOptional = TagHandler.getAttributeFromTagByKey(tag, "actions");

        //Ensure actions attribute exists
        if (actionsAttributeOptional.isPresent()) {
            Xml.Attribute actionsAttribute = actionsAttributeOptional.get();

            // Ensure tag content is not null
            List<Content> content = new ArrayList<>(Optional.ofNullable(tag.getContent()).orElse(new ArrayList<>()));

            // Determine the existing indentation level
            String parentIndent = tag.getPrefix();  // Prefix contains leading spaces & newlines
            String childIndent = parentIndent + "    ";  // Assuming 4-space indentation

            // Convert actions attribute value into multiple <param> children with proper indentation
            for (String s : actionsAttribute.getValueAsString().split("_")) {
                Xml.Tag child = Xml.Tag.build("<Param name=\"action\" value=\"" + s + "\"/>")
                        .withPrefix("\n" + childIndent);  // Ensure correct indentation
                content.add(child);
            }

            return TagHandler.getTagWithoutAttribute(tag, "actions")// Remove the original 'actions' attribute
                    .withContent(content) //Add parameters
                    .withPrefix(parentIndent);  // Maintain parent's indentation
        }

        return super.visitTag(tag, executionContext);
    }
}
