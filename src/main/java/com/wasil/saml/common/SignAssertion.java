package com.wasil.saml.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.opensaml.Configuration;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.wasil.saml.idp.Constants;
 
public class SignAssertion
{
   private final static Logger logger = LoggerFactory.getLogger(SignAssertion.class);
   private static Credential signingCredential = null;
   final static Signature assertionSignature = null;
 
   @SuppressWarnings("static-access")
   private void intializeCredentials()
   {
      KeyStore ks = null;
      InputStream fis = null;
      char[] password = ConfigManager.getIDPKeystorePassword().toCharArray();
 
      // Get Default Instance of KeyStore
      try
      {
         ks = KeyStore.getInstance("jks");
      }
      catch (KeyStoreException e)
      {
         logger.error("Error while Intializing Keystore", e);
      }
      if(ConfigManager.getIDPKeystoreLocation().startsWith(Constants.CLASSPATH))
    	  fis = getClass().getResourceAsStream(ConfigManager.getIDPKeystoreLocation().substring(Constants.CLASSPATH.length()));
      else if(ConfigManager.getIDPKeystoreLocation().startsWith(Constants.FILESYSTEM))
		try {
			fis = new FileInputStream(ConfigManager.getIDPKeystoreLocation().substring(Constants.FILESYSTEM.length()));
		} catch (FileNotFoundException e1) {
			logger.error("Keystore file missing! "+e1);
		}
      // Load KeyStore
      try
      {
         ks.load(fis, password);
      }
      catch (NoSuchAlgorithmException e)
      {
         logger.error("Failed to Load the KeyStore:: ", e);
      }
      catch (CertificateException e)
      {
         logger.error("Failed to Load the KeyStore:: ", e);
      }
      catch (IOException e)
      {
         logger.error("Failed to Load the KeyStor7e:: ", e);
      }
 
      // Close InputFileStream
      try
      {
         fis.close();
      }
      catch (IOException e)
      {
         logger.error("Failed to close file stream:: ", e);
      }
 
      // Get Private Key Entry From Certificate
      KeyStore.PrivateKeyEntry pkEntry = null;
      try
      {
         pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(ConfigManager.getIDPCertificateAlias(), new KeyStore.PasswordProtection(
        		 ConfigManager.getIDPEntryPassword().toCharArray()));
      }
      catch (NoSuchAlgorithmException e)
      {
         logger.error("Failed to Get Private Entry From the keystore:: " + ConfigManager.getIDPKeystoreLocation(), e);
      }
      catch (UnrecoverableEntryException e)
      {
         logger.error("Failed to Get Private Entry From the keystore:: " + ConfigManager.getIDPKeystoreLocation(), e);
      }
      catch (KeyStoreException e)
      {
         logger.error("Failed to Get Private Entry From the keystore:: " + ConfigManager.getIDPKeystoreLocation(), e);
      }
      PrivateKey pk = pkEntry.getPrivateKey();
 
      X509Certificate certificate = (X509Certificate) pkEntry.getCertificate();
      BasicX509Credential credential = new BasicX509Credential();
      credential.setEntityCertificate(certificate);
      credential.setPrivateKey(pk);
      signingCredential = credential;
 
      logger.info("Private Key" + pk.toString());
 
   }
 
   public static String createSignedResponse(SAMLInputContainer responseConfig) throws Exception
   {
      SignAssertion sign = new SignAssertion();
      sign.intializeCredentials();
      Signature assertionSignature = null;
      assertionSignature = (Signature) Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
      assertionSignature.setSigningCredential(signingCredential);
      assertionSignature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
      assertionSignature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
      SecurityConfiguration secConfig = Configuration.getGlobalSecurityConfiguration();
      try
      {
         SecurityHelper.prepareSignatureParams(assertionSignature, signingCredential, secConfig, null);
      }
      catch (SecurityException e)
      {
         e.printStackTrace();
      }
      catch (org.opensaml.xml.security.SecurityException e)
      {
         e.printStackTrace();
      }
      
      Signature responseSignature = null;
      responseSignature = (Signature) Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
      responseSignature.setSigningCredential(signingCredential);
      responseSignature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
      responseSignature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
      try
      {
         SecurityHelper.prepareSignatureParams(responseSignature, signingCredential, secConfig, null);
      }
      catch (SecurityException e)
      {
         e.printStackTrace();
      }
      catch (org.opensaml.xml.security.SecurityException e)
      {
         e.printStackTrace();
      }
 
      Response resp = new SAMLWriter().getSamlResponse(responseConfig);
      resp.setSignature(responseSignature);
      Assertion assertion = (Assertion)resp.getAssertions().get(0);
      assertion.setSignature(assertionSignature);
      ((SAMLObjectContentReference)assertionSignature.getContentReferences().get(0)).setDigestAlgorithm(EncryptionConstants.ALGO_ID_DIGEST_SHA256);
      ((SAMLObjectContentReference)responseSignature.getContentReferences().get(0)).setDigestAlgorithm(EncryptionConstants.ALGO_ID_DIGEST_SHA256);
      
      
      Element plain = null;
      
      try
      {
         ResponseMarshaller marshaller = new ResponseMarshaller();
         plain = marshaller.marshall(resp);
      }
      catch (MarshallingException e)
      {
         e.printStackTrace();
      }
      
      
      try
      {
         Signer.signObject(assertionSignature);
         Signer.signObject(responseSignature);
      }
      catch (SignatureException e)
      {
         e.printStackTrace();
      }
      
      
      
      
      String samlResponse = XMLHelper.nodeToString(plain);
      logger.info("Generated signed response :\n" + samlResponse);
	  return samlResponse;
 
   }
}