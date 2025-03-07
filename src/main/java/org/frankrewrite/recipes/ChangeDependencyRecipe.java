package org.frankrewrite.recipes;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openrewrite.*;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return new XmlIsoVisitor<>(){
            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if (tag.getName().equals("dependency")) {
                    List<? extends Content> content = tag.getContent();
                    if (content==null) //If for some reason tag is like this <dependency/>
                        return super.visitTag(tag, executionContext); // Avoid null pointer

                    //Get groupId tag expression
                    Optional<Xml.Tag> groupId = (Optional<Xml.Tag>) content.stream().filter(c ->
                            c instanceof Xml.Tag t //Check if content is tag
                                    && t.getName().equals("groupId") && t.getValue().map( //Handle optional with .map .orElse
                                    v->v.equals(oldGroupId)).orElse(false)).findFirst(); //Check if tag value matches old groupId
                    //Check if the expression returns an existing tag
                    if(groupId.isPresent()) {
                        // GroupId exists!
                        // Do the same thing but for artifactId, you know the drill
                        List<Content> resultContent = new ArrayList<>(content);

                        Optional<Xml.Tag> artifactId = (Optional<Xml.Tag>) content.stream().filter(c ->
                                c instanceof Xml.Tag t
                                        && t.getName().equals("artifactId")
                                        && t.getValue().map(
                                        v -> v.equals(oldArtifactId)).orElse(false)).findFirst();
                        if (artifactId.isPresent()) {
                            //We've established tag is the target dependency tag
                            //Now we should replace the content tag values according to the parameters
                            resultContent.removeAll(List.of(artifactId.get(), groupId.get()));
                            resultContent.add(content.indexOf(groupId.get()), groupId.get().withValue(newGroupId));
                            resultContent.add(content.indexOf(artifactId.get()), artifactId.get().withValue(newArtifactId));

                            //Check if it's necessary to replace the version tag value as well
                            if (version==null) {
                                return tag.withContent(resultContent);
                            }else {
                                //Get the version tag if it exists
                                Optional<Xml.Tag> versionTag = (Optional<Xml.Tag>) content.stream().filter(c ->
                                        c instanceof Xml.Tag t
                                                && t.getName().equals("version")).findFirst();
                                if (versionTag.isPresent()) {
                                    //Update version tag in content
                                    resultContent.remove(versionTag.get());
                                    resultContent.add(content.indexOf(versionTag.get()), versionTag.get().withValue(version));
                                    //Return tag with updated content
                                    return tag.withContent(resultContent);
                                }
                            }
                        }
                    }
                }



                return super.visitTag(tag, executionContext);
            }
        };
    }
}
