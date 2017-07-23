package com.wasil.saml.sp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wasil.saml.common.ConfigManager;

public class AuthTriggerServlet extends HttpServlet {
	
	private final static Logger logger = LoggerFactory.getLogger(AuthTriggerServlet.class);
	
	public static final String REDIRECT = "redirect";
	public static final String POST = "post";
	public static final String RELOAD = "reload";
	
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
				req.getRequestDispatcher("post.jsp").forward(req,resp);
			} else if(param.equalsIgnoreCase(RELOAD)){
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
				req.getRequestDispatcher("post.jsp").forward(req,resp);
			} else if(param.equalsIgnoreCase(RELOAD)){
				ConfigManager.load();
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
	      nameIdPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
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
	      
	    
	      
	      DateTime issueInstant = new DateTime();
	      AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
	      AuthnRequest authRequest = authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol", "AuthnRequest", "samlp");
	      authRequest.setForceAuthn(false);
	      authRequest.setIsPassive(false);
	      authRequest.setIssueInstant(issueInstant);
	      authRequest.setDestination(ConfigManager.getSinglesignonDestinationUrl());
	      //authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
	      authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
	      authRequest.setAssertionConsumerServiceURL(ConfigManager.getAcsRecipientUrl());
	      authRequest.setIssuer(issuer); 
	      authRequest.setNameIDPolicy(nameIdPolicy); 
	      authRequest.setRequestedAuthnContext(requestedAuthnContext); 
	      authRequest.setID(randId); 
	      authRequest.setVersion(SAMLVersion.VERSION_20);
	      String stringRep = authRequest.toString();
	      System.out.println("AuthTriggerServlet: AuthnRequestImpl: " + stringRep);
	      
	      
	      Marshaller marshaller = org.opensaml.Configuration.getMarshallerFactory().getMarshaller(authRequest);
	      org.w3c.dom.Element authDOM = marshaller.marshall(authRequest);
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
}
