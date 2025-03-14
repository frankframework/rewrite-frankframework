package org.frankrewrite.recipes.visitors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class ChangeDependencyVisitor extends XmlIsoVisitor<ExecutionContext> {
    private String oldGroupId;
    private String oldArtifactId;
    private String newGroupId;
    private String newArtifactId;
    private String version;

    public ChangeDependencyVisitor(String oldGroupId, String oldArtifactId, String newGroupId, String newArtifactId, String version) {
        this.oldGroupId = oldGroupId;
        this.oldArtifactId = oldArtifactId;
        this.newGroupId = newGroupId;
        this.newArtifactId = newArtifactId;
        this.version = version;
    }

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        if (tag.getName().equals("dependency")) {
            List<Content> content = getContent(tag);
            //Get groupId tag expression
            Optional<Xml.Tag> groupId = content.stream().filter(c ->
                    c instanceof Xml.Tag t //Check if content is tag
                            && t.getName().equals("groupId") && t.getValue().map( //Handle optional with .map .orElse
                            v->v.equals(oldGroupId)).orElse(false)).map(Xml.Tag.class::cast).findFirst(); //Check if tag value matches old groupId
            //Check if the expression returns an existing tag
            if(groupId.isPresent()) {
                // GroupId exists!
                // Do the same thing but for artifactId, you know the drill
                List<Content> resultContent = new ArrayList<>(content);

                Optional<Xml.Tag> artifactId = content.stream().filter(c ->
                        c instanceof Xml.Tag t
                                && t.getName().equals("artifactId")//Filter the artifactId tag
                                && t.getValue().map(
                                v -> v.equals(oldArtifactId)).orElse(false)).map(Xml.Tag.class::cast).findFirst();
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
                        Optional<Xml.Tag> versionTag = content.stream().filter(c ->
                                        c instanceof Xml.Tag t
                                                && t.getName().equals("version"))//Filter the version tag
                                .map(Xml.Tag.class::cast).findFirst();//Cast Content to Xml.Tag
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
}
