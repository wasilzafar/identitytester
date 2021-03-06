package com.wasil.saml.sp;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Artifact;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.soap.client.BasicSOAPMessageContext;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.client.http.HttpSOAPClient;
import org.opensaml.ws.soap.common.SOAPException;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.wasil.saml.common.ConfigManager;
import com.wasil.saml.common.OpenSAMLUtils;
import com.wasil.saml.idp.IDPCredentials;

/**
 * Servlet implementation class AssertionConsumerServlet
 */
public class AssertionConsumerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(AssertionConsumerServlet.class);

	public AssertionConsumerServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (request.getParameter("SAMLart") != null) {
			handleArtifactResolution(request, response);
		} else {
			request.setAttribute("originalResponse", request.getParameter("SAMLResponse"));
			request.getRequestDispatcher("sp.jsp").forward(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (request.getParameter("SAMLart") != null) {
			handleArtifactResolution(request, response);
		} else {
			request.setAttribute("originalResponse", request.getParameter("SAMLResponse"));
			request.getRequestDispatcher("sp.jsp").forward(request, response);
		}
	}

	protected void handleArtifactResolution(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("Artifact received");
		Artifact artifact = buildArtifactFromRequest(req);
		logger.info("Artifact: " + artifact.getArtifact());

		ArtifactResolve artifactResolve = buildArtifactResolve(artifact);
		signArtifactResolve(artifactResolve);
		logger.info("Sending ArtifactResolve");
		logger.info("ArtifactResolve: ");
		OpenSAMLUtils.logSAMLObject(artifactResolve);

		ArtifactResponse artifactResponse = sendAndReceiveArtifactResolve(artifactResolve);
		logger.info("ArtifactResponse received");
		logger.info("ArtifactResponse: ");
		OpenSAMLUtils.logSAMLObject(artifactResponse);

		EncryptedAssertion encryptedAssertion = getEncryptedAssertion(artifactResponse);
		Assertion assertion = decryptAssertion(encryptedAssertion);
		verifyAssertionSignature(assertion);
		logger.info("Decrypted Assertion: ");
		OpenSAMLUtils.logSAMLObject(assertion);

		logAssertionAttributes(assertion);
		logAuthenticationInstant(assertion);
		logAuthenticationMethod(assertion);

		setAuthenticatedSession(req);
		redirectToGotoURL(req, resp);
	}

	private Assertion decryptAssertion(EncryptedAssertion encryptedAssertion) {
		StaticKeyInfoCredentialResolver keyInfoCredentialResolver = new StaticKeyInfoCredentialResolver(
				SPCredentials.getCredential());

		Decrypter decrypter = new Decrypter(null, keyInfoCredentialResolver, new InlineEncryptedKeyResolver());
		decrypter.setRootInNewDocument(true);

		try {
			return decrypter.decrypt(encryptedAssertion);
		} catch (DecryptionException e) {
			throw new RuntimeException(e);
		}
	}

	private void verifyAssertionSignature(Assertion assertion) {
		if (!assertion.isSigned()) {
			throw new RuntimeException("The SAML Assertion was not signed");
		}

		try {
			SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
			profileValidator.validate(assertion.getSignature());

			SignatureValidator sigValidator = new SignatureValidator(IDPCredentials.getCredential());

			sigValidator.validate(assertion.getSignature());

			logger.info("SAML Assertion signature verified");
		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}

	}

	private void signArtifactResolve(ArtifactResolve artifactResolve) {
		Signature signature = OpenSAMLUtils.buildSAMLObject(Signature.class);
		signature.setSigningCredential(SPCredentials.getCredential());
		signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
		signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

		artifactResolve.setSignature(signature);

		try {
			Configuration.getMarshallerFactory().getMarshaller(artifactResolve).marshall(artifactResolve);
		} catch (MarshallingException e) {
			throw new RuntimeException(e);
		}

		try {
			Signer.signObject(signature);
		} catch (SignatureException e) {
			throw new RuntimeException(e);
		}
	}

	private void setAuthenticatedSession(HttpServletRequest req) {
		req.getSession().setAttribute("authenticated", true);
	}

	private void redirectToGotoURL(HttpServletRequest req, HttpServletResponse resp) {
		String gotoURL = ConfigManager.getRelayState();
		logger.info("Redirecting to requested URL: " + gotoURL);
		try {
			resp.sendRedirect(gotoURL);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void logAuthenticationMethod(Assertion assertion) {
		logger.info("Authentication method: " + assertion.getAuthnStatements().get(0).getAuthnContext()
				.getAuthnContextClassRef().getAuthnContextClassRef());
	}

	private void logAuthenticationInstant(Assertion assertion) {
		logger.info("Authentication instant: " + assertion.getAuthnStatements().get(0).getAuthnInstant());
	}

	private void logAssertionAttributes(Assertion assertion) {
		for (Attribute attribute : assertion.getAttributeStatements().get(0).getAttributes()) {
			logger.info("Attribute name: " + attribute.getName());
			for (XMLObject attributeValue : attribute.getAttributeValues()) {
				logger.info("Attribute value: " + ((XSString) attributeValue).getValue());
			}
		}
	}

	private EncryptedAssertion getEncryptedAssertion(ArtifactResponse artifactResponse) {
		Response response = (Response) artifactResponse.getMessage();
		return response.getEncryptedAssertions().get(0);
	}

	private ArtifactResponse sendAndReceiveArtifactResolve(final ArtifactResolve artifactResolve) {
		try {
			Envelope envelope = OpenSAMLUtils.wrapInSOAPEnvelope(artifactResolve);

			HttpClientBuilder clientBuilder = new HttpClientBuilder();
			HttpSOAPClient soapClient = new HttpSOAPClient(clientBuilder.buildClient(), new BasicParserPool());

			BasicSOAPMessageContext soapContext = new BasicSOAPMessageContext();
			soapContext.setOutboundMessage(envelope);

			soapClient.send(ConfigManager.getAssertionResolutionServiceUrl(), soapContext);

			Envelope soapResponse = (Envelope) soapContext.getInboundMessage();
			return (ArtifactResponse) soapResponse.getBody().getUnknownXMLObjects().get(0);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (SOAPException e) {
			throw new RuntimeException(e);
		}
	}

	private Artifact buildArtifactFromRequest(final HttpServletRequest req) {
		Artifact artifact = OpenSAMLUtils.buildSAMLObject(Artifact.class);
		artifact.setArtifact(req.getParameter("SAMLart"));
		return artifact;
	}

	private ArtifactResolve buildArtifactResolve(final Artifact artifact) {
		ArtifactResolve artifactResolve = OpenSAMLUtils.buildSAMLObject(ArtifactResolve.class);

		Issuer issuer = com.wasil.saml.common.OpenSAMLUtils.buildSAMLObject(Issuer.class);
		issuer.setValue(ConfigManager.getSpnamequalifierSpentityidIssuer());
		artifactResolve.setIssuer(issuer);

		artifactResolve.setIssueInstant(new DateTime());

		artifactResolve.setID(com.wasil.saml.common.OpenSAMLUtils.generateSecureRandomId());

		artifactResolve.setDestination(ConfigManager.getAssertionResolutionServiceUrl());

		artifactResolve.setArtifact(artifact);

		return artifactResolve;
	}

}
