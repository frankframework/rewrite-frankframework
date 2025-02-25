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

import org.frankrewrite.recipes.visitors.WarningAnnotationUpdaterVisitor;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class WarningAnnotationUpdaterRecipe extends Recipe {

    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Update pipes recipe";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Updates pipes' attribute names and/or tag name.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new WarningAnnotationUpdaterVisitor();
    }
}
