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

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

class ChangeChildAttributeRecipeTest implements RewriteTest {

    @Test
    void testChangeChildAttributeValue() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", null, "value1", "value2")),
          //language=xml
          xml(
            """
             <parent>
             dfgh
                 <child attribute="value1"/>
             </parent>""","""
             <parent>
             dfgh
                 <child attribute="value2"/>
             </parent>"""
          )
        );
    }
    @Test
    void testChangeChildAttributeKey() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parent>
             dfgh
                 <child attribute="value1"/>
             </parent>""","""
             <parent>
             dfgh
                 <child attribute2="value1"/>
             </parent>"""
          )
        );
    }
    @Test
    void onlyChangeSpecifiedParent() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parent2>
                 <child attribute="value1"/>
             </parent2>"""
          )
        );
    }
    @Test
    void dontChangeParentWithoutContent() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parent/>"""
          )
        );
    }
    @Test
    void onlyChangeSpecifiedChildAttribute() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parent>
                 <child2 attribute="value1"/>
             </parent>"""
          )
        );
    }
    @Test
    void changeSpecifiedChildAttributeGlobally() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe(null, "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parentsdfgdsfg>
                 <child attribute="value1"/>
             </parentsdfgdsfg>""","""
             <parentsdfgdsfg>
                 <child attribute2="value1"/>
             </parentsdfgdsfg>"""
          )
        );
    }
}