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

public class MoveReturnedSessionsKeyRecipeTest implements RewriteTest {
    @Test
    public void testReturnedSessionKeysNotRemovedWhenListenerPresent(){
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
    @Test
    public void testReturnedSessionKeysNotRemovedWhenListenerNotPresent(){
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new MoveReturnedSessionKeysRecipe()),
          xml(
            """
            <Adapter>
                <Receiver
                    name="forEachTag"
                    returnedSessionKeys="backlogItem,team_Id,OriginalXML"
                >
                </Receiver>
            </Adapter>
            """
          )
        );
    }
}
