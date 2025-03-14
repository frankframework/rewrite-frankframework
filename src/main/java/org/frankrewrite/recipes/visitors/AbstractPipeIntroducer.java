package org.frankrewrite.recipes.visitors;

import org.frankrewrite.recipes.util.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.frankrewrite.recipes.util.TagHandler.getContent;

public abstract class AbstractPipeIntroducer extends XmlIsoVisitor<ExecutionContext> {

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
        if (!tag.getName().equalsIgnoreCase("pipeline")) {
            return super.visitTag(tag, ctx);
        }

        AtomicBoolean changed = new AtomicBoolean(false);
        List<Content> updatedChildren = getUpdatedChildren(tag, changed);

        if (changed.get()) {
            return tag.withContent(updatedChildren);
        }

        return super.visitTag(tag, ctx);
    }

    protected abstract List<Content> getUpdatedChildren(Xml.Tag tag, AtomicBoolean changed);

}