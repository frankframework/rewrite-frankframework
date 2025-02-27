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
    @Test
    void changesValueForOldValue2(){
        rewriteRun(spec-> spec.recipe(new ChangeAttributeRecipe("exit","state", null, "succes", "success")),
          xml(
            """
                <exits>
                    <exit path="OK" state="succes" />
                </exits>""",
                """
                <exits>
                    <exit path="OK" state="success" />
                </exits>"""
          )
        );
    }
}
