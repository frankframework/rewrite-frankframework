package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class RemoveAttributeRecipeTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
    }

    @Test
    void removesConfigurationNameAttribute(){
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveAttributeRecipe("configurationName",null, null, null)),
          xml(
            """
                <Configuration configurationName="config">
                </Configuration>
            """,
            """
                <Configuration>
                </Configuration>
            """
          )
        );
    }

    @Test
    void changesXslt2ToXsltAttributeFromSpecifiedNameOnly(){
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveAttributeRecipe("configurationName","Configuration",null, null)),
          xml(
            """
                <Configuration configurationName="config">
                </Configuration>
            """,
            """
                <Configuration>
                </Configuration>
            """
          )
        );
    }

    @Test
    void notChangesXslt2ToXsltAttributeFromSpecifiedWrongNameOnly(){
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveAttributeRecipe("configurationName","Configuration",null, null)),
          xml(
            """
                <Configuration2 configurationName="config">
                </Configuration2>
            """
          )
        );
    }
    @Test
    void notChangesXslt2ToXsltAttributeFromSpecifiedWrongValueOnly(){
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveAttributeRecipe("configurationName","Configuration","config2", null)),
          xml(
            """
                <Configuration configurationName="config">
                </Configuration>
            """
          )
        );
    }

    @Test
    void changesXslt2ToXsltAttributeFromSpecifiedNameAndValueOnly(){
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveAttributeRecipe("configurationName","Configuration","config2", null)),
          xml(
            """
                <Configuration configurationName="config2">
                </Configuration>
            """,
            """
                <Configuration>
                </Configuration>
            """
          )
        );
    }

}
