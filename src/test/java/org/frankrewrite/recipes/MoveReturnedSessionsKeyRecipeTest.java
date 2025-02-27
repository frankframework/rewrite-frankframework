package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class MoveReturnedSessionsKeyRecipeTest implements RewriteTest {
    @Test
    public void test(){
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new MoveReturnedSessionKeysRecipe()),
          xml(
            """
            <Adapter>
                <Receiver
                    name="forEachTag"
                    returnedSessionKeys="backlogItem,team_Id,OriginalXML"
                >
                    <JavaListener
                        name="forEachTag"
                    />
                </Receiver>
            </Adapter>
            ""","""
            <Adapter>
                <Receiver
                    name="forEachTag"
                >
                    <JavaListener
                        name="forEachTag"
                    returnedSessionKeys="backlogItem,team_Id,OriginalXML"
                    />
                </Receiver>
            </Adapter>
            """
          )
        );
    }
}
