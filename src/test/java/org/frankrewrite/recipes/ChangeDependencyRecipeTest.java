package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class ChangeDependencyRecipeTest implements RewriteTest {

    @Test
    void changeDependency() {
        String oldGroupId = "com.old";
        String oldArtifactId = "old-artifact";
        String newGroupId = "com.new";
        String newArtifactId = "new-artifact";
        String newVersion = "2.0.0";

        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new ChangeDependencyRecipe(oldGroupId, oldArtifactId, newGroupId, newArtifactId, newVersion)),

          // language=xml
          xml(
            """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.old</groupId>
                        <artifactId>old-artifact</artifactId>
                        <version>1.0.0</version>
                    </dependency>
                    <dependency>
                        <groupId>com.another</groupId>
                        <artifactId>other-artifact</artifactId>
                        <version>1.0.0</version>
                    </dependency>
                </dependencies>
            </project>
            """,
              """
              <project>
                  <dependencies>
                      <dependency>
                          <groupId>com.new</groupId>
                          <artifactId>new-artifact</artifactId>
                          <version>2.0.0</version>
                      </dependency>
                      <dependency>
                          <groupId>com.another</groupId>
                          <artifactId>other-artifact</artifactId>
                          <version>1.0.0</version>
                      </dependency>
                  </dependencies>
              </project>
              """
            )
          );
    }
    @Test
    void dontChangeDependenciesIfVersionNotExists() {
        String oldGroupId = "com.old";
        String oldArtifactId = "old-artifact";
        String newGroupId = "com.new";
        String newArtifactId = "new-artifact";
        String newVersion = "2.0.0";

        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new ChangeDependencyRecipe(oldGroupId, oldArtifactId, newGroupId, newArtifactId, newVersion)),

          // language=xml
          xml(
            """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.old</groupId>
                        <artifactId>old-artifact</artifactId>
                        RANDOM
                    </dependency>
                    <dependency>
                        <groupId>com.another</groupId>
                        <artifactId>other-artifact</artifactId>
                        RANDOM
                    </dependency>
                </dependencies>
            </project>
            """
            )
          );
    }
    @Test
    void dontChangeDependenciesIfArtifactIdNotExists() {
        String oldGroupId = "com.old";
        String oldArtifactId = "old-artifact";
        String newGroupId = "com.new";
        String newArtifactId = "new-artifact";
        String newVersion = "2.0.0";

        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new ChangeDependencyRecipe(oldGroupId, oldArtifactId, newGroupId, newArtifactId, newVersion)),

          // language=xml
          xml(
            """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.old</groupId>
                    </dependency>
                </dependencies>
            </project>
            """
            )
          );
    }
    @Test
    void dontChangeDependenciesIfArtifactIdValueNotMatchesOldArtifactId() {
        String oldGroupId = "com.old";
        String oldArtifactId = "old-artifact";
        String newGroupId = "com.new";
        String newArtifactId = "new-artifact";
        String newVersion = "2.0.0";

        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new ChangeDependencyRecipe(oldGroupId, oldArtifactId, newGroupId, newArtifactId, newVersion)),

          // language=xml
          xml(
            """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.old</groupId>
                        <artifactId>other-artifact</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """
            )
          );
    }
    @Test
    void dontChangeDependenciesIfGroupIdNotExists() {
        String oldGroupId = "com.old";
        String oldArtifactId = "old-artifact";
        String newGroupId = "com.new";
        String newArtifactId = "new-artifact";
        String newVersion = "2.0.0";

        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new ChangeDependencyRecipe(oldGroupId, oldArtifactId, newGroupId, newArtifactId, newVersion)),

          // language=xml
          xml(
            """
            <project>
                <dependencies>
                    <dependency>
                        <artifactId>old-artifact</artifactId>
                    </dependency>
                    <dependency>
                        <artifactId>other-artifact</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """
            )
          );
    }

    @Test
    void changeDependencyWithoutVersion() {
        String oldGroupId = "com.old";
        String oldArtifactId = "old-artifact";
        String newGroupId = "com.new";
        String newArtifactId = "new-artifact";

        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new ChangeDependencyRecipe(oldGroupId, oldArtifactId, newGroupId, newArtifactId, null)),
          // language=xml
          xml(
            """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.old</groupId>
                        <artifactId>old-artifact</artifactId>
                        <version>1.0.0</version>
                    </dependency>
                    <dependency>
                        <groupId>com.another</groupId>
                        <artifactId>other-artifact</artifactId>
                        <version>1.0.0</version>
                    </dependency>
                </dependencies>
            </project>
            """,
              """
              <project>
                  <dependencies>
                      <dependency>
                          <groupId>com.new</groupId>
                          <artifactId>new-artifact</artifactId>
                          <version>1.0.0</version>
                      </dependency>
                      <dependency>
                          <groupId>com.another</groupId>
                          <artifactId>other-artifact</artifactId>
                          <version>1.0.0</version>
                      </dependency>
                  </dependencies>
              </project>
              """
            )
          );
    }
    @Test
    void dontChangeDependenciesIfArtifactIdValueIsNull() {
        String oldGroupId = "com.old";
        String oldArtifactId = "old-artifact";
        String newGroupId = "com.new";
        String newArtifactId = "new-artifact";
        String newVersion = "2.0.0";

        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new ChangeDependencyRecipe(oldGroupId, oldArtifactId, newGroupId, newArtifactId, newVersion)),

          xml(
            """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.old</groupId>
                        <artifactId>null-artifact</artifactId>
                        <version>1.0.0</version>
                    </dependency>
                    <dependency>
                        <groupId>com.old</groupId>
                        <artifactId></artifactId>
                        <version>1.0.0</version>
                    </dependency>
                </dependencies>
            </project>
            """
          )
        );
    }
    @Test
    void testHandleNullPointerForDependencyContent() {
        String oldGroupId = "com.old";
        String oldArtifactId = "old-artifact";
        String newGroupId = "com.new";
        String newArtifactId = "new-artifact";
        String newVersion = "2.0.0";

        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new ChangeDependencyRecipe(oldGroupId, oldArtifactId, newGroupId, newArtifactId, newVersion)),

          xml(
            """
            <dependency/>
            """
          )
        );
    }

}
