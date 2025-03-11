package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class CapitalizeAttributeValueRecipeTest implements RewriteTest {
    @Test
    void capitalizeAttributeValueTest() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new CapitalizeAttributeValueRecipe("attribute", "test")),
          //language=xml
          xml(
            """
             <root>
                 <test attribute="test"/>
                 <test attribute2="test"/>
                 <test2 attribute="test"/>
             </root>
             """,
             """
              <root>
                  <test attribute="TEST"/>
                  <test attribute2="test"/>
                  <test2 attribute="test"/>
              </root>"""
          )
        );
    }
    @Test
    void capitalizeAttributeValueWithoutTagNameTest() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new CapitalizeAttributeValueRecipe("attribute", null)),
          //language=xml
          xml(
            """
             <root>
                 <test attribute="test"/>
                 <test1 attribute2="test"/>
                 <test2 attribute="test"/>
             </root>
             """,
             """
              <root>
                  <test attribute="TEST"/>
                  <test1 attribute2="test"/>
                  <test2 attribute="TEST"/>
              </root>"""
          )
        );
    }
}
