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
