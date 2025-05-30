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

import org.frankrewrite.recipes.visitors.IntroduceReplacerPipeForFixedResultPipeVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.xml.XmlIsoVisitor;

public class IntroduceReplacerPipeForFixedResultPipeRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Introduce ReplacerPipe and update path strings";
    }

    @Override
    public String getDescription() {
        return "Replaces replaceFrom/To attributes in FixedResultPipe with an ReplacerPipe and updates path references.";
    }

    @Override
    public XmlIsoVisitor<ExecutionContext> getVisitor() {
        return new IntroduceReplacerPipeForFixedResultPipeVisitor() ;
    }
}
