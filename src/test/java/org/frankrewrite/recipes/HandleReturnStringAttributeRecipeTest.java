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

public class HandleReturnStringAttributeRecipeTest implements RewriteTest {
    @Test
    void IntroducesEchoPipeAndUpdatesPathStringsAccordingly() {
        rewriteRun(specs->specs.recipe(new HandleReturnStringAttributeRecipe()),
          //language=xml
          xml(
            """
                <pipeline>
                dfghdgfh
                    <Exits>
                        <Exit code='200' path='READY'/>
                    </Exits>
                    <FixedResultPipe
                      name="noBacklogitems"
                      returnString='{"backlogitems": []}'
                    >
                    dfghdgfh
                      <test name="success" path="myEchoPipe" />
                      <forward name="success" path="READY" />
                    </FixedResultPipe>
                    <FixedResultPipe
                      name="noBacklogitemsSecond"
                      returnString='{"backlogitems": []}'
                    >
                      <forward name="success" path="READY" />
                    </FixedResultPipe>
                    <test/>
                </pipeline>
                """,
            """
                <pipeline>
                dfghdgfh
                    <Exits>
                        <Exit code='200' path='READY'/>
                    </Exits>
                    <FixedResultPipe
                      name="noBacklogitems"
                    >
                    dfghdgfh
                      <test name="success" path="myEchoPipe" />
                      <forward name="success" path="myEchoPipe" />
                    </FixedResultPipe><EchoPipe name="myEchoPipe" getInputFromFixedValue='{"backlogitems": []}'><forward name="success" path="READY"/></EchoPipe>
                    <FixedResultPipe
                      name="noBacklogitemsSecond"
                    >
                      <forward name="success" path="myEchoPipe2" />
                    </FixedResultPipe><EchoPipe name="myEchoPipe2" getInputFromFixedValue='{"backlogitems": []}'><forward name="success" path="READY"/></EchoPipe>
                    <test/>
                </pipeline>
                """
          )
        );
    }
}
