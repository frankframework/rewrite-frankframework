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

public class ParameterizedActionsRecipeTest implements RewriteTest{
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ParameterizedActionsRecipe());
    }

    @Test
    void parameterizeMultipleActions() {
        rewriteRun(
                xml(
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                        outputType="base64"
                        actions="read_delete"
                    >
                        <forward name="exception" path="ServerError" />
                        <forward name="success" path="CreateMail" />
                    </LocalFileSystemPipe>
                    """,
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                        outputType="base64"
                    >
                        <forward name="exception" path="ServerError" />
                        <forward name="success" path="CreateMail" />
                        <Param name="action" value="read"/>
                        <Param name="action" value="delete"/>
                    </LocalFileSystemPipe>
                    """
                )
        );
    }
    @Test
    void parameterizeSingleAction() {
        rewriteRun(
                xml(
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                        outputType="base64"
                        actions="read"
                    >
                        <forward name="exception" path="ServerError" />
                        <forward name="success" path="CreateMail" />
                    </LocalFileSystemPipe>
                    """,
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                        outputType="base64"
                    >
                        <forward name="exception" path="ServerError" />
                        <forward name="success" path="CreateMail" />
                        <Param name="action" value="read"/>
                    </LocalFileSystemPipe>
                    """
                )
        );
    }

}
