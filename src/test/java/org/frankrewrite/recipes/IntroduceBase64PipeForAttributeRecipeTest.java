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
                    base64="DECODE"
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
                    <forward name="success" path="OpenZipStreamDecoder" />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe><Base64Pipe name="OpenZipStreamDecoder" direction="DECODE" storeResultInSessionKey="base64zip">
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
    void dontChangeInvalidPipeLineChildren() {
        //language=xml
        rewriteRun(recipeSpec -> recipeSpec.recipe(new IntroduceBase64PipeForAttributeRecipe()),
          xml(
            """
             <pipeline>
             dsfgdfg
                <LocalFileSystemPipe
                >
                    <forward path="ServerError" />
                    <Param name="action" value="read"/>
                    <Param name="action" value="delete"/>
                </LocalFileSystemPipe>
             </pipeline>"""
          )
        );
    }
}
