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

public class ChangeAttributeValueToUnionTypeRecipeTest implements RewriteTest {
    @Test
    void capitalizeAttributeValueTest() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new ChangeAttributeValueToUnionTypeRecipe("attribute", "test")),
          //language=xml
          xml(
            """
             <root>
                 <test attribute="test me"/>
                 <test attribute2="test me"/>
                 <test2 attribute="test me"/>
             </root>
             """,
             """
              <root>
                  <test attribute="TEST_ME"/>
                  <test attribute2="test me"/>
                  <test2 attribute="test me"/>
              </root>"""
          )
        );
    }
    @Test
    void capitalizeAttributeValueWithoutTagNameTest() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new ChangeAttributeValueToUnionTypeRecipe("attribute", null)),
          //language=xml
          xml(
            """
             <root>
                 <test attribute="test me"/>
                 <test1 attribute2="test me"/>
                 <test2 attribute="test me"/>
             </root>
             """,
             """
              <root>
                  <test attribute="TEST_ME"/>
                  <test1 attribute2="test me"/>
                  <test2 attribute="TEST_ME"/>
              </root>"""
          )
        );
    }
}
