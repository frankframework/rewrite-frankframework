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

import org.frankrewrite.recipes.util.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;

public class AddNameAttributeToTagRecipe extends Recipe {
    @Option(displayName = "Tag name",
            description = "The name of the tags of which to add the name attribute to.",
            required = true)
    String tagName;

    private static int elementsWithName=0;

    private static void increaseElementsWithName(){
        elementsWithName++;
    }

    public AddNameAttributeToTagRecipe(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Add name attribute to xml tag recipe";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Adds dynamic name attribute to the xml tags that have the tag name passed to the recipe.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>(){
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if (tag.getName().equals(tagName)&& !TagHandler.hasAnyAttributeWithKey(tag, "name")){

                    List<Xml.Attribute> attributes = new ArrayList<>(tag.getAttributes());
                    Xml.Attribute attributeToAdd = Xml.Tag.build("<x name=\"my"+tag.getName()+(elementsWithName==0?"":elementsWithName+1)+"\"></x>").getAttributes().get(0);
                    attributes.add(attributeToAdd);
                    //avoid conflicting names
                    increaseElementsWithName();
                    return tag.withAttributes(attributes);
                }

                return super.visitTag(tag, executionContext);
            }
        };
    }
}
