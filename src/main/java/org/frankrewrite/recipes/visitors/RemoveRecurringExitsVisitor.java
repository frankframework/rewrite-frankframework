package org.frankrewrite.recipes.visitors;

import org.frankrewrite.recipes.scanresults.ExitScanResult;
import org.frankrewrite.recipes.util.AttributeHandler;
import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.*;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public class RemoveRecurringExitsVisitor extends XmlIsoVisitor<ExecutionContext> {
    private final ExitScanResult acc;

    public RemoveRecurringExitsVisitor(ExitScanResult acc) {
        this.acc = acc;
    }

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        if (!tag.getName().equalsIgnoreCase("adapter")) {
            return super.visitTag(tag, executionContext);
        }

        Xml.Document document = getCursor().firstEnclosing(Xml.Document.class);
        List<Xml.Tag> exitsForTag = getRecurringExits(tag, document);
        int exitCount = countExits(tag);
        Optional<Content> exitsOptional = findExitsTag(tag);

        if (exitsOptional.isPresent()
                &&!exitsForTag.isEmpty()) {
            List<Content> updatedContent = updateRecurringExits(tag, exitsForTag, document, exitsOptional.get());
            if (exitCount != countExits(tag.withContent(updatedContent))) {
                return tag.withContent(updatedContent);
            }
        }
        return super.visitTag(tag, executionContext);
    }

    private List<Xml.Tag> getRecurringExits(Xml.Tag tag, Xml.Document document) {
        return acc.foundRecurringExitsPerAdapterPerDocument
                .getOrDefault(document, Collections.emptyMap())
                .getOrDefault(tag, Collections.emptyList());
    }

    private static int countExits(Xml.Tag tag) {
        return (int) getContent(tag).stream()
                .filter(pipeline -> pipeline instanceof Xml.Tag && ((Xml.Tag) pipeline).getName().equalsIgnoreCase("pipeline"))
                .flatMap(pipeline -> getContent(pipeline).stream())
                .filter(child -> child instanceof Xml.Tag && ((Xml.Tag) child).getName().equalsIgnoreCase("exits"))
                .flatMap(exitsTag -> getContent(exitsTag).stream())
                .filter(exitTag -> exitTag instanceof Xml.Tag && ((Xml.Tag) exitTag).getName().equalsIgnoreCase("exit"))
                .count();
    }

    private List<Content> updateRecurringExits(Xml.Tag adapter, List<Xml.Tag> recurringExits, Xml.Document document, Content exitsTag) {
        List<Content> filteredRecurringExits = filterRecurringExits(adapter, recurringExits);

        List<Content> updatedExitChildren = filterExitChildren(exitsTag, filteredRecurringExits);
        List<Content> updatedPipelineContent = updatePipelineContent(adapter, updatedExitChildren, exitsTag);
        adapter = adapter.withContent(updatedPipelineContent);

        return updateTagContents(document, adapter);
    }

    private List<Content> filterRecurringExits(Xml.Tag adapter, List<Xml.Tag> recurringExits) {
        return getContent(adapter).stream()
                .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                .flatMap(pipelineContent -> getContent(pipelineContent).stream())
                .filter(content -> content instanceof Xml.Tag exitsTag && exitsTag.getName().equalsIgnoreCase("exits"))
                .flatMap(exitsContent -> getContent(exitsContent).stream())
                .filter(exitTag -> !(exitTag instanceof Xml.Tag t) || !recurringExits.contains(t) || TagHandler.getAttributeValueFromTagByKey(t, "code").isEmpty())
                .toList();
    }

    private Optional<Content> findExitsTag(Xml.Tag adapter) {
        return getContent(adapter).stream()
                .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                .flatMap(pipelineContent -> getContent(pipelineContent).stream())
                .filter(content -> content instanceof Xml.Tag t && t.getName().equalsIgnoreCase("exits"))
                .findFirst();
    }

    private List<Content> filterExitChildren(Content exitsTag, List<Content> filteredRecurringExits) {
        return getContent(exitsTag).stream()
                .filter(content -> content instanceof Xml.Tag t && filteredRecurringExits.contains(t))
                .toList();
    }

    private List<Content> updatePipelineContent(Xml.Tag adapter, List<Content> updatedExitChildren, Content exitsTag) {
        List<Content> pipelineContentWithoutExits = new ArrayList<>(removeExistingExitsTag(adapter));

        Xml.Tag updatedExitsTag = ((Xml.Tag) exitsTag).withContent(updatedExitChildren);
        pipelineContentWithoutExits.add(0, updatedExitsTag);

        return getContent(adapter).stream()
                .map(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline")
                        ? pipelineTag.withContent(pipelineContentWithoutExits)
                        : content)
                .toList();
    }

    private List<Content> removeExistingExitsTag(Xml.Tag adapter) {
        return getContent(adapter).stream()
                .filter(content -> content instanceof Xml.Tag pipelineTag && pipelineTag.getName().equalsIgnoreCase("pipeline"))
                .flatMap(pipelineContent -> getContent(pipelineContent).stream())
                .filter(content -> !(content instanceof Xml.Tag t && t.getName().equalsIgnoreCase("exits")))
                .toList();
    }

    private List<Content> updateTagContents(Xml.Document document, Xml.Tag adapter) {
        return getContent(adapter).stream()
                .map(child -> updateTagContent(document, adapter, child))
                .toList();
    }

    private Content updateTagContent(Xml.Document document, Xml.Tag adapter, Content content) {
        if (!(content instanceof Xml.Tag tag)) return content;
        List<Xml.Attribute> updatedAttributes = getUpdatedAttributes(document, adapter, tag);
        List<Content> updatedChildren = getContent(tag).stream()
                .map(child -> updateTagContent(document, adapter, child))
                .toList();
        return tag.withAttributes(updatedAttributes).withContent(updatedChildren);
    }

    private List<Xml.Attribute> getUpdatedAttributes(Xml.Document document, Xml.Tag adapter, Xml.Tag tag) {
        List<Xml.Attribute> updatedAttributes = tag.getAttributes();
        TagHandler.getAttributeFromTagByKey(tag, "path")
                .ifPresent(attr -> AttributeHandler.updateListForAttributeWithNewValue(updatedAttributes, attr,
                        acc.getNewExitNameByOldPathForDocument(document, adapter, attr.getValueAsString())));
        return updatedAttributes;
    }
}
