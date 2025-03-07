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

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

public class FrameworkVersionRecipeTest implements RewriteTest {

    @Test
    public void changeVersionBothFiles() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new FrameworkVersionRecipe("7.7")),
          xml("""
            <iaf.version>7.6</iaf.version>""","""
            <iaf.version>7.7</iaf.version>"""),
          properties("""
            ff.version=7.6""", """
            ff.version=7.7""")
        );

    }
    @Test
    public void notChangesPropertyWhenKeyIsNotMatching() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new FrameworkVersionRecipe("7.7")),
          xml("""
            <random>7.6</random>"""),
          properties("""
            random=7.6"""
          )
        );

    }
    @Test
    public void notChangesYamlFile() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new FrameworkVersionRecipe("7.7")),
          xml("""
            <ff.version>7.6</ff.version>""","""
            <ff.version>7.7</ff.version>"""),
          yaml("""
            ff.version=7.6""")
        );
    }
    @Test
    public void notChangesEmptyTagValue() {
        rewriteRun(recipeSpec -> recipeSpec.recipe(new FrameworkVersionRecipe("7.7")),
          xml("""
            <ff.version/>""")
        );
    }

}
