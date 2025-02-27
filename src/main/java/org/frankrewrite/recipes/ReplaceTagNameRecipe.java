package org.frankrewrite.recipes;

import org.openrewrite.*;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

public class ReplaceTagNameRecipe extends Recipe {
    @Option(displayName = "Old tag name",
            description = "The tag name to be replaced.",
            required = true)
    String oldName;
    @Option(displayName = "New tag name",
            description = "The new tag name to replace the old tag name with.",
            required = true)
    String newName;

    public ReplaceTagNameRecipe(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
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
        return new XmlIsoVisitor<>(){
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if (!tag.getName().equalsIgnoreCase(oldName)) {
                    super.visitTag(tag, executionContext);
                }
                return tag.withName(newName);
            }
        };
    }
}
