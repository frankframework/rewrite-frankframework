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
