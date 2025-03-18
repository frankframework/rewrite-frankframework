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
