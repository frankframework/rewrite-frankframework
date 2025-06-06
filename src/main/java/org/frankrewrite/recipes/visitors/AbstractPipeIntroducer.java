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
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public abstract class AbstractPipeIntroducer extends XmlIsoVisitor<ExecutionContext> {

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
        if (!tag.getName().equalsIgnoreCase("pipeline")) {
            return super.visitTag(tag, ctx);
        }

        AtomicBoolean changed = new AtomicBoolean(false);
        List<Content> updatedChildren = getUpdatedChildren(tag, changed);

        if (changed.get()) {
            return tag.withContent(updatedChildren);
        }

        return super.visitTag(tag, ctx);
    }

    protected List<Content> updateForwardPathAttributeWithNewPipeName(Xml.Tag child, String pipeName) {
        List<Content> childContent = getContent(child);
        childContent.stream()
                .filter(grandChild -> grandChild instanceof Xml.Tag t
                        && t.getName().equalsIgnoreCase("forward")
                        && TagHandler.hasAnyAttributeWithKeyValue(t, "name", "success"))
                .findFirst()
                .map(Xml.Tag.class::cast)
                .ifPresent(successForwardTag -> {
                    Xml.Tag updatedSuccessTag = TagHandler.getTagWithNewAttributeValueByAttributeName(successForwardTag, pipeName, "path");
                    if (updatedSuccessTag != null) {
                        childContent.set(childContent.indexOf(successForwardTag), updatedSuccessTag);
                    }
                });
        return childContent;
    }

    protected @NotNull String getForwardPathValue(Xml.Tag child, String nameValue) {
        return getContent(child).stream()
                .filter(grandchild -> grandchild instanceof Xml.Tag t
                        && t.getName().equalsIgnoreCase("forward")
                        && TagHandler.getAttributeValueFromTagByKey(t, "name").map(nameValue::equals).orElse(false)) // Avoids unnecessary orElse("")
                .map(t -> TagHandler.getAttributeValueFromTagByKey((Xml.Tag) t, "path"))
                .findFirst()
                .flatMap(path -> path) // Removes double Optional
                .orElse(""); // Return empty string if not found
    }


    protected abstract List<Content> getUpdatedChildren(Xml.Tag tag, AtomicBoolean changed);

}