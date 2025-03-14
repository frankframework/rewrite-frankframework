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
        if (tag.getName().equalsIgnoreCase("adapter")) {
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
