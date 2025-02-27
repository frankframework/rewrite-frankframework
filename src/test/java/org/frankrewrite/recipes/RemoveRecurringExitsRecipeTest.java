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

public class RemoveRecurringExitsRecipeTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RemoveRecurringExitsRecipe());
    }

    @Test
    public void testSingleExitIsNotRemoved() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
            <exit name="exit1" code="403"/>
            """)
        );
    }
    @Test
    public void testSingleExitIsRemovedAndForwardPathValueChanged() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
            <root>
                <adapter name='adapter1'>
                    <pipeline>
                        <exits>
                            <exit code="403" path="exit1"/>
                            <exit path="exit2" code="403"/>
                        </exits>
                        <FixedResultPipe
                            name="NotActivatedAccount"
                            returnString='{ "error" : "Your account associated with your email address has not been activated yet. Is your activation code expired? Try requesting a new activation code." } '
                        >
                            <forward name="success" path="exit2" />
                        </FixedResultPipe>
                    </pipeline>
                </adapter>
                <adapter name='adapter2'>
                    <pipeline>
                        <exits>
                            <exit code="403" path="exit2"/>
                            <exit code="403" path="exit1"/>
                        </exits>
                        <FixedResultPipe
                            name="NotActivatedAccount"
                            returnString='{ "error" : "Your account associated with your email address has not been activated yet. Is your activation code expired? Try requesting a new activation code." } '
                        >
                            <forward name="success" path="exit1" />
                            <forward name="success" path="exit2" />
                        </FixedResultPipe>
                    </pipeline>
                </adapter>
            </root>
            """,
            """
            <root>
                <adapter name='adapter1'>
                    <pipeline>
                        <exits>
                            <exit code="403" path="exit1"/>
                        </exits>
                        <FixedResultPipe
                            name="NotActivatedAccount"
                            returnString='{ "error" : "Your account associated with your email address has not been activated yet. Is your activation code expired? Try requesting a new activation code." } '
                        >
                            <forward name="success" path="exit1" />
                        </FixedResultPipe>
                    </pipeline>
                </adapter>
                <adapter name='adapter2'>
                    <pipeline>
                        <exits>
                            <exit code="403" path="exit2"/>
                        </exits>
                        <FixedResultPipe
                            name="NotActivatedAccount"
                            returnString='{ "error" : "Your account associated with your email address has not been activated yet. Is your activation code expired? Try requesting a new activation code." } '
                        >
                            <forward name="success" path="exit2" />
                            <forward name="success" path="exit2" />
                        </FixedResultPipe>
                    </pipeline>
                </adapter>
            </root>
            """)
        );
    }

}
