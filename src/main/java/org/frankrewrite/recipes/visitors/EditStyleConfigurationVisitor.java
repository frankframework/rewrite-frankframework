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

import org.frankrewrite.recipes.util.PackageScanner;
import org.frankrewrite.recipes.util.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class EditStyleConfigurationVisitor extends XmlIsoVisitor<ExecutionContext> {
    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
        Optional<Xml.Attribute> classNameAttribute = TagHandler.getAttributeFromTagByKey(tag, "className");

        //Handle exceptional cases, these produce warnings when refactored
        if (tag.getName().equalsIgnoreCase("errorStorage")||
                tag.getName().equalsIgnoreCase("messageLog")){
            super.visitTag(tag, ctx);
        }
        //Check if className Attribute exists in tag
        else if (classNameAttribute.isPresent()) {
            //Already sure that className Attribute exists, ignore warning
            String classSimpleName = classNameAttribute.get().getValue().getValue().substring(classNameAttribute.get().getValue().getValue().lastIndexOf(".")+1);
            if (!isCustomElement(classSimpleName)){
                String elementName = getElementName(classSimpleName);
                // Get the result tag and use getElementName to prevent bad element name casing
                if(elementName==null)
                    return super.visitTag(tag, ctx);
                Xml.Tag updatedTag = getTagWithClassNameValueAsTagName(tag, elementName);
                if (updatedTag != null) {
                    return updatedTag;
                }
            }else return super.visitTag(tag, ctx);

        }else if (tag.getName().equalsIgnoreCase("job")){
            Optional<Xml.Attribute> functionOptional = tag.getAttributes().stream().filter(attribute -> attribute.getKeyAsString().equalsIgnoreCase("function")).findFirst();
            if (functionOptional.isPresent()){
                String newName = functionOptional.get().getValueAsString()+"Job";

                return tag
                        .withName(newName)
                        .withAttributes(tag.getAttributes().stream()
                                .filter(attribute -> !attribute.getKeyAsString().equalsIgnoreCase("function")).toList());
            }
        }


        //Return the original tag if no changes were made
        return super.visitTag(tag, ctx);
    }

    private Xml.@Nullable Tag getTagWithClassNameValueAsTagName(Xml.@NotNull Tag tag, String classSimpleName) {
        Optional<Xml.Attribute> classNameAttribute = TagHandler.getAttributeFromTagByKey(tag, "className");
        if (classNameAttribute.isEmpty()) {
            return null; //If classNameAttribute doesn't exist in tag
        }
        //Get the className Xml.Attribute from the tag
        String className = classNameAttribute.get().getValue().getValue();
        @NotNull String[] subPackage = className.split("\\.");

        if (className.startsWith("org.frankframework.")
            || className.startsWith("nl.nn.adapterframework.")
        ) {
            //Extract the class name without package names
            String type = subPackage[subPackage.length-2];

            //Check if tag className is in the *.pipe.{pipe's simple class name} namespace and if it doesn't yet end with "Pipe"
            if (!classSimpleName.endsWith("Pipe")&&type.equals("pipes")){
                //Add "Pipe" at the end of the tag's name if it does not end with it already.
                //Some pipe classes don't end with pipe (in java), but need to (in xml) in order to make some properties accessible.
                // For instance Json2XmlValidator class needs to be implemented like <Json2XmlValidatorPipe .../> because Json2XmlValidator.setSender() is a protected method
                classSimpleName=classSimpleName+"Pipe";
            }

            //Update the tag name to the indexed class name
            Xml.Tag updatedTag = tag.withName(classSimpleName);

            //Remove the className attribute
            updatedTag = TagHandler.getTagWithoutAttribute(updatedTag, "className");
            return updatedTag.withContent(getContent(updatedTag));
        }
        return null;
    }

    private boolean isCustomElement(String className) {
        return PackageScanner.getInstance().getClasses().stream().noneMatch(clazz -> className.equalsIgnoreCase(clazz.getSimpleName()));  // Returns true if it's custom (not found), false if it's a known class
    }
    private String getElementName(String className) {
        Optional<Class<?>> foundClass = PackageScanner.getInstance().getClasses().stream().filter(clazz -> className.equalsIgnoreCase(clazz.getSimpleName())).findFirst();;  // Returns true if it's custom (not found), false if it's a known class
        return foundClass.map(Class::getSimpleName).orElse(null);
    }
}
