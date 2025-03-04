package org.frankrewrite.recipes.visitors;

import org.frankrewrite.recipes.scanresults.ExitScanResult;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

public class ExitScanningVisitor extends XmlIsoVisitor<ExecutionContext> {
    private final ExitScanResult acc;

    public ExitScanningVisitor(ExitScanResult acc) {
        this.acc = acc;
    }

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        Xml.Document document = getCursor().firstEnclosing(Xml.Document.class);
        if (document!=null&&tag.getName().equalsIgnoreCase("adapter")) {
            tag.getChildren().stream()
                    .filter(pipeline -> pipeline.getName().equalsIgnoreCase("pipeline"))
                    .flatMap(pipelineTag -> pipelineTag.getChildren().stream())
                    .filter(exits -> exits.getName().equalsIgnoreCase("exits"))
                    .flatMap(exitsTag -> exitsTag.getChildren().stream())
                    .filter(exitTag -> exitTag.getName().equalsIgnoreCase("exit"))
                    .forEach(exitTag -> acc.addTagToTracking(document, tag, exitTag));
        }
        return super.visitTag(tag, executionContext);
    }

}
