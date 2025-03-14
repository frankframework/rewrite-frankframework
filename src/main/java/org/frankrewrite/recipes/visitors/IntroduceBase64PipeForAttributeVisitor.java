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
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class IntroduceBase64PipeForAttributeVisitor extends AbstractPipeIntroducer {
    @Override
    protected List<Content> getUpdatedChildren(Xml.Tag tag, AtomicBoolean changed) {
        return getContent(tag).stream().map(content -> {
            if (content instanceof Xml.Tag child && shouldIntroduceBase64PipeForChildTag(child)) {
                String storeResultInSessionKeyValue = TagHandler.getAttributeValueFromTagByKey(child, "storeResultInSessionKey").orElse("");
                String base64Value = TagHandler.getAttributeValueFromTagByKey(child, "base64").orElse("");
                String pipeName = TagHandler.getAttributeValueFromTagByKey(child, "name").get()+(base64Value.equals("ENCODE")?"Encoder":"Decoder");

                child = TagHandler.getTagWithoutAttribute(child,"base64"); //Remove attribute

                // Get path attribute value with corresponding forward tag in child
                String successPathValue = getForwardPathValue(child, "success"); //Before success forward path has changed, get the old value
                String exceptionPathValue = getForwardPathValue(child, "exception");

                //Update the forward path to the base64pipe name to be introduced
                List<Content> childContent = updateForwardPathAttributeWithNewPipeName(child, pipeName);

                // Update forward path
                child = child.withContent(childContent);


                // Create new EchoPipe element
                Xml.Tag base64Pipe = createBase64Pipe(pipeName, base64Value, storeResultInSessionKeyValue, exceptionPathValue, successPathValue);
                changed.set(true);

                return List.of(child, base64Pipe);

            }
            return new ArrayList<>(List.of(content));
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    protected Xml.Tag createBase64Pipe(String pipeName, String base64Value, String storeResultInSessionKeyValue, String exceptionPathValue, String successPathValue) {
        return Xml.Tag.build(
                "<Base64Pipe name=\"" + pipeName + "\" direction=\"" + base64Value + "\" storeResultInSessionKey=\"" + storeResultInSessionKeyValue + "\">" +
                        (exceptionPathValue.isEmpty() ?"":"\n       <forward name=\"exception\" path=\""+exceptionPathValue+"\"/>") +
                        (successPathValue.isEmpty() ?"": "\n       <forward name=\"success\" path=\""+successPathValue+"\"/>") +
                        "\n   </Base64Pipe>"
        );
    }


    protected boolean shouldIntroduceBase64PipeForChildTag(Xml.Tag childTag) {
        return childTag.getName().equals("LocalFileSystemPipe")
                && TagHandler.hasAnyAttributeWithKey(childTag, "base64")
                && TagHandler.hasAnyAttributeWithKey(childTag, "storeResultInSessionKey")
                && TagHandler.hasAnyAttributeWithKey(childTag, "name");
    }

}
