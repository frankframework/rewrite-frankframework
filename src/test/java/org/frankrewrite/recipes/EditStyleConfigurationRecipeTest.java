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

public class EditStyleConfigurationRecipeTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new EditStyleConfigurationRecipe());
    }

    @Test
    void ignoresErrorStorageAndMessageLogTags() {
        rewriteRun(
          xml(
            """
            <errorStorage/>
            """
          ),
          xml(
            """
            <messageLog/>
            """
          )
        );
    }

    @Test
    void updatesTagBasedOnClassNameAttributeWithTypeExtention() {
        rewriteRun(
          xml(
            """
            <SomeTag className="org.frankframework.pipes.TheBest"/>
            """,
            """
            <TheBestPipe/>
            """
          )
        );
    }
    @Test
    void updatesTagBasedOnClassNameAttributeForFrankFramework() {
        rewriteRun(
          xml(
            """
            <SomeTag className="org.frankframework.pipes.MyPipe"/>
            """,
            """
            <MyPipe/>
            """
          )
        );
    }
    @Test
    void updatesNotPipeBasedOnClassNameAttribute() {
        rewriteRun(
          xml(
            """
            <SomeTag className="nl.nn.adapterframework.senders.MyNot"/>
            """,
            """
            <MyNot/>
            """
          )
        );
    }
    @Test
    void updatesTagBasedOnClassNameAttributeForAdapterFramework() {
        rewriteRun(
          xml(
            """
            <SomeTag className="nl.nn.adapterframework.pipes.MyPipe"/>
            """,
            """
            <MyPipe/>
            """
          )
        );
    }

    @Test
    void doesNotChangeCustomElements() {
        rewriteRun(
          xml(
            """
            <CustomTag className="com.custom.package.CustomClass"/>
            """
          )
        );
    }

    @Test
    void renamesJobBasedOnFunctionAttribute() {
        rewriteRun(
          xml(
            """
            <job function="myFunction"/>
            """,
            """
            <myFunctionJob/>
            """
          )
        );
    }

    @Test
    void keepsJobTagWhenNoFunctionAttribute() {
        rewriteRun(
          xml(
            """
            <job/>
            """
          )
        );
    }

    @Test
    void doesNotChangeTagWithoutClassNameOrFunction() {
        rewriteRun(
          xml(
            """
            <SomeOtherTag/>
            """
          )
        );
    }
    @Test
    void doesNotChangeTagWithWrongClassNamePackage() {
        rewriteRun(
          xml(
            """
            <pipe className="org.nonexistingorg.pipes.TheBest"/>
            """
          )
        );
    }
}
