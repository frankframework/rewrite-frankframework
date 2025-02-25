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

import org.frankrewrite.recipes.EditStyleConfigurationRecipe;
import org.frankrewrite.recipes.WarningAnnotationUpdaterRecipe;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class EditStyleConfigurationRecipeTest implements RewriteTest{
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new EditStyleConfigurationRecipe());
    }

    @Test
    void changesElementStyle() {
        rewriteRun(
                xml(
                        """
                            <pipe name="Test" className="org.frankframework.XsltPipe">
                        
                            </pipe>
                        """,
                        """
                            <XsltPipe name="Test">
                        
                            </XsltPipe>
                        """
                )
        );
    }
    @Test
    void changesElementStyleAndHandlesMessageSendingPipeDerivatives() {
        rewriteRun(
                xml(
                        """
                            <pipe name="Test" className="org.frankframework.pipes.Json2XmlValidator">
                        
                            </pipe>
                        """,
                        """
                            <Json2XmlValidatorPipe name="Test">
                        
                            </Json2XmlValidatorPipe>
                        """
                )
        );
    }
    @Test
    void doesNotChangeCustomElementStyle() {
        rewriteRun(
                xml(
                        """
                            <pipe name="Test" className="org.frankframework.Kaas">
                        
                            </pipe>
                        """
                )
        );
    }

    @Test
    void changesSender() {
        rewriteRun(
                xml(
                        """
                            <sender className="nl.nn.adapterframework.jdbc.FixedQuerySender">
                        
                            </sender>
                        ""","""
                            <FixedQuerySender>
                        
                            </FixedQuerySender>
                        """
                )
        );
    }
    @Test
    void changesLarvaPipe() {
        rewriteRun(recipeSpec -> recipeSpec.recipes(new EditStyleConfigurationRecipe(),new WarningAnnotationUpdaterRecipe()),
                xml(
                        """
                            <pipe className="nl.nn.adapterframework.pipes.LarvaPipe">
                        
                            </pipe>
                        ""","""
                            <LarvaPipe>
                        
                            </LarvaPipe>
                        """
                )
        );
    }

    @Test
    void dontChangeErrorStorage() {
        rewriteRun(recipeSpec -> recipeSpec.recipes(new EditStyleConfigurationRecipe(),new WarningAnnotationUpdaterRecipe()),
                xml(
                """
                <errorStorage
                    className="nl.nn.adapterframework.jdbc.JdbcTransactionalStorage"
                    jmsRealm="${jdbc.realm}"
                    slotId="mailSender"
                    schemaOwner4Check="current schema"
                />
                """
                )
        );
    }

    @Test
    void changesJob() {
        rewriteRun(
                xml(
                        """
                          <scheduler>
                            <job
                                name="Delete Demo Accounts"
                                cronExpression="0 0 1 * * ?"
                                description="Run every day at a certain time to Delete Demo Accounts"
                                function="SendMessage"
                                receiverName="DemoTeamGetter"
                                adapterName="DemoTeamGetter"
                            />
                        </scheduler>
                        ""","""
                          <scheduler>
                            <SendMessageJob
                                name="Delete Demo Accounts"
                                cronExpression="0 0 1 * * ?"
                                description="Run every day at a certain time to Delete Demo Accounts"
                                receiverName="DemoTeamGetter"
                                adapterName="DemoTeamGetter"
                            />
                        </scheduler>
                        """
                )
        );
    }
}
