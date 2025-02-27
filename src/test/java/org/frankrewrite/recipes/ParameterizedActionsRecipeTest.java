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
