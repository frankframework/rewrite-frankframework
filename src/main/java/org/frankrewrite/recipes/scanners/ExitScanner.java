package org.frankrewrite.recipes.scanners;

import org.frankrewrite.recipes.util.TagHandler;
import org.openrewrite.xml.tree.Xml;

import java.util.*;

public class ExitScanner {
    public final Map<Xml.Document, Map<Xml.Tag, List<Xml.Tag>>> foundExitsPerAdapterPerDocument = new HashMap<>();
    public final Map<Xml.Document, Map<Xml.Tag, List<Xml.Tag>>> foundRecurringExitsPerAdapterPerDocument = new HashMap<>();

    public String getNewExitNameByOldPathForDocument(Xml.Document document, Xml.Tag adapter, String oldPath) {
        return foundRecurringExitsPerAdapterPerDocument
                .getOrDefault(document, Collections.emptyMap())
                .entrySet().stream()
                .filter(entry -> TagHandler.haveMatchingAttribute(entry.getKey(), adapter, "name"))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .filter(tag -> TagHandler.hasMatchingAttribute(tag, "path", oldPath))
                .flatMap(tag -> TagHandler.getAttributeValueFromTagByKey(tag, "code").stream())
                .flatMap(code -> getTagByCode(foundExitsPerAdapterPerDocument.getOrDefault(document, Collections.emptyMap()).get(adapter), code).stream())
                .flatMap(newTag -> TagHandler.getAttributeValueFromTagByKey(newTag, "path").stream())
                .findFirst()
                .orElse(null);
    }

    public Optional<Xml.Tag> getTagByCode(List<Xml.Tag> tags, String code) {
        return Optional.ofNullable(tags)
                .orElse(Collections.emptyList())
                .stream()
                .filter(tag -> TagHandler.hasMatchingAttribute(tag, "code", code))
                .findFirst();
    }

    public void addTagToTracking(Xml.Document document, Xml.Tag adapter, Xml.Tag tag) {
        Optional<String> codeValue = TagHandler.getAttributeValueFromTagByKey(tag, "code");
        if (codeValue.isEmpty()) return;

        String code = codeValue.get();
        foundExitsPerAdapterPerDocument.computeIfAbsent(document, k -> new HashMap<>())
                .computeIfAbsent(adapter, k -> new ArrayList<>());

        foundRecurringExitsPerAdapterPerDocument.computeIfAbsent(document, k -> new HashMap<>())
                .computeIfAbsent(adapter, k -> new ArrayList<>());

        List<Xml.Tag> firstOccurrences = foundExitsPerAdapterPerDocument.get(document).get(adapter);
        List<Xml.Tag> recurringOccurrences = foundRecurringExitsPerAdapterPerDocument.get(document).get(adapter);

        if (firstOccurrences.stream().anyMatch(existing -> TagHandler.hasMatchingAttribute(existing, "code", code))) {
            if (!recurringOccurrences.contains(tag)) recurringOccurrences.add(tag);
        } else {
            firstOccurrences.add(tag);
        }
    }

}
