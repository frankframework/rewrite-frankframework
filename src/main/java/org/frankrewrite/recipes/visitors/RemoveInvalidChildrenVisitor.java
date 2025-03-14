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

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class RemoveInvalidChildrenVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        if (isTargetTag(tag.getName())) {
            List<Content> textChildren = getInvalidTextChildren(tag);

            if (!textChildren.isEmpty()) {
                if (tag.getName().equalsIgnoreCase("adapter")) {
                    List<Content> invalidContent = getInvalidAdapterChildren(tag);
                    return tag.withContent(filterValidContent(tag, textChildren, invalidContent));
                }
                return tag.withContent(filterValidContent(tag, textChildren));
            }
        }
        return super.visitTag(tag, executionContext);
    }

    private boolean isTargetTag(String tagName) {
        return tagName.equalsIgnoreCase("Configuration") ||
                tagName.equalsIgnoreCase("adapter") ||
                tagName.equalsIgnoreCase("pipeline");
    }

    private List<Content> getInvalidTextChildren(Xml.Tag tag) {
        return getContent(tag).stream()
                .filter(child -> child instanceof Xml.CharData charData && !charData.getText().startsWith("&"))
                .toList();
    }

    private List<Content> getInvalidAdapterChildren(Xml.Tag tag) {
        return getContent(tag).stream()
                .filter(it -> it instanceof Xml.Tag t &&
                        !(t.getName().equalsIgnoreCase("pipeline") ||
                                t.getName().equalsIgnoreCase("receiver") ||
                                t.getName().contains("ormatter")))
                .toList();
    }

    private List<Content> filterValidContent(Xml.Tag tag, List<Content>... invalidLists) {
        return getContent(tag).stream()
                .filter(it -> !isInvalidContent(it, invalidLists))
                .toList();
    }

    private boolean isInvalidContent(Content content, List<Content>[] invalidLists) {
        for (List<Content> list : invalidLists) {
            if (list.contains(content)) return true;
        }
        return false;
    }
}