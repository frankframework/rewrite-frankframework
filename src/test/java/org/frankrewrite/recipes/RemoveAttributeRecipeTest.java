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
