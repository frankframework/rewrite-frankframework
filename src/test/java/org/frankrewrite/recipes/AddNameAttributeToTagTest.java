package org.frankrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

public class AddNameAttributeToTagTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddNameAttributeToTagRecipe("XmlValidatorPipe"));
    }

    @Test
    void addsNameAttributeToXmlValidatorPipe(){
        rewriteRun(recipeSpec -> recipeSpec.cycles(1),
          xml(
            """
                <module>
                    <adapter name="ManageDatabase" description="Manage the database" active="${manageDatabase.active}">
                        <FixedErrorMessage fileName="/ManageDatabase/xml/ErrorMessage.xml" replaceFrom="%reasonCode" replaceTo="INTERNAL_ERROR" />
                        <GenericReceiver name="ManageDatabase">
                            <JavaListener name="ManageDatabase" serviceName="ManageDatabase" />
                        </GenericReceiver>
                        <GenericReceiver name="ManageDatabase-ws" active="${manageDatabase.webServiceListener.active}">
                            <WebServiceListener name="ManageDatabase-ws" serviceNamespaceURI="http://managedatabase.ibissource.org/" />
                        </GenericReceiver>
                        <pipeline firstPipe="Query" transactionAttribute="Required">
                            <XmlValidatorPipe schema="ManageDatabase/xsd/ManageDatabase.xsd" root="manageDatabaseREQ">
                                <forward name="failure" path="InputValidateFailure" />
                                <forward name="parserError" path="InputValidateError" />
                            </XmlValidatorPipe>
                            <XmlValidatorPipe schema="ManageDatabase/xsd/ManageDatabase.xsd" root="manageDatabaseRLY">
                                <forward name="failure" path="InternalXsdFailure" />
                                <forward name="parserError" path="InternalXsdError" />
                            </XmlValidatorPipe>
                            <exits>
                                <exit path="EXIT" state="success" />
                            </exits>
                            <ForEachChildElementPipe name="Query" elementXPathExpression="manageDatabaseREQ/*" ignoreExceptions="true">
                                <XmlQuerySender jmsRealm="jdbc" />
                                <forward name="success" path="ManageDatabaseRLY" />
                            </ForEachChildElementPipe>
                            <XsltPipe name="ManageDatabaseRLY" styleSheetName="/ManageDatabase/xsl/ManageDatabaseRLY.xsl">
                                <param name="returnResults" sessionKey="originalMessage" xpathExpression="/manageDatabaseREQ/@returnResults" />
                                <forward name="success" path="EXIT" />
                            </XsltPipe>
                            <!-- ERRORS -->
                            <FixedResultPipe name="InputValidateError" fileName="/ManageDatabase/xml/ErrorMessage.xml" replaceFrom="%reasonCode" replaceTo="NOT_WELL_FORMED_XML">
                                <forward name="success" path="EXIT" />
                            </FixedResultPipe>
                            <XsltPipe name="InputValidateFailure" styleSheetName="/ManageDatabase/xsl/ErrorMessage.xsl" getInputFromFixedValue="&lt;dummy/&gt;">
                                <param name="errorCode" value="INVALID_XML" />
                                <param name="failureReason" sessionKey="failureReason" />
                                <forward name="success" path="EXIT" />
                            </XsltPipe>
                            <XsltPipe name="InternalXsdError" styleSheetName="/ManageDatabase/xsl/ErrorMessage.xsl" getInputFromFixedValue="&lt;dummy/&gt;">
                                <param name="errorCode" value="INTERNAL_XSD_ERROR" />
                                <param name="failureReason" value="NOT_WELL_FORMED_XML" />
                                <forward name="success" path="EXIT" />
                            </XsltPipe>
                            <XsltPipe name="InternalXsdFailure" styleSheetName="/ManageDatabase/xsl/ErrorMessage.xsl" getInputFromFixedValue="&lt;dummy/&gt;">
                                <param name="errorCode" value="INTERNAL_XSD_ERROR" />
                                <param name="failureReason" sessionKey="failureReason" />
                                <forward name="success" path="EXIT" />
                            </XsltPipe>
                        </pipeline>
                    </adapter>
                </module>""",
            """
                <module>
                    <adapter name="ManageDatabase" description="Manage the database" active="${manageDatabase.active}">
                        <FixedErrorMessage fileName="/ManageDatabase/xml/ErrorMessage.xml" replaceFrom="%reasonCode" replaceTo="INTERNAL_ERROR" />
                        <GenericReceiver name="ManageDatabase">
                            <JavaListener name="ManageDatabase" serviceName="ManageDatabase" />
                        </GenericReceiver>
                        <GenericReceiver name="ManageDatabase-ws" active="${manageDatabase.webServiceListener.active}">
                            <WebServiceListener name="ManageDatabase-ws" serviceNamespaceURI="http://managedatabase.ibissource.org/" />
                        </GenericReceiver>
                        <pipeline firstPipe="Query" transactionAttribute="Required">
                            <XmlValidatorPipe schema="ManageDatabase/xsd/ManageDatabase.xsd" root="manageDatabaseREQ" name="myXmlValidatorPipe">
                                <forward name="failure" path="InputValidateFailure" />
                                <forward name="parserError" path="InputValidateError" />
                            </XmlValidatorPipe>
                            <XmlValidatorPipe schema="ManageDatabase/xsd/ManageDatabase.xsd" root="manageDatabaseRLY" name="myXmlValidatorPipe2">
                                <forward name="failure" path="InternalXsdFailure" />
                                <forward name="parserError" path="InternalXsdError" />
                            </XmlValidatorPipe>
                            <exits>
                                <exit path="EXIT" state="success" />
                            </exits>
                            <ForEachChildElementPipe name="Query" elementXPathExpression="manageDatabaseREQ/*" ignoreExceptions="true">
                                <XmlQuerySender jmsRealm="jdbc" />
                                <forward name="success" path="ManageDatabaseRLY" />
                            </ForEachChildElementPipe>
                            <XsltPipe name="ManageDatabaseRLY" styleSheetName="/ManageDatabase/xsl/ManageDatabaseRLY.xsl">
                                <param name="returnResults" sessionKey="originalMessage" xpathExpression="/manageDatabaseREQ/@returnResults" />
                                <forward name="success" path="EXIT" />
                            </XsltPipe>
                            <!-- ERRORS -->
                            <FixedResultPipe name="InputValidateError" fileName="/ManageDatabase/xml/ErrorMessage.xml" replaceFrom="%reasonCode" replaceTo="NOT_WELL_FORMED_XML">
                                <forward name="success" path="EXIT" />
                            </FixedResultPipe>
                            <XsltPipe name="InputValidateFailure" styleSheetName="/ManageDatabase/xsl/ErrorMessage.xsl" getInputFromFixedValue="&lt;dummy/&gt;">
                                <param name="errorCode" value="INVALID_XML" />
                                <param name="failureReason" sessionKey="failureReason" />
                                <forward name="success" path="EXIT" />
                            </XsltPipe>
                            <XsltPipe name="InternalXsdError" styleSheetName="/ManageDatabase/xsl/ErrorMessage.xsl" getInputFromFixedValue="&lt;dummy/&gt;">
                                <param name="errorCode" value="INTERNAL_XSD_ERROR" />
                                <param name="failureReason" value="NOT_WELL_FORMED_XML" />
                                <forward name="success" path="EXIT" />
                            </XsltPipe>
                            <XsltPipe name="InternalXsdFailure" styleSheetName="/ManageDatabase/xsl/ErrorMessage.xsl" getInputFromFixedValue="&lt;dummy/&gt;">
                                <param name="errorCode" value="INTERNAL_XSD_ERROR" />
                                <param name="failureReason" sessionKey="failureReason" />
                                <forward name="success" path="EXIT" />
                            </XsltPipe>
                        </pipeline>
                    </adapter>
                </module>"""
          )
        );
    }
}
