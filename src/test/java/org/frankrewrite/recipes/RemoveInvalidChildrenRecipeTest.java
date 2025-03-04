package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class RemoveInvalidChildrenRecipeTest implements RewriteTest {
    @Test
    public void removesInvalidPipelineChildren() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveInvalidChildrenRecipe()),
            xml("""
              <Pipeline>&ijfoj;<doodoo/>ioijfoijfo</Pipeline>""", """
              <Pipeline>&ijfoj;<doodoo/></Pipeline>""")
        );
    }
    @Test
    public void removesInvalidAdapterChildren() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveInvalidChildrenRecipe()),
            xml("""
              <adapter>&ijfoj;<doodoo/>ioijfoijfo</adapter>""", """
              <adapter>&ijfoj;</adapter>""")
        );
    }
    @Test
    public void removesInvalidConfigurationChildren() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveInvalidChildrenRecipe()),
            xml("""
              <configuration>&ijfoj;<doodoo/>ioijfoijfo</configuration>""", """
              <configuration>&ijfoj;<doodoo/></configuration>""")
        );
    }
}
