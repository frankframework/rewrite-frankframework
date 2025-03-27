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

import org.frankrewrite.recipes.visitors.RemoveEtagHandlerPipeVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class RemoveEtagHandlerPipeRecipe extends Recipe {
    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Remove EtagHandlerPipe recipe";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Removes EtagHandlerPipe recipe from the pipeline and update the ApiListener and forwards accordingly.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveEtagHandlerPipeVisitor();
    }

}
