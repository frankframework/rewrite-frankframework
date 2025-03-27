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

public class IntroduceBase64PipeForAttributeRecipeTest implements RewriteTest {
    @Test
    void introduceBase64Pipe() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new IntroduceBase64PipeForAttributeRecipe()),
          xml(
            """
             <pipeline>
                <LocalFileSystemPipe
                    name="OpenZipStream"
                    storeResultInSessionKey="base64zip"
                    base64="ENCODE"
                >
                    <forward name="exception" path="ServerError" />
                    <forward name="success" path="CreateMail" />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe>
             </pipeline>""","""
             <pipeline>
                <LocalFileSystemPipe
                    name="OpenZipStream"
                    storeResultInSessionKey="base64zip"
                >
                    <forward name="exception" path="ServerError" />
                    <forward name="success" path="OpenZipStreamEncoder" />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe><Base64Pipe name="OpenZipStreamEncoder" direction="ENCODE" storeResultInSessionKey="base64zip">
                    <forward name="exception" path="ServerError"/>
                    <forward name="success" path="CreateMail"/>
                </Base64Pipe>
             </pipeline>"""
          )
        );
    }
    @Test
    void dontAddNonExistingForwardsToBase64Pipe() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new IntroduceBase64PipeForAttributeRecipe()),
          xml(
            """
             <pipeline>
                <LocalFileSystemPipe
                    name="OpenZipStream"
                    storeResultInSessionKey="base64zip"
                    base64="DECODE"
                >
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe>
             </pipeline>""","""
             <pipeline>
                <LocalFileSystemPipe
                    name="OpenZipStream"
                    storeResultInSessionKey="base64zip"
                >
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe><Base64Pipe name="OpenZipStreamDecoder" direction="DECODE" storeResultInSessionKey="base64zip">
                </Base64Pipe>
             </pipeline>"""
          )
        );
    }
    @Test
    void dontAddInvalidForwardsToBase64Pipe() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new IntroduceBase64PipeForAttributeRecipe()),
          xml(
            """
             <pipeline>
                <LocalFileSystemPipe
                    name="OpenZipStream"
                    storeResultInSessionKey="base64zip"
                    base64="DECODE"
                >
                    <forward path="ServerError" />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe>
             </pipeline>""","""
             <pipeline>
                <LocalFileSystemPipe
                    name="OpenZipStream"
                    storeResultInSessionKey="base64zip"
                >
                    <forward path="ServerError" />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe><Base64Pipe name="OpenZipStreamDecoder" direction="DECODE" storeResultInSessionKey="base64zip">
                </Base64Pipe>
             </pipeline>"""
          )
        );
    }
    @Test
    void dontChangeChildrenForParentWithMissingNameAttribute() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new IntroduceBase64PipeForAttributeRecipe()),
          xml(
            """
             <pipeline>
             dsfgdfg
                <LocalFileSystemPipe
                    base64="DECODE"
                    storeResultInSessionKey="base64zip"
                >
                    <forward path="ServerError" />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe>
             </pipeline>"""
          )
        );
    }
    @Test
    void dontChangeChildrenForParentWithMissingStoreResultInSessionKeyAttribute() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new IntroduceBase64PipeForAttributeRecipe()),
          xml(
            """
             <pipeline>
             dsfgdfg
                <LocalFileSystemPipe
                 base64="DECODE"
                 name="OpenZipStream"
                >
                    <forward path="ServerError" />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe>
             </pipeline>"""
          )
        );
    }    @Test
    void dontAddMissingChildrenForParentWithMissingSuccessResultChild() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new IntroduceBase64PipeForAttributeRecipe()),
          xml(
            """
             <pipeline>
             dsfgdfg
                <LocalFileSystemPipe
                    name="OpenZipStream"
                    storeResultInSessionKey="base64zip"
                    base64='DECODE'
                >
                    <forward name='success'/> />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe>
             </pipeline>""","""
             <pipeline>
             dsfgdfg
                <LocalFileSystemPipe
                    name="OpenZipStream"
                    storeResultInSessionKey="base64zip"
                >
                    <forward name='success'/> />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe><Base64Pipe name="OpenZipStreamDecoder" direction="DECODE" storeResultInSessionKey="base64zip">
                </Base64Pipe>
             </pipeline>"""
          )
        );
    }
}
