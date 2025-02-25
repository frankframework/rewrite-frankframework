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

package org.frankrewrite;

import org.frankrewrite.recipes.WarningAnnotationUpdaterRecipe;
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
    void changesDeprecatedElementNameAndAttributes() {
        rewriteRun(recipeSpec -> recipeSpec.cycles(2),
          xml(
            """
                <FileLineIteratorPipe charset="utf-8"/>
            """,
            """
                <StreamLineIteratorPipe charset="utf-8"/>
            """
          )
        );
    }

    @Test
    void changesDeprecatedElementAttribute1() {
        rewriteRun(
          xml(
            """
                <PutParametersInSessionPipe
                    name="PutPayloadInSession"
                    preserveInput="true"
                >
                    <param name="payload" 		sessionKey="OriginalRequest" xpathExpression="request/payload" defaultValue="null"/>
                    <param name="javaListener"  sessionKey="OriginalRequest" xpathExpression="request/javaListener"/>
            
                    <forward name="success" path="ChooseMethod"/>
                    <forward name="exception" path="ServerError" />
                </PutParametersInSessionPipe>
            """,
            """
                <PutInSessionPipe
                    name="PutPayloadInSession"
                    preserveInput="true"
                >
                    <param name="payload" 		sessionKey="OriginalRequest" xpathExpression="request/payload" defaultValue="null"/>
                    <param name="javaListener"  sessionKey="OriginalRequest" xpathExpression="request/javaListener"/>
            
                    <forward name="success" path="ChooseMethod"/>
                    <forward name="exception" path="ServerError" />
                </PutInSessionPipe>
            """
          )
        );
    }

    @Test
    void changesDeprecatedElementAttribute() {
        rewriteRun(recipeSpec -> recipeSpec.cycles(1),
          xml(
            """
                <XmlSwitch serviceSelectionStylesheetFilename="x">

                </XmlSwitch>
            """,
            """
                <XmlSwitch styleSheetName="x">

                </XmlSwitch>
            """
          )
        );
    }
    @Test
    void changesDeprecatedElementAttributes() {
        rewriteRun(recipeSpec -> recipeSpec.cycles(1),
          xml(
            """
                <XmlSwitch serviceSelectionStylesheetFilename="x">

                </XmlSwitch>
            """,
            """
                <XmlSwitch styleSheetName="x">

                </XmlSwitch>
            """
          )
        );
    }
}
