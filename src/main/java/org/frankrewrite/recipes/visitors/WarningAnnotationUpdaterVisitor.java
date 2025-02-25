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

import org.frankrewrite.recipes.util.TagUpdater;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

public class WarningAnnotationUpdaterVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {

        Xml.Tag updatedTag = TagUpdater.getTagWithNewAttributes(tag);
        if (tag.getName().equalsIgnoreCase("GenericReceiver")){
            updatedTag = tag.withName("Receiver");
        }

        //Exit if no changes were made
        if (updatedTag!=null){
            return updatedTag;
        }
        return super.visitTag(tag, ctx);
    }
}
