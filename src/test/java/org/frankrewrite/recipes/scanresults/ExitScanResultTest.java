package org.frankrewrite.recipes.scanresults;

import org.junit.jupiter.api.Test;
import org.openrewrite.xml.tree.Xml;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExitScanResultTest {
    @Test
    void dontAddEmptyCodeValueToTracking() {
        Xml.Document document = mock(Xml.Document.class);
        ExitScanResult exitScanResult = new ExitScanResult();
        Xml.Tag adapter = Xml.Tag.build("""
            <Adapter>
                <exits>
                    <exit path='path1' />
                </exits>
            </Adapter>""");
        exitScanResult.addTagToTracking(document,
          //language=xml
          adapter,
          //language=xml
          Xml.Tag.build("""
            <exit path='path1' />""")
        );

        assertNull(exitScanResult.foundExitsPerAdapterPerDocument.get(document));
    }
    @Test
    void dontAddExistingTagWithMatchingCodeValueToTrackingAgain() {
        Xml.Document document = mock(Xml.Document.class);
        ExitScanResult exitScanResult = new ExitScanResult();
        Xml.Tag adapter = Xml.Tag.build("""
            <Adapter>
                <exits>
                    <exit code="1" path='path1' />
                </exits>
            </Adapter>""");
        Xml.Tag exit = Xml.Tag.build("""
            <exit code="1" path='path1' />""");
        exitScanResult.addTagToTracking(document,adapter,exit);

        assertEquals(1, exitScanResult.foundExitsPerAdapterPerDocument.get(document).get(adapter).size());
        Xml.Tag exit2 = Xml.Tag.build("""
            <exit code="1" path='path2' />""");

        exitScanResult.addTagToTracking(document,adapter,exit2);

        assertEquals(1, exitScanResult.foundExitsPerAdapterPerDocument.get(document).get(adapter).size());

    }
    @Test
    void dontAddSameTagToTrackingAgain() {
        Xml.Document document = mock(Xml.Document.class);
        ExitScanResult exitScanResult = new ExitScanResult();
        Xml.Tag adapter = Xml.Tag.build("""
            <Adapter>
                <exits>
                    <exit code="1" path='path1' />
                </exits>
            </Adapter>""");
        Xml.Tag exit = Xml.Tag.build("""
            <exit code="1" path='path1' />""");
        exitScanResult.addTagToTracking(document,adapter,exit);

        assertEquals(1, exitScanResult.foundExitsPerAdapterPerDocument.get(document).get(adapter).size());
        Xml.Tag exit2 = Xml.Tag.build("""
            <exit code="1" path='path2' />""");

        exitScanResult.addTagToTracking(document,adapter,exit2);

        assertEquals(1, exitScanResult.foundExitsPerAdapterPerDocument.get(document).get(adapter).size());

        exitScanResult.addTagToTracking(document,adapter,exit2);

        assertEquals(1, exitScanResult.foundExitsPerAdapterPerDocument.get(document).get(adapter).size());

    }
}