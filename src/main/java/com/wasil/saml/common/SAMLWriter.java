package com.wasil.saml.common;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Condition;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.OneTimeUse;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
 
/**
 * This is a demo class which creates a valid SAML 2.0 Assertion.
 */
public class SAMLWriter
{
	private final static Logger logger = LoggerFactory.getLogger(SAMLWriter.class);
	public Response getSamlResponse(SAMLInputContainer input) {
		Assertion assertion = null;
    	try {
			assertion = SAMLWriter.buildDefaultAssertion(input);
			AssertionMarshaller marshaller = new AssertionMarshaller();
			Element plaintextElement = marshaller.marshall(assertion);
			String originalAssertionString = XMLHelper.nodeToString(plaintextElement);
 
			System.out.println("Assertion String: " + originalAssertionString); 
		} catch (MarshallingException e) {
			e.printStackTrace();
		}
		try {
			return createResponse(assertion, input.getSessionId());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MarshallingException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		};
		return null;
 
	}
 
	private static XMLObjectBuilderFactory builderFactory;
 
	public static XMLObjectBuilderFactory getSAMLBuilder() throws ConfigurationException{ 
		if(builderFactory == null){
	         builderFactory = Configuration.getBuilderFactory();
		}
 
		return builderFactory;
	}
 
	/**
	 * Builds a SAML Attribute of type String
	 * @param name
	 * @param value
	 * @param builderFactory
	 * @return
	 * @throws ConfigurationException
	 */
	public static Attribute buildStringAttribute(String name, String value, XMLObjectBuilderFactory builderFactory) throws ConfigurationException
	{
		SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder) getSAMLBuilder().getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
		 Attribute attrFirstName = (Attribute) attrBuilder.buildObject();
		 attrFirstName.setName(name);
 
		 // Set custom Attributes
		 XMLObjectBuilder stringBuilder = getSAMLBuilder().getBuilder(XSString.TYPE_NAME);
		 XSString attrValueFirstName = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
		 attrValueFirstName.setValue(value);
 
		 attrFirstName.getAttributeValues().add(attrValueFirstName);
		return attrFirstName;
	}
 
	/**
	 * Helper method which includes some basic SAML fields which are part of almost every SAML Assertion.
	 *
	 * @param input
	 * @return
	 */
	public static Assertion buildDefaultAssertion(SAMLInputContainer input)
	{
		try
		{
	         // Create the NameIdentifier
	         SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(NameID.DEFAULT_ELEMENT_NAME);
	         NameID nameId = (NameID) nameIdBuilder.buildObject();
	         nameId.setValue(input.getStrNameID());
	         nameId.setNameQualifier(input.getStrNameQualifier());
	         nameId.setFormat(NameID.UNSPECIFIED);
 
	         // Create the SubjectConfirmation
 
	         SAMLObjectBuilder confirmationMethodBuilder = (SAMLObjectBuilder)  SAMLWriter.getSAMLBuilder().getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
	         SubjectConfirmationData confirmationMethod = (SubjectConfirmationData) confirmationMethodBuilder.buildObject();
	         DateTime now = new DateTime();
	         confirmationMethod.setNotBefore(now);
	         confirmationMethod.setNotOnOrAfter(now.plusMinutes(2));
 
	         SAMLObjectBuilder subjectConfirmationBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
	         SubjectConfirmation subjectConfirmation = (SubjectConfirmation) subjectConfirmationBuilder.buildObject();
	         subjectConfirmation.setSubjectConfirmationData(confirmationMethod);
 
	         // Create the Subject
	         SAMLObjectBuilder subjectBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(Subject.DEFAULT_ELEMENT_NAME);
	         Subject subject = (Subject) subjectBuilder.buildObject();
 
	         subject.setNameID(nameId);
	         subject.getSubjectConfirmations().add(subjectConfirmation);
 
	         // Create Authentication Statement
	         SAMLObjectBuilder authStatementBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
	         AuthnStatement authnStatement = (AuthnStatement) authStatementBuilder.buildObject();
	         //authnStatement.setSubject(subject);
	         //authnStatement.setAuthenticationMethod(strAuthMethod);
	         DateTime now2 = new DateTime();
	         authnStatement.setAuthnInstant(now2);
	         authnStatement.setSessionIndex(input.getSessionId());
	         authnStatement.setSessionNotOnOrAfter(now2.plus(input.getMaxSessionTimeoutInMinutes()));
 
	         SAMLObjectBuilder authContextBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(AuthnContext.DEFAULT_ELEMENT_NAME);
	         AuthnContext authnContext = (AuthnContext) authContextBuilder.buildObject();
 
	         SAMLObjectBuilder authContextClassRefBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
	         AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) authContextClassRefBuilder.buildObject();
	         authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:Password"); 
 
			authnContext.setAuthnContextClassRef(authnContextClassRef);
	        authnStatement.setAuthnContext(authnContext);
 
	        // Builder Attributes
	         SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
	         AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();
 
	      // Create the attribute statement
	         Map attributes = input.getAttributes();
	         if(attributes != null){
	        	 Set keySet = attributes.keySet();
	        	 for (Object key : keySet)
				{
	        		 Attribute attrFirstName = buildStringAttribute((String)key, (String)attributes.get(key), getSAMLBuilder());
	        		 attrStatement.getAttributes().add(attrFirstName);
				}
	         }
 
	         // Create the do-not-cache condition
	         SAMLObjectBuilder doNotCacheConditionBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(OneTimeUse.DEFAULT_ELEMENT_NAME);
	         Condition oneTimeUseCondition = (Condition) doNotCacheConditionBuilder.buildObject();
	         
	         SAMLObjectBuilder audienceRestrictionBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
	         Condition audienceRestrictionCondition = (Condition) audienceRestrictionBuilder.buildObject();
	        
	         SAMLObjectBuilder audienceBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(Audience.DEFAULT_ELEMENT_NAME);
	         Audience audience = (Audience) audienceBuilder.buildObject();
	         audience.setAudienceURI(ConfigManager.getSpnamequalifierSpentityidIssuer());
	         ((AudienceRestriction)audienceRestrictionCondition).getAudiences().add(audience);
 
	         SAMLObjectBuilder conditionsBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
	         Conditions conditions = (Conditions) conditionsBuilder.buildObject();
	         conditions.getConditions().add(oneTimeUseCondition);
	         conditions.getConditions().add(audienceRestrictionCondition);
 
	         // Create Issuer
	         SAMLObjectBuilder issuerBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
	         Issuer issuer = (Issuer) issuerBuilder.buildObject();
	         issuer.setValue(input.getStrIssuer());
 
	         // Create the assertion
	         SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder) SAMLWriter.getSAMLBuilder().getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
	         Assertion assertion = (Assertion) assertionBuilder.buildObject();
	         assertion.setID("abcdedf1234567");
	         assertion.setIssuer(issuer);
	         assertion.setIssueInstant(now);
	         assertion.setVersion(SAMLVersion.VERSION_20);
 
	         assertion.getAuthnStatements().add(authnStatement);
	         assertion.getAttributeStatements().add(attrStatement);
	         assertion.setConditions(conditions);
	         assertion.setSubject(subject);
 
			return assertion;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
    /**
    <u>Slightly</u> easier way to create objects using OpenSAML's 
    builder system.
    */
    // cast to SAMLObjectBuilder<T> is caller's choice    
    @SuppressWarnings ("unchecked")
    public <T> T create (Class<T> cls, QName qname)
    {
        return (T) ((XMLObjectBuilder) 
            Configuration.getBuilderFactory ().getBuilder (qname))
                .buildObject (qname);
    }
	
	
    /**
    Helper method to generate a response, based on a pre-built assertion.
    */
    public Response createResponse (Assertion assertion)
        throws IOException, MarshallingException, TransformerException
    {
        return createResponse (assertion, null);
    }
    
    /**
    Helper method to generate a shell response with a given status code
    and query ID.
    */
    public Response createResponse (String statusCode, String inResponseTo)
        throws IOException, MarshallingException, TransformerException
    {
        return createResponse (statusCode, null, inResponseTo);
    }
    
    /**
    Helper method to spawn a new Issuer element based on our issuer URL.
    */
    public Issuer spawnIssuer ()
    {
        Issuer result = null;
        if (ConfigManager.getIdpIssuer() != null)
        {
            result = create (Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
            result.setValue (ConfigManager.getIdpIssuer());
        }
        
        return result;
    }
    
    /**
    Helper method to generate a shell response with a given status code,
    status message, and query ID.
    */
    public Response createResponse 
            (String statusCode, String message, String inResponseTo)
        throws IOException, MarshallingException, TransformerException
    {
        Response response = create 
            (Response.class, Response.DEFAULT_ELEMENT_NAME);
        response.setID ("a1b2c3d4e5d6f7");

        if (inResponseTo != null)
            response.setInResponseTo (inResponseTo);
            
        DateTime now = new DateTime ();
        response.setIssueInstant (now);
        
        if (ConfigManager.getIdpIssuer()  != null)
            response.setIssuer (spawnIssuer ());
        
        StatusCode statusCodeElement = create 
            (StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        statusCodeElement.setValue (statusCode);
        
        Status status = create (Status.class, Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode (statusCodeElement);
        response.setStatus (status);

        if (message != null)
        {
            StatusMessage statusMessage = create 
                (StatusMessage.class, StatusMessage.DEFAULT_ELEMENT_NAME);
            statusMessage.setMessage (message);
            status.setStatusMessage (statusMessage);
        }
        
        return response;
    }
    
    /**
    Helper method to generate a response, based on a pre-built assertion
    and query ID.
    */
    public Response createResponse (Assertion assertion, String inResponseTo)
        throws IOException, MarshallingException, TransformerException
    {
        Response response = 
            createResponse (StatusCode.SUCCESS_URI, inResponseTo);

        response.getAssertions ().add (assertion);
        
        return response;
    }
 
}