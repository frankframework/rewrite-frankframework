package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class ChangeAttributeTest implements RewriteTest {

    @Test
    void addsNameAttributeToXmlValidatorPipe(){
        rewriteRun(spec-> spec.recipe(new ChangeAttributeRecipe("LocalFileSystemPipe","outputType", "base64", null, "DECODE")),
          xml(
            """
                <LocalFileSystemPipe outputType="base64"/>""",
            """
                <LocalFileSystemPipe base64="DECODE"/>"""
          )
        );
    }
    @Test
    void addsNameAttributeToXmlValidatorPipeWithValue(){
        rewriteRun(spec-> spec.recipe(new ChangeAttributeRecipe("LocalFileSystemPipe","outputType", "base64", "base64", "DECODE")),
          xml(
            """
                <LocalFileSystemPipe outputType="base64"/>""",
            """
                <LocalFileSystemPipe base64="DECODE"/>"""
          )
        );
    }
    @Test
    void notAddsNameAttributeToXmlValidatorPipeWithValue(){
        rewriteRun(spec-> spec.recipe(new ChangeAttributeRecipe("LocalFileSystemPipe","outputType", "base64", "base6412", "DECODE")),
          xml(
            """
                <LocalFileSystemPipe outputType="base64"/>"""
          )
        );
    }
    @Test
    void changesValueForOldValue(){
        rewriteRun(spec-> spec.recipe(new ChangeAttributeRecipe("sender","queryType", "queryType", "insert into", "insert")),
          xml(
            """
                <sender jmsRealm="jdbc" queryType="insert into" query="INSERT INTO BACKLOGS (TEAM_ID) VALUES (?)" className="nl.nn.adapterframework.jdbc.FixedQuerySender">
					<param name="teamId" type="integer" sessionKey="teamId"/>
				</sender>""",
                """
                <sender jmsRealm="jdbc" query="INSERT INTO BACKLOGS (TEAM_ID) VALUES (?)" className="nl.nn.adapterframework.jdbc.FixedQuerySender" queryType="insert">
					<param name="teamId" type="integer" sessionKey="teamId"/>
				</sender>"""
          )
        );
    }
}
