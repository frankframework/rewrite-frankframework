package org.frankrewrite.recipes;

import org.frankrewrite.recipes.visitors.ParameterizedActionsVisitor;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class ParameterizedActionsRecipe extends Recipe {
    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Parameterized actions recipe";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Make action parameters in element as child from actions attribute.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ParameterizedActionsVisitor();
    }
}
