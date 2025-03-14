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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.properties.PropertiesIsoVisitor;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Optional;

public class FrameworkVersionRecipe extends Recipe {
    @Option(displayName = "Framework version",
            description = "The version to update the properties and pom to.",
            required = true)
    private String version;

    @JsonCreator
    public FrameworkVersionRecipe(@JsonProperty("version") String version) {
        this.version = version;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Framework version updater";
    }

    @Override
    public @NotNull String getDescription() {
        return "Update Framework version in properties and pom file.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext executionContext) {
                if (tree instanceof Xml.Document) {
                    return new XmlIsoVisitor<ExecutionContext>() {
                        @Override
                        public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                            Optional<String> tagValue = tag.getValue();
                            if (tagValue.isPresent()
                                &&!tagValue.get().equals(version)
                                &&(tag.getName().equals("iaf.version")||
                                    tag.getName().equals("ff.version"))) {
                                return tag.withValue(version);
                            }
                            return super.visitTag(tag, executionContext);
                        }
                    }.visit(tree, executionContext);
                } else if (tree instanceof Properties.File) {
                    return new PropertiesIsoVisitor<ExecutionContext>() {
                        @Override
                        public Properties.Entry visitEntry(Properties.Entry entry, ExecutionContext executionContext) {
                            if (entry.getKey().equals("ff.version")&&!entry.getValue().getText().equals(version)) {
                                return entry.withValue(entry.getValue().withText(version));
                            }
                            return super.visitEntry(entry, executionContext);
                        }
                    }.visit(tree, executionContext);
                }
                return super.visit(tree, executionContext);
            }

        };

    }
}



