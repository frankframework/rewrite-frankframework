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
