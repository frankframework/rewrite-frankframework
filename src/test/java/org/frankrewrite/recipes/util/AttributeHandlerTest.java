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

import org.junit.jupiter.api.Test;
import org.openrewrite.xml.tree.Xml;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AttributeHandlerTest {
    @Test
    void updateListForAttributeWithNewValueUpdatesList() {
        Xml.Tag tag = Xml.Tag.build("""
          <MyTag key="value"/>""");
        List<Xml.Attribute> attributes = tag.getAttributes();

        AttributeHandler.updateListForAttributeWithNewValue(attributes, tag.getAttributes().get(0), "value2");

        //expect <MyTag key="value2"/>
        assertTrue(attributes.stream().anyMatch(attr->attr.getValueAsString().equals("value2")));
    }
    @Test
    void updateListForAttributeWithNewValueNotUpdatesListWhenValueIsNull() {
        Xml.Tag tag = Xml.Tag.build("""
          <MyTag key="value"/>""");
        List<Xml.Attribute> attributes = tag.getAttributes();

        AttributeHandler.updateListForAttributeWithNewValue(attributes, tag.getAttributes().get(0), null);

        //expect <MyTag key="value"/>
        assertTrue(attributes.stream().anyMatch(attr->attr.getValueAsString().equals("value")));
    }
}