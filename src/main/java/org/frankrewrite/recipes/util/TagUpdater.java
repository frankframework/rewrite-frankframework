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

package org.frankrewrite.recipes.util;

import org.jetbrains.annotations.Nullable;
import org.openrewrite.xml.tree.Xml;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class TagUpdater {

    public static Xml.Tag getUpdatedTagWithNewName(Xml.Tag tag){
        Map<Class<?>, Class<?>> oldNew = ElementMapper.getDeprecatedClassToNewClassMapInPackage();
        for (Map.Entry<Class<?>, Class<?>> entry : oldNew.entrySet()) {
            if (tag.getName().equalsIgnoreCase(entry.getKey().getSimpleName())) {
                return tag.withName(entry.getValue().getSimpleName());
            }
            else if (isPipe(entry.getValue())){
                String newName = entry.getValue().getSimpleName().contains("Pipe")?entry.getValue().getSimpleName():entry.getValue().getSimpleName()+"Pipe";
                String oldName = entry.getKey().getSimpleName().contains("Pipe")?entry.getKey().getSimpleName():entry.getKey().getSimpleName()+"Pipe";

                if (tag.getName().equalsIgnoreCase(oldName)) {
                    return tag.withName(newName);
                }else if (tag.getName().contains(newName)) {
                    return tag.withName(oldName+"Pipe");
                }
            }
        }
        return tag;
    }

    private static boolean isPipe(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        String[] packageList = packageName.split("\\.");
        String classDir = packageList[packageList.length - 1];
        return classDir.equalsIgnoreCase("pipes");
    }


    public static Xml.Tag getTagWithNewAttributes(Xml.Tag tag){
        boolean changed = false;
        //Update tag name if necessary
        if (!getUpdatedTagWithNewName(tag).getName().equalsIgnoreCase(tag.getName())){
            tag = getUpdatedTagWithNewName(tag);
            changed=true;
        }

        //Get Map of deprecated and new element attributes
        Map<Method,Method> deprecatedAttributesToNewElementMap =
                ElementMapper.getDeprecatedMethodToNewMethodMapForClass(tag.getName());

        //Get current attributes in the tag
        List<Xml.Attribute> newAttributes = tag.getAttributes();

        //Loop through the map
        for (Map.Entry<Method, Method> oldNew : deprecatedAttributesToNewElementMap.entrySet()) {

            Method toReplaceAttribute = oldNew.getKey();
            Method replacementAttribute = oldNew.getValue();

            //Get the attribute if it exists
            Xml.Attribute existingAttribute = getExistingAttribute(newAttributes, getAttributeName(toReplaceAttribute));

            if (existingAttribute != null) {
                //Replace the attribute
                newAttributes.remove(existingAttribute);
                newAttributes.add(existingAttribute.withKey(existingAttribute.getKey().withName(getAttributeName(replacementAttribute))));
                changed = true;
            }
        }

        return changed?tag.withAttributes(newAttributes):null;
    }

    private static Xml.@Nullable Attribute getExistingAttribute(List<Xml.Attribute> newAttributes, String deprecatedAttributesToNewElementMapEntry) {
        return newAttributes.stream()
                .filter(attr -> attr.getKey().getName().equals(deprecatedAttributesToNewElementMapEntry))
                .findFirst()
                .orElse(null);
    }

    private static String getAttributeName(Method method){
        String withoutSet = method.getName().substring(method.getName().lastIndexOf("set")+3);
        return Character.toString(withoutSet.charAt(0)).toLowerCase()+withoutSet.substring(1);
    }

}
