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

public class WarningAnnotationReaderTest implements RewriteTest{
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new WarningAnnotationUpdaterRecipe());
    }

    @Test
    void changesDeprecatedElementName() {
        rewriteRun(
          xml(
            """
                <MyPipeTwo />
            """,
            """
                <MySecondPipe />
            """
          )
        );
    }

    @Test
    void changesDeprecatedElementAttribute() {
        rewriteRun(recipeSpec -> recipeSpec.cycles(1),
          xml(
            """
                <MySecondPipe pipeAttribute="x">

                </MySecondPipe>
            """,
            """
                <MySecondPipe myPipeAttribute="x">

                </MySecondPipe>
            """
          )
        );
    }
    @Test
    void dontChangeDeprecatedElementNamesForInvalidWarnings() {
        rewriteRun(
          xml(
            """
                <MyPipe>

                </MyPipe>
            """
          )
        );
    }
    @Test
    void changesGenericReceiverToReceiver() {
        rewriteRun(
          xml(
            """
            <GenericReceiver>
            </GenericReceiver>
            """, """
            <Receiver>
            </Receiver>
            """
          )
        );
    }
    @Test
    void changesAttributeKeysWhenCapitalizationIsUpdated() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.cycles(1),
          xml(
            """
            <FixedErrorMessage fileName=''>
            </FixedErrorMessage>
            """, """
            <FixedErrorMessage filename=''>
            </FixedErrorMessage>
            """
          )
        );
    }
    @Test
    void dontCapitalizeDotAnnotation() {
        //language=xml
        rewriteRun(
          xml(
            """
            <EmptyAnnotatedClass test=''>
            </EmptyAnnotatedClass>
            """
          )
        );
    }

}
