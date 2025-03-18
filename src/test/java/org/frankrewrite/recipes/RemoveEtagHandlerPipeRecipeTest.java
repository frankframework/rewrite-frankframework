package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

class RemoveEtagHandlerPipeRecipeTest implements RewriteTest {
    @Test
    void removeEtagHandlerPipeAndUpdatePathsAndApiListenerAttributes() {
        //language=xml
        rewriteRun(recipespec->recipespec.recipe(new RemoveEtagHandlerPipeRecipe()),
          xml(
            """
            <root>
            asdf
                <pipeline>
                asdf
                    <ApiListener>
                    </ApiListener>
                    <pipe>
                    asdf
                        <random/>
                        <forward name='success' path='path1'/>
                    </pipe>
                    <EtagHandlerPipe name='path1'>
                    asdf
                        <random/>
                        <forward name='success' path='path2'/>
                        <forward name='successsss' path='pathWrong'/>
                    </EtagHandlerPipe>
                </pipeline>
            </root>""","""
            <root>
            asdf
                <pipeline>
                asdf
                    <ApiListener updateEtag='true'>
                    </ApiListener>
                    <pipe>
                    asdf
                        <random/>
                        <forward name='success' path='path2'/>
                    </pipe>
                </pipeline>
            </root>"""
          )
        );
    }
    @Test
    void dontUpdateIfApiListenerIsNotPresent() {
        //language=xml
        rewriteRun(recipespec->recipespec.recipe(new RemoveEtagHandlerPipeRecipe()),
          xml(
            """
            <root>
                <pipeline>
                    <pipe>
                        <forward name='success' path='path1'/>
                    </pipe>
                    <EtagHandlerPipe name='path1'>
                        <forward name='success' path='path2'/>
                    </EtagHandlerPipe>
                </pipeline>
            </root>"""
          )
        );
    }
    @Test
    void dontUpdateIfPipelineSelfCloses() {
        //language=xml
        rewriteRun(recipespec->recipespec.recipe(new RemoveEtagHandlerPipeRecipe()),
          xml(
            """
            <root>
                <pipeline/>
            </root>"""
          )
        );
    }
    @Test
    void dontUpdateIfForwardPathDoesNotMatchEtagHandlerName() {
        //language=xml
        rewriteRun(recipespec->recipespec.recipe(new RemoveEtagHandlerPipeRecipe()),
          xml(
            """
            <root>
                <pipeline>
                    <ApiListener />
                    <pipe>
                        <forward name='success' path='differentPath'/>
                    </pipe>
                    <pipe>
                        <forward name='success' path='path1'/>
                    </pipe>
                    <EtagHandlerPipe name='path1'>
                        <forward name='success' path='path2'/>
                    </EtagHandlerPipe>
                </pipeline>
            </root>""",
            """
            <root>
                <pipeline>
                    <ApiListener updateEtag='true' />
                    <pipe>
                        <forward name='success' path='differentPath'/>
                    </pipe>
                    <pipe>
                        <forward name='success' path='path2'/>
                    </pipe>
                </pipeline>
            </root>"""
          )
        );
    }


}