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
              <adapter>&ijfoj;<xFormatter/><pipeline/><receiver/><doodoo/>ioijfoijfo</adapter>""", """
              <adapter>&ijfoj;<xFormatter/><pipeline/><receiver/></adapter>""")
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
