package org.frankrewrite.recipes.visitors;

import org.frankrewrite.recipes.scanresults.ExitScanResult;
import org.frankrewrite.recipes.util.AttributeHandler;
import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.*;

public class RemoveRecurringExitsVisitor extends XmlIsoVisitor<ExecutionContext> {
    private final ExitScanResult acc;

    public RemoveRecurringExitsVisitor(ExitScanResult acc) {
        this.acc = acc;
    }

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        Xml.Document document = getCursor().firstEnclosing(Xml.Document.class);
        //check if tag is an adapter
        if (!tag.getName().equalsIgnoreCase("adapter"))
            return super.visitTag(tag, executionContext);

        Map<Xml.Tag, List<Xml.Tag>> recurringExits = acc.foundRecurringExitsPerAdapterPerDocument
                .getOrDefault(document, Collections.emptyMap());
        List<Xml.Tag> exitsForTag = recurringExits.get(tag);

        int exitCount = countExits(tag);

        if (exitsForTag != null && !exitsForTag.isEmpty()) {
            List<? extends Content> updatedContent = updateRecurringExits(tag, exitsForTag, document);
            //check if any exits have been removed to avoid unnecessary cycles
            if (exitCount != countExits(tag.withContent(updatedContent)))
                return tag.withContent(updatedContent);
        }

        return super.visitTag(tag, executionContext);
    }

    private static int countExits(Xml.Tag tag) {
        return (int) tag.getChildren().stream()
                .filter(pipeline -> pipeline.getName().equalsIgnoreCase("pipeline"))
                .flatMap(pipeline -> pipeline.getChildren().stream())
                .filter(child -> child.getName().equalsIgnoreCase("exits"))
                .flatMap(exitsTag -> exitsTag.getChildren().stream())
                .filter(exitTag -> exitTag.getName().equalsIgnoreCase("exit"))
                .count();
    }

    private List<Content> updateRecurringExits(Xml.Tag adapter, List<Xml.Tag> recurringExits, Xml.Document document) {
        // Filter recurring exits: only keep unique code values
        List<? extends Content> filteredRecurringExits = adapter.getContent().stream()
                .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                .flatMap(pipelineContent -> ((Xml.Tag) pipelineContent).getContent().stream())
                .filter(content -> content instanceof Xml.Tag exitsTag && exitsTag.getName().equalsIgnoreCase("exits"))
                .flatMap(exitsContent -> ((Xml.Tag) exitsContent).getContent().stream())
                .filter(exitTag -> {
                    if (!(exitTag instanceof Xml.Tag t)) {
                        return true;
                    }
                    String code = TagHandler.getAttributeValueFromTagByKey(t, "code").orElse(null);
                    return code == null || !recurringExits.contains(t);
                })
                .toList();

        Optional<? extends Content> exitsOptional = adapter.getContent().stream()
                .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                .flatMap(pipelineContent -> ((Xml.Tag) pipelineContent).getContent().stream())
                .filter(content -> content instanceof Xml.Tag t && t.getName().equalsIgnoreCase("exits"))
                .findFirst();

        // Filter child tags in "exits", keeping only those in filteredRecurringExits
        List<? extends Content> updatedExitChildren = ((Xml.Tag) exitsOptional.get()).getContent()
                .stream()
                .filter(content -> content instanceof Xml.Tag t && filteredRecurringExits.contains(t))
                .toList();

        // Remove the existing "exits" tag from the pipeline content
        List<Content> pipelineContentWithoutExits = new ArrayList<>(adapter.getContent().stream()
                .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                .flatMap(pipelineContent -> ((Xml.Tag) pipelineContent).getContent().stream())
                .filter(content -> !(content instanceof Xml.Tag t && t.getName().equalsIgnoreCase("exits")))
                .toList());

        // Create a new "exits" tag with the updated children
        Xml.Tag updatedExitsTag = ((Xml.Tag) exitsOptional.get()).withContent(updatedExitChildren);

        // Add the updated "exits" tag back to the pipeline content
        pipelineContentWithoutExits.add(0, updatedExitsTag);

        // Update the "pipeline" content in the adapter
        List<Content> updatedPipelineContent = new ArrayList<>(adapter.getContent().stream()
                .map(content -> {
                    if (content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline")) {
                        return pipelineTag.withContent(pipelineContentWithoutExits);
                    }
                    return content;
                })
                .toList());

        adapter = adapter.withContent(updatedPipelineContent);


        Xml.Tag finalAdapter = adapter;
        //adapter.getContent() always returns a not null value, because it contains exits tag
        return adapter.getContent().stream()
                        .map(child -> updateTagContent(document, finalAdapter, child))
                        .toList();
    }

    private Content updateTagContent(Xml.Document document, Xml.Tag adapter, Content content) {
        //Check if content is a tag to avoid invalid cast (necessary for tag text values)
        if (!(content instanceof Xml.Tag tag))
            return content;

        List<Xml.Attribute> updatedAttributes = getUpdatedAttributes(document, adapter, tag);
        List<Content> updatedChildren = tag.getContent() == null ?
                Collections.emptyList() :
                tag.getContent().stream()
                        .map(child -> updateTagContent(document, adapter, child))
                        .toList();

        return tag.withAttributes(updatedAttributes).withContent(updatedChildren);
    }

    private List<Xml.Attribute> getUpdatedAttributes(Xml.Document document, Xml.Tag adapter, Xml.Tag tag) {
        List<Xml.Attribute> updatedAttributes = new ArrayList<>(tag.getAttributes());
        TagHandler.getAttributeFromTagByKey(tag, "path")
                .ifPresent(attr -> AttributeHandler.updateListForAttributeWithNewValue(updatedAttributes, attr,
                        acc.getNewExitNameByOldPathForDocument(document, adapter, attr.getValueAsString())));//Get updated path attribute Value
        return updatedAttributes;
    }

}