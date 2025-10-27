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

public class ConvertFilePipeRecipeTest implements RewriteTest{
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ConvertFilePipeRecipe());
    }

    @Test
    void replacefileNameSessionKey() {
        rewriteRun(
                xml(
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                        outputType="base64"
                        fileNameSessionKey="outputFileName"
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
                        <Param name="filename" sessionKey="outputFileName"/>
                    </LocalFileSystemPipe>
                    """
                )
        );
    }

    @Test
    void replaceDirecoryAndFileNameCombination() {
        rewriteRun(
                xml(
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                        directory="${APPSERVER_ROOT_DIR}"
                        filename="test.txt"
                    >
                        <forward name="exception" path="ServerError" />
                        <forward name="success" path="CreateMail" />
                    </LocalFileSystemPipe>
                    """,
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                    >
                        <forward name="exception" path="ServerError" />
                        <forward name="success" path="CreateMail" />
                        <Param name="filename" value="${APPSERVER_ROOT_DIR}/test.txt"/>
                    </LocalFileSystemPipe>
                    """
                )
        );
    }

    @Test
    void replaceDirecoryAndFileNameSessionKeyCombination() {
        rewriteRun(
                xml(
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                        directory="${APPSERVER_ROOT_DIR}"
                        fileNameSessionKey="outputFileName"
                    >
                        <forward name="exception" path="ServerError" />
                        <forward name="success" path="CreateMail" />
                    </LocalFileSystemPipe>
                    """,
                    """
                    <LocalFileSystemPipe
                        name="OpenZipStream"
                        storeResultInSessionKey="base64zip"
                    >
                        <forward name="exception" path="ServerError" />
                        <forward name="success" path="CreateMail" />
                        <Param name="filename" xpathExpression="concat($directory, '/', $fileNameSessionKey)">
                            <Param name="directory" value="${APPSERVER_ROOT_DIR}"/>
                            <Param name="fileNameSessionKey" sessionKey="outputFileName"/></Param>
                    </LocalFileSystemPipe>
                    """
                )
        );
    }
}
