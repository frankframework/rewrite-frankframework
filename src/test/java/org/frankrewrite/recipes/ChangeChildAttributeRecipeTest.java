package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.openrewrite.xml.Assertions.xml;

class ChangeChildAttributeRecipeTest implements RewriteTest {

    @Test
    void testChangeChildAttributeValue() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", null, "value1", "value2")),
          //language=xml
          xml(
            """
             <parent>
             dfgh
                 <child attribute="value1"/>
             </parent>""","""
             <parent>
             dfgh
                 <child attribute="value2"/>
             </parent>"""
          )
        );
    }
    @Test
    void testChangeChildAttributeKey() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parent>
             dfgh
                 <child attribute="value1"/>
             </parent>""","""
             <parent>
             dfgh
                 <child attribute2="value1"/>
             </parent>"""
          )
        );
    }
    @Test
    void onlyChangeSpecifiedParent() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parent2>
                 <child attribute="value1"/>
             </parent2>"""
          )
        );
    }
    @Test
    void dontChangeParentWithoutContent() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parent/>"""
          )
        );
    }
    @Test
    void onlyChangeSpecifiedChildAttribute() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe("parent", "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parent>
                 <child2 attribute="value1"/>
             </parent>"""
          )
        );
    }
    @Test
    void changeSpecifiedChildAttributeGlobally() {
        rewriteRun(spec->spec.recipe(new ChangeChildAttributeRecipe(null, "child", "attribute", "attribute2", "value1", null)),
          //language=xml
          xml(
            """
             <parentsdfgdsfg>
                 <child attribute="value1"/>
             </parentsdfgdsfg>""","""
             <parentsdfgdsfg>
                 <child attribute2="value1"/>
             </parentsdfgdsfg>"""
          )
        );
    }
}