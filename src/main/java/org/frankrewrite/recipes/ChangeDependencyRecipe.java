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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.frankrewrite.recipes.visitors.ChangeDependencyVisitor;
import org.openrewrite.*;

public class ChangeDependencyRecipe extends Recipe {
    @Option(displayName = "The old groupId value",
            description = "The old groupId to update in the pom file.",
            required = true)
    private String oldGroupId;
    @Option(displayName = "The old artifactId",
            description = "The old artifactId to update in the pom file.",
            required = true)
    private String oldArtifactId;
    @Option(displayName = "The new groupId",
            description = "The new groupId to update the old groupId to in the pom file.",
            required = true)
    private String newGroupId;
    @Option(displayName = "The new artifactId",
            description = "The new artifactId to update the old artifactId to in the pom file.",
            required = true)
    private String newArtifactId;
    @Option(displayName = "New version",
            description = "The version to update the dependency to.",
            required = false)
    private String version;

    public ChangeDependencyRecipe(@JsonProperty("oldGroupId")String oldGroupId, @JsonProperty("oldArtifactId")String oldArtifactId, @JsonProperty("newGroupId")String newGroupId, @JsonProperty("newArtifactId")String newArtifactId, @JsonProperty("version")String version) {
        this.oldGroupId = oldGroupId;
        this.oldArtifactId = oldArtifactId;
        this.newGroupId = newGroupId;
        this.newArtifactId = newArtifactId;
        this.version = version;
    }

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Change dependency recipe";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Recipe to refactor pom dependency names and optionally the corresponding version as well.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ChangeDependencyVisitor(oldGroupId, oldArtifactId, newGroupId, newArtifactId, version);
    }
}
