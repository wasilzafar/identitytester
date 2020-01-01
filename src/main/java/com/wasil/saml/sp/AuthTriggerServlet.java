package com.wasil.saml.sp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wasil.saml.common.CertificateManager;
import com.wasil.saml.common.ConfigManager;
import com.wasil.saml.idp.Constants;

public class AuthTriggerServlet extends HttpServlet {
	
	private final static Logger logger = LoggerFactory.getLogger(AuthTriggerServlet.class);
	
	public static final String REDIRECT = "redirect";
	public static final String POST = "post";
	public static final String ARTIFACT = "artifact";
	public static final String RELOAD = "reload";
	
	private CertificateManager certManager = new CertificateManager();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String param = req.getParameter("p");
		if (param != null) {
			if (param.equalsIgnoreCase(REDIRECT)) {
				resp.sendRedirect(getRedirectURL(buildAuthnRequest(false)));
			} else if(param.equalsIgnoreCase(POST)){
				req.setAttribute("assertion", buildAuthnRequest(true));
				req.setAttribute("url", ConfigManager.getSinglesignonDestinationUrl());
				req.setAttribute("parameterName", "SAMLRequest");
				req.setAttribute("protocol", "continueWithPost");
				req.getRequestDispatcher("post.jsp").forward(req,resp);
			} else if(param.equalsIgnoreCase(ARTIFACT)){
				req.setAttribute("assertion", buildAuthnRequest(true));
				req.setAttribute("url", ConfigManager.getSinglesignonDestinationUrl());
				req.setAttribute("parameterName", "SAMLRequest");
				req.setAttribute("protocol", "continueWithArtifact");
				req.getRequestDispatcher("post.jsp").forward(req,resp);
			} else if(param.equalsIgnoreCase(RELOAD)){
				Properties props = new Properties();
				props.setProperty(ConfigManager.IDP_ISSUER, req.getParameter(ConfigManager.IDP_ISSUER));
				props.setProperty(ConfigManager.ACS_RECIPIENT_URL, req.getParameter(ConfigManager.ACS_RECIPIENT_URL));
				props.setProperty(ConfigManager.IDP_CERTIFICATE_ALIAS, req.getParameter(ConfigManager.IDP_CERTIFICATE_ALIAS));
				props.setProperty(ConfigManager.SP_CERTIFICATE_ALIAS, req.getParameter(ConfigManager.SP_CERTIFICATE_ALIAS));
				props.setProperty(ConfigManager.IDP_ENTRY_PASSWORD, req.getParameter(ConfigManager.IDP_ENTRY_PASSWORD));
				props.setProperty(ConfigManager.SP_ENTRY_PASSWORD, req.getParameter(ConfigManager.SP_ENTRY_PASSWORD));
				props.setProperty(ConfigManager.IDP_ISSUER_QUALIFIER, req.getParameter(ConfigManager.IDP_ISSUER_QUALIFIER));
				props.setProperty(ConfigManager.IDP_KEYSTORE_LOCATION, req.getParameter(ConfigManager.IDP_KEYSTORE_LOCATION));
				props.setProperty(ConfigManager.SP_KEYSTORE_LOCATION, req.getParameter(ConfigManager.SP_KEYSTORE_LOCATION));
				props.setProperty(ConfigManager.RELAY_STATE, req.getParameter(ConfigManager.RELAY_STATE));
				props.setProperty(ConfigManager.SINGLESIGNON_DESTINATION_URL, req.getParameter(ConfigManager.SINGLESIGNON_DESTINATION_URL));
				props.setProperty(ConfigManager.SPNAMEQUALIFIER_SPENTITYID_ISSUER, req.getParameter(ConfigManager.SPNAMEQUALIFIER_SPENTITYID_ISSUER));
				props.setProperty(ConfigManager.ASSERTION_RESOLUTION_SERVICE_URL, req.getParameter(ConfigManager.ASSERTION_RESOLUTION_SERVICE_URL));
				props.setProperty(ConfigManager.IDP_KEYSTORE_PASSWORD, req.getParameter(ConfigManager.IDP_KEYSTORE_PASSWORD));
				props.setProperty(ConfigManager.SP_KEYSTORE_PASSWORD, req.getParameter(ConfigManager.SP_KEYSTORE_PASSWORD));
				ConfigManager.unload(props);
				ConfigManager.load();
				resp.sendRedirect("/idpsp");
			}
		}
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String param = req.getParameter("p");
		if (param != null) {
			if (param.equalsIgnoreCase(REDIRECT)) {
				resp.sendRedirect(getRedirectURL(buildAuthnRequest(false)));
			} else if(param.equalsIgnoreCase(POST)){
				req.setAttribute("assertion", buildAuthnRequest(true));
				req.setAttribute("url", ConfigManager.getSinglesignonDestinationUrl());
				req.setAttribute("parameterName", "SAMLRequest");
				req.setAttribute("protocol", "continueWithPost");
				req.getRequestDispatcher("post.jsp").forward(req,resp);
			} else if(param.equalsIgnoreCase(ARTIFACT)){
				req.setAttribute("assertion", buildAuthnRequest(true));
				req.setAttribute("url", ConfigManager.getSinglesignonDestinationUrl());
				req.setAttribute("parameterName", "SAMLRequest");
				req.setAttribute("protocol", "continueWithArtifact");
				req.getRequestDispatcher("post.jsp").forward(req,resp);
			} else if(param.equalsIgnoreCase(RELOAD)){
				Properties props = new Properties();
				props.setProperty(ConfigManager.IDP_ISSUER, req.getParameter(ConfigManager.IDP_ISSUER));
				props.setProperty(ConfigManager.ACS_RECIPIENT_URL, req.getParameter(ConfigManager.ACS_RECIPIENT_URL));
				props.setProperty(ConfigManager.IDP_CERTIFICATE_ALIAS, req.getParameter(ConfigManager.IDP_CERTIFICATE_ALIAS));
				props.setProperty(ConfigManager.SP_CERTIFICATE_ALIAS, req.getParameter(ConfigManager.SP_CERTIFICATE_ALIAS));
				props.setProperty(ConfigManager.IDP_ENTRY_PASSWORD, req.getParameter(ConfigManager.IDP_ENTRY_PASSWORD));
				props.setProperty(ConfigManager.SP_ENTRY_PASSWORD, req.getParameter(ConfigManager.SP_ENTRY_PASSWORD));
				props.setProperty(ConfigManager.IDP_ISSUER_QUALIFIER, req.getParameter(ConfigManager.IDP_ISSUER_QUALIFIER));
				props.setProperty(ConfigManager.IDP_KEYSTORE_LOCATION, req.getParameter(ConfigManager.IDP_KEYSTORE_LOCATION));
				props.setProperty(ConfigManager.SP_KEYSTORE_LOCATION, req.getParameter(ConfigManager.SP_KEYSTORE_LOCATION));
				props.setProperty(ConfigManager.RELAY_STATE, req.getParameter(ConfigManager.RELAY_STATE));
				props.setProperty(ConfigManager.SINGLESIGNON_DESTINATION_URL, req.getParameter(ConfigManager.SINGLESIGNON_DESTINATION_URL));
				props.setProperty(ConfigManager.SPNAMEQUALIFIER_SPENTITYID_ISSUER, req.getParameter(ConfigManager.SPNAMEQUALIFIER_SPENTITYID_ISSUER));
				props.setProperty(ConfigManager.ASSERTION_RESOLUTION_SERVICE_URL, req.getParameter(ConfigManager.ASSERTION_RESOLUTION_SERVICE_URL));
				props.setProperty(ConfigManager.IDP_KEYSTORE_PASSWORD, req.getParameter(ConfigManager.IDP_KEYSTORE_PASSWORD));
				props.setProperty(ConfigManager.SP_KEYSTORE_PASSWORD, req.getParameter(ConfigManager.SP_KEYSTORE_PASSWORD));
				ConfigManager.unload(props);
				ConfigManager.load();
				resp.sendRedirect("/idpsp");
			}
		}
		
	}

	private String generateRandom() {
		Random randomService = new Random();
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 42) {
		    sb.append(Integer.toHexString(randomService.nextInt()));
		}
		sb.setLength(42);
		return sb.toString();
	}
	public String buildAuthnRequest(boolean base64Only){
		String samlRequest = null;
		String messageXML =null;
		byte[] samlAuthRequestDeflated = null;
	    try {	      
	      String randId = generateRandom();
	      
	      IssuerBuilder issuerBuilder = new IssuerBuilder();
	      Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp" );
	      issuer.setValue(ConfigManager.getSpnamequalifierSpentityidIssuer());
	      
	      //Create NameIDPolicy
	      NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
	      NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
	      //nameIdPolicy.setSchemaLocation("urn:oasis:names:tc:SAML:2.0:protocol");
	      nameIdPolicy.setFormat(NameID.TRANSIENT);
	      nameIdPolicy.setSPNameQualifier(ConfigManager.getSpnamequalifierSpentityidIssuer());
	      nameIdPolicy.setAllowCreate(true);
	      
	      //Create AuthnContextClassRef
	      AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
	      AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "AuthnContextClassRef", "saml");
	      authnContextClassRef.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
	      //Marshaller accrMarshaller = org.opensaml.Configuration.getMarshallerFactory().getMarshaller(authnContextClassRef);
	      //org.w3c.dom.Element authnContextClassRefDom = accrMarshaller.marshall(authnContextClassRef);
	      
	      
	      //Create RequestedAuthnContext
	      RequestedAuthnContextBuilder requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
	      RequestedAuthnContext requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
	      requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
	      requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
	      //requestedAuthnContext.setDOM(authnContextClassRefDom);
	      //authnContextClassRef.
	      //.setParent((XMLObject) requestedAuthnContext);
	      
	    
	      Signature sign = createSignature();
	      DateTime issueInstant = new DateTime();
	      AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
	      AuthnRequest authRequest = authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol", "AuthnRequest", "samlp");
	      authRequest.setForceAuthn(false);
	      authRequest.setIsPassive(false);
	      authRequest.setIssueInstant(issueInstant);
	      authRequest.setDestination(ConfigManager.getSinglesignonDestinationUrl());
	      //authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
	      authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
	      authRequest.setAssertionConsumerServiceURL(ConfigManager.getAcsRecipientUrl());
	      authRequest.setIssuer(issuer); 
	      authRequest.setNameIDPolicy(nameIdPolicy); 
	      authRequest.setRequestedAuthnContext(requestedAuthnContext); 
	      authRequest.setID(randId); 
	      authRequest.setVersion(SAMLVersion.VERSION_20);
	      authRequest.setSignature(sign);
	      String stringRep = authRequest.toString();
	      System.out.println("AuthTriggerServlet: AuthnRequestImpl: " + stringRep);
	      
	      
	      Marshaller marshaller = org.opensaml.Configuration.getMarshallerFactory().getMarshaller(authRequest);
	      org.w3c.dom.Element authDOM = marshaller.marshall(authRequest);
	      
	      try
	      {
	         Signer.signObject(sign);
	      }
	      catch (SignatureException e)
	      {
	         e.printStackTrace();
	      }
	      
	      StringWriter rspWrt = new StringWriter();
	      XMLHelper.writeNode(authDOM, rspWrt);
	      messageXML = rspWrt.toString();

	      Deflater deflater = new Deflater(Deflater.DEFLATED, true);
	      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); 
	      DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
	      deflaterOutputStream.write(messageXML.getBytes()); 
	      deflaterOutputStream.close();
	      samlAuthRequestDeflated = byteArrayOutputStream.toByteArray();
	      samlRequest = Base64.encodeBytes(samlAuthRequestDeflated, Base64.DONT_BREAK_LINES);
	      System.out.println("AuthTriggerServlet: Marshalled AuthRequest: " + messageXML);
	          
	    } catch (MarshallingException e) {
	      e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    } catch (Throwable e) {
			e.printStackTrace();
		} finally{
	    }
	    if(base64Only)
	    	return Base64.encodeBytes(messageXML.getBytes(), Base64.DONT_BREAK_LINES);
	    else
	    	return samlRequest;
	  }
	
	private String getRedirectURL(String samlRequest){
		 String actionURL = ConfigManager.getSinglesignonDestinationUrl();
	      System.out.println("AuthTriggerServlet: SAML auth request deflated and base64 encoded: " + samlRequest);
	      String url = null;
		try {
			url = actionURL + "?SAMLRequest=" + URLEncoder.encode(samlRequest,"UTF-8") + "&RelayState=" + ConfigManager.getRelayState();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	      System.out.println("AuthTriggerServlet: URL - "+url);	      
	      return url;
		
	}
	
	private Signature createSignature() throws Throwable {
		if (ConfigManager.getSPKeystoreLocation() != null) {			
			Signature authRequestSignature = null;
			Credential cred = certManager.getSigningCredential(ConfigManager.getSPKeystorePassword(), ConfigManager.getSPEntryPassword(), Constants.ENTITY_SP);
			authRequestSignature = (Signature) Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
			authRequestSignature.setSigningCredential(cred);
			authRequestSignature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
			authRequestSignature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		      SecurityConfiguration secConfig = Configuration.getGlobalSecurityConfiguration();
		      try
		      {
		         SecurityHelper.prepareSignatureParams(authRequestSignature, cred, secConfig, null);
		      }
		      catch (SecurityException e)
		      {
		         e.printStackTrace();
		      }
		      catch (org.opensaml.xml.security.SecurityException e)
		      {
		         e.printStackTrace();
		      }
			return authRequestSignature;
		}
		
		return null;
	}
	
	
	}