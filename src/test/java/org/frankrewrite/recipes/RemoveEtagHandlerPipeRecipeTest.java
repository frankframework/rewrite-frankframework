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