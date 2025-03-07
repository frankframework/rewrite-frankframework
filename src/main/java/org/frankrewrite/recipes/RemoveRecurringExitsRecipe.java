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

import org.frankrewrite.recipes.scanresults.ExitScanResult;
import org.frankrewrite.recipes.visitors.ExitScanningVisitor;
import org.frankrewrite.recipes.visitors.RemoveRecurringExitsVisitor;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;

public class RemoveRecurringExitsRecipe extends ScanningRecipe<ExitScanResult> {

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Remove recurring exits";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Removes recurring exit tags with the same code value.";
    }

    @Override
    public @NotNull ExitScanResult getInitialValue(ExecutionContext ctx) {
        return new ExitScanResult();
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getScanner(ExitScanResult acc) {
        return new ExitScanningVisitor(acc);
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor(ExitScanResult acc) {
        return new RemoveRecurringExitsVisitor(acc);
    }

}

