package org.frankrewrite.recipes;

import org.frankrewrite.recipes.visitors.RemoveInvalidChildrenVisitor;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class RemoveInvalidChildrenRecipe extends Recipe {
    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Remove random children";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Remove random children from element only accepting elements.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveInvalidChildrenVisitor();
    }
}
