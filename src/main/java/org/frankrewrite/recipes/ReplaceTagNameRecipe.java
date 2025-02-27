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
