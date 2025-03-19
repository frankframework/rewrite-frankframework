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
                asdf
                    <pipeline>
                    asdf
                        <exits>
                            <exit code="403" path="exit1"/>
                            <exit path="exit2" code="403"/>
                        </exits>
                        <FixedResultPipe
                            name="NotActivatedAccount"
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
                        >
                            <forward name="success" path="exit1" />
                            <forward name="success" path="exit2" />
                        </FixedResultPipe>
                    </pipeline>
                    <random/>
                </adapter>
            </root>
            """,
            """
            <root>
                <adapter name='adapter1'>
                asdf
                    <pipeline><exits>
                            <exit code="403" path="exit1"/>
                        </exits>
                    asdf
                       \s
                        <FixedResultPipe
                            name="NotActivatedAccount"
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
                        >
                            <forward name="success" path="exit2" />
                            <forward name="success" path="exit2" />
                        </FixedResultPipe>
                    </pipeline>
                    <random/>
                </adapter>
            </root>
            """)
        );
    }
    @Test
    public void notChangesExitsNotPresent() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
            <root>
                <adapter name='adapter1'>
                    <pipeline>
                        <exits>
                        </exits>
                        <FixedResultPipe
                            name="NotActivatedAccount"
                        >
                            <forward name="success" path="exit2" />
                        </FixedResultPipe>
                    </pipeline>
                </adapter>
                <adapter name='adapter2'>
                    <pipeline>
                        <exits>
                        </exits>
                        <FixedResultPipe
                            name="NotActivatedAccount"
                        >
                            <forward name="success" path="exit1" />
                            <forward name="success" path="exit2" />
                        </FixedResultPipe>
                    </pipeline>
                </adapter>
            </root>
            """)
        );
    }
    @Test
    public void testMultipleRecurringExitsAreRemoved() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <exit code="403" path="exit1"/>
                        <exit code="403" path="exit2"/>
                        <exit code="403" path="exit3"/>
                    </exits>
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
                </pipeline>
            </adapter>
        </root>
        """)
        );
    }

    @Test
    public void testNoChangeWhenNoRecurringExits() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <exit code="200" path="exit1"/>
                        <exit code="404" path="exit2"/>
                    </exits>
                </pipeline>
            </adapter>
        </root>
        """)
        );
    }

    @Test
    public void testExitRemovalRetainsUniqueCodes() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <exit code="500" path="exit1"/>
                        <exit code="500" path="exit1"/>
                        <exit code="200" path="exit2"/>
                    </exits>
                </pipeline>
            </adapter>
        </root>
        """,
            """
            <root>
                <adapter name='adapter1'>
                    <pipeline>
                        <exits>
                            <exit code="500" path="exit1"/>
                            <exit code="200" path="exit2"/>
                        </exits>
                    </pipeline>
                </adapter>
            </root>
            """)
        );
    }

    @Test
    public void testExitForwardPathIsUpdatedWhenExitIsRemoved() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <exit code="403" path="exit1"/>
                        <exit code="403" path="exit2"/>
                    </exits>
                    <FixedResultPipe name="TestPipe">
                        <forward name="success" path="exit2"/>
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
                        <FixedResultPipe name="TestPipe">
                            <forward name="success" path="exit1"/>
                        </FixedResultPipe>
                    </pipeline>
                </adapter>
            </root>
            """)
        );
    }
    @Test
    public void testNoExitTagsPresent() {
        // Covers case where <exits> tag is missing
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                </pipeline>
            </adapter>
        </root>
        """)
        );
    }

    @Test
    public void testEmptyExitsTagDoesNotCauseIssues() {
        // Covers case where <exits> tag exists but is empty
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits></exits>
                </pipeline>
            </adapter>
        </root>
        """)
        );
    }

    @Test
    public void testExitTagWithoutCodeAttributeIsNotRemoved() {
        // Covers case where an <exit> tag is missing "code" attribute
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <exit path="exit1"/>
                        <exit path="exit2" code="403"/>
                    </exits>
                </pipeline>
            </adapter>
        </root>
        """)
        );
    }

    @Test
    public void testExitTagWithDifferentCaseNames() {
        // Covers case where <exit> tag names use different cases ("Exit" vs "exit")
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <Exit code="403" path="exit1"/>
                        <exit code="403" path="exit2"/>
                    </exits>
                </pipeline>
            </adapter>
        </root>
        """,
        """
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <Exit code="403" path="exit1"/>
                    </exits>
                </pipeline>
            </adapter>
        </root>
        """)
        );
    }    @Test
    public void testRemovesNonExitContentInExitsTag() {
        // Covers case where <exit> tag names use different cases ("Exit" vs "exit")
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        dfghfdg
                        <Exit code="403" path="exit1"/>
                        <exit code="403" path="exit2"/>
                    </exits>
                </pipeline>
            </adapter>
        </root>
        """,
        """
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits><Exit code="403" path="exit1"/>
                    </exits>
                </pipeline>
            </adapter>
        </root>
        """)
        );
    }

    @Test
    public void testDontRemoveNonTagContentInAdapterAndPipeline() {
        // Covers case where <exit> tag names use different cases ("Exit" vs "exit")
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <Exit code="403" path="exit1"/>
                        <exit code="403" path="exit2"/>
                    </exits>
                    testcontent
                </pipeline>
                testcontent
            </adapter>
        </root>
        ""","""
        <root>
            <adapter name='adapter1'>
                <pipeline>
                    <exits>
                        <Exit code="403" path="exit1"/>
                    </exits>
                    testcontent
                </pipeline>
                testcontent
            </adapter>
        </root>
        """)
        );
    }

    @Test
    public void testAdapterWithoutContent() {
        // Covers case where <exit> tag names use different cases ("Exit" vs "exit")
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
            </adapter>
        </root>
        """)
        );
    }
    @Test
    public void testDontUpdateExitsWithoutCodeAttribute() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new RemoveRecurringExitsRecipe()),
          xml("""
        <root>
            <adapter name='adapter1'>
                <exits>
                    <exit path="exit1"/>
                </exits>
            </adapter>
        </root>
        """)
        );
    }
}
