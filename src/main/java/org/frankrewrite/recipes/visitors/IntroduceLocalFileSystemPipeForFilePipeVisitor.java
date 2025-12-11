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
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.Optional;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class IntroduceLocalFileSystemPipeForFilePipeVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        //Find attributes
        Optional<Xml.Attribute> fileNameSessionKeyAttributeOptional = TagHandler.getAttributeFromTagByKey(tag, "fileNameSessionKey");
        Optional<Xml.Attribute> directoryAttributeOptional = TagHandler.getAttributeFromTagByKey(tag, "directory");
        Optional<Xml.Attribute> filenameAttributeOptional = TagHandler.getAttributeFromTagByKey(tag, "filename");

        List<Content> content = getContent(tag); // To get rid of the wildcard type

        String parentIndent = tag.getPrefix(); // Prefix contains leading spaces & newlines
        String childIndent = parentIndent + "    "; // Assuming 4-space indentation
        
        // Case 1: fileNameSessionKey only
        if (fileNameSessionKeyAttributeOptional.isPresent() && directoryAttributeOptional.isEmpty()) {
            String fileNameSessionKeyValue = fileNameSessionKeyAttributeOptional.get().getValueAsString();
            Xml.Tag param = Xml.Tag.build("<Param name=\"filename\" sessionKey=\"" + fileNameSessionKeyValue + "\"/>")
                    .withPrefix("\n" + childIndent); // Ensure correct indentation
            content.add(param);

            return TagHandler.getTagWithoutAttribute(tag, "fileNameSessionKey")// Remove the original 'fileNameSessionKey' attribute
                    .withContent(content) //Add parameters
                    .withPrefix(parentIndent);  // Maintain parent's indentation
        }

        // Case 2: directory + filename combination
        if (directoryAttributeOptional.isPresent() && filenameAttributeOptional.isPresent()) {
            String directoryValue = directoryAttributeOptional.get().getValueAsString();
            String filenameValue = filenameAttributeOptional.get().getValueAsString();
            String combinedPath = directoryValue.endsWith("/") ? directoryValue + filenameValue : directoryValue + "/" + filenameValue;

            Xml.Tag param = Xml.Tag.build("<Param name=\"filename\" value=\"" + combinedPath + "\"/>")
                    .withPrefix("\n" + childIndent);
            content.add(param);

            tag = TagHandler.getTagWithoutAttribute(tag, "directory");
            tag = TagHandler.getTagWithoutAttribute(tag, "filename");

            return tag.withContent(content).withPrefix(parentIndent);
        }

        // Case 3: directory + fileNameSessionKey combination
        if (directoryAttributeOptional.isPresent() && fileNameSessionKeyAttributeOptional.isPresent()) {
            String directoryValue = directoryAttributeOptional.get().getValueAsString();
            String fileNameSessionKeyValue = fileNameSessionKeyAttributeOptional.get().getValueAsString();

            Xml.Tag directoryParam = Xml.Tag.build("<Param name=\"directory\" value=\"" + directoryValue + "\"/>")
                .withPrefix("\n" + childIndent + childIndent);

            Xml.Tag fileNameSessionKeyParam = Xml.Tag.build("<Param name=\"fileNameSessionKey\" sessionKey=\"" + fileNameSessionKeyValue + "\"/>")
                .withPrefix("\n" + childIndent + childIndent);

                
            Xml.Tag param = Xml.Tag.build("<Param name=\"filename\" xpathExpression=\"concat($directory, '/', $fileNameSessionKey)\"/>")
                    .withContent(List.of(directoryParam, fileNameSessionKeyParam))
                    .withPrefix("\n" + childIndent);

            // Xml.Tag param = Xml.Tag.build("<Param name=\"filename\" xpathExpression=\"concat($directory, '/', $fileNameSessionKey)\">" + directoryParam.printAll() + fileNameSessionKeyParam.printAll() + "</Param>")
            //     .withPrefix("\n" + childIndent);
            content.add(param);

            tag = TagHandler.getTagWithoutAttribute(tag, "directory");
            tag = TagHandler.getTagWithoutAttribute(tag, "fileNameSessionKey");

            return tag.withContent(content).withPrefix(parentIndent);
        }
        
        return super.visitTag(tag, executionContext);
    }
}