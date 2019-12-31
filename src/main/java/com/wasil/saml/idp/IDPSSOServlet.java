package com.wasil.saml.idp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Artifact;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.ArtifactBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.wasil.saml.common.ConfigManager;
import com.wasil.saml.common.SAMLInputContainer;
import com.wasil.saml.common.SignAssertion;

public class IDPSSOServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory.getLogger(IDPSSOServlet.class);
	
	public IDPSSOServlet() throws Exception {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.getWriter().print("<html><body><p>IDPSSOServlet: Served at: " + request.getContextPath()+"</p>");
		response.getWriter().print("<p>"+"IDPSSOServlet: SAMLRequest parameter recieved ! "+"</p>");
		String carryOnProcessing = request.getParameter("continue");
		if (carryOnProcessing != null){ 
			if(carryOnProcessing.equalsIgnoreCase("continueWithPost")||carryOnProcessing.equalsIgnoreCase("continueWithRedirect"))
				processAndForwardResponse(request.getParameter("PreservedAuthRequest"), request, response, true);
			else
				sendArtifact(request.getParameter("PreservedAuthRequest"), request, response);
		}else
			displayAuthnRequest(request.getParameter("SAMLRequest"), request, response);

	}

	private void sendArtifact(String parameter, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(ConfigManager.getAcsRecipientUrl()+ "?SAMLart=AAQAAMFbLinlXaCM%2BFIxiDwGOLAy2T71gbpO7ZhNzAgEANlB90ECfpNEVLg%3D");
        		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.getWriter().print("<html><body><p>IDPSSOServlet: Served at: " + request.getContextPath()+"</p>");
		response.getWriter().print("<p>"+"IDPSSOServlet: SAMLRequest parameter recieved ! "+"</p>");
		String carryOnProcessing = request.getParameter("continue");
		if (carryOnProcessing != null){ 
			if(carryOnProcessing.equalsIgnoreCase("continueWithPost")||carryOnProcessing.equalsIgnoreCase("continueWithRedirect"))
				processAndForwardResponse(request.getParameter("PreservedAuthRequest"), request, response, true);
			else
				sendArtifact(request.getParameter("PreservedAuthRequest"), request, response);
		}else
			displayAuthnRequest(request.getParameter("SAMLRequest"), request, response);

	}

	private void processAndForwardResponse(String SAMLRequest, HttpServletRequest request, HttpServletResponse response,
			boolean post) {
		try {
			String originalRequest = SAMLRequest+"";
			AuthnRequest samlRequestObject = processRequest(SAMLRequest, request);
			String outSAMLResponse = prepareResponse(samlRequestObject);
			
			Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); 
		      DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
		      deflaterOutputStream.write(outSAMLResponse.getBytes()); 
		      deflaterOutputStream.close();
		      byte[] samlResponse = byteArrayOutputStream.toByteArray();
		      String base64Encoded = Base64.encodeBytes(samlResponse, Base64.DONT_BREAK_LINES);
		      String url = ConfigManager.getAcsRecipientUrl() + "?SAMLResponse=" + URLEncoder.encode(base64Encoded,"UTF-8") + "&RelayState=" + ConfigManager.getRelayState();
		      if(post){
		    	  request.setAttribute("assertion", Base64.encodeBytes(outSAMLResponse.getBytes(), Base64.DONT_BREAK_LINES));
		    	  request.setAttribute("url", ConfigManager.getAcsRecipientUrl());
		    	  request.setAttribute("parameterName", "SAMLResponse");
		    	  request.getRequestDispatcher("post.jsp").forward(request,response);
		      }else
		    	  response.sendRedirect(url);
		} catch (Exception e) {
			logger.error("Exception while processing GET Request from SP. ");
			logger.error("The error is : " + e.toString());
		}

	}

	public void displayAuthnRequest(String SAMLRequest, HttpServletRequest request, HttpServletResponse response) {
		try {
			String originalRequest = SAMLRequest+"";
			AuthnRequest samlRequestObject = processRequest(SAMLRequest, request);
			String stringResponse = prepareResponse(samlRequestObject);
			request.setAttribute(AuthnRequest.ASSERTION_CONSUMER_SERVICE_URL_ATTRIB_NAME, samlRequestObject.getAssertionConsumerServiceURL());
			request.setAttribute(AuthnRequest.ID_ATTRIB_NAME, samlRequestObject.getID());
			request.setAttribute(AuthnRequest.ISSUE_INSTANT_ATTRIB_NAME, samlRequestObject.getIssueInstant().toString());
			request.setAttribute(ConfigManager.SPNAMEQUALIFIER_SPENTITYID_ISSUER, samlRequestObject.getIssuer().getValue().toLowerCase().trim());
			request.setAttribute(ConfigManager.SINGLESIGNON_DESTINATION_URL,ConfigManager.getSinglesignonDestinationUrl());
			request.setAttribute(Response.DEFAULT_ELEMENT_LOCAL_NAME, stringResponse);
			request.setAttribute("originalRequest", originalRequest);
			request.getRequestDispatcher("idp.jsp").forward(request,response);
		} catch (Exception e) {
			logger.error("Exception while processing GET Request from SP. ");
			logger.error("The error is : " + e.toString());
		}

	}
	
	public void processAndForwardResponse(String SAMLRequest, HttpServletRequest request, HttpServletResponse response) {
		processAndForwardResponse(SAMLRequest, request, response, false);
	}
	
	private AuthnRequest processRequest(String SAMLRequest, HttpServletRequest request) throws ParserConfigurationException, UnmarshallingException, SAXException, IOException {
		logger.info("The BASE64 Encoded SAML Request is : " + SAMLRequest);
		byte[] decodedSAMLRequestBytes = Base64.decode(SAMLRequest);
		SAMLRequest = new String(decodedSAMLRequestBytes, "UTF-8");
		logger.info("The base 64 decoded SAML Request ( deflated ) is : " + SAMLRequest);
		logger.info("The Accept-Encoding header data is : " + request.getHeader("Accept-Encoding"));
		if (request.getHeader("Accept-Encoding").contains("deflate")) {
			try {
				Inflater inflater = new Inflater(true);
				inflater.setInput(decodedSAMLRequestBytes);
				byte[] xmlMessageBytes = new byte[5000];
				int resultLength = inflater.inflate(xmlMessageBytes);
				inflater.end();
				SAMLRequest = new String(xmlMessageBytes, 0, resultLength, "UTF-8");
				logger.info("The deflated SAMLRequest is : " + SAMLRequest);
			} catch (Exception e) {
				logger.warn(
						"Exception during inflation attempt. Data might be already deflated - HTTP POST Binding."+e);
			}
		}
		ByteArrayInputStream is = new ByteArrayInputStream(SAMLRequest.getBytes("UTF-8"));
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.parse(is);
		Element element = document.getDocumentElement();
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
		XMLObject responseXmlObj = unmarshaller.unmarshall(element);
		AuthnRequest samlRequestObject = (AuthnRequest) responseXmlObj;
		SAMLRequest = samlRequestObject.toString();
		logger.info("The received SAML Request is : " + SAMLRequest);
		return samlRequestObject;
	}

	public String prepareResponse(AuthnRequest authRequest) throws Exception{
	      SAMLInputContainer input = new SAMLInputContainer();
			input.setStrIssuer(ConfigManager.getIdpIssuer());
			input.setStrNameID("zafar");
			input.setSessionId(authRequest.getID());
			input.setStrNameQualifier(ConfigManager.getIdpIssuerQualifier());

			Map customAttributes = new HashMap();
			customAttributes.put("firstName", "Sahil");
			customAttributes.put("lastName", "Shamsi");

			input.setAttributes(customAttributes);
			return new SignAssertion().createSignedResponse(input);
	}
	
	
	public Artifact buildArtifact() {
	    String artifactId = UUID.randomUUID().toString().replaceAll("-", "");
	    ArtifactBuilder artifactBuilder = new ArtifactBuilder();	    
	    Artifact artifact = (Artifact) artifactBuilder.buildObject();
	    artifact.setArtifact(artifactId);
	    return artifact;
	  }
}