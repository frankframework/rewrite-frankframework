package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class ReplaceTagNameRecipeTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReplaceTagNameRecipe("GenericReceiver","Receiver"));
    }

    @Test
    void addsNameAttributeToXmlValidatorPipe(){
        rewriteRun(
          xml(
            """
                <GenericReceiver/>""",
            """
                <Receiver/>"""
          )
        );
    }
}
