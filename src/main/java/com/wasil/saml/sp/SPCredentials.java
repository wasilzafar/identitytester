package com.wasil.saml.sp;

import org.opensaml.xml.security.*;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wasil.saml.common.ConfigManager;
import com.wasil.saml.idp.Constants;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Privat on 13/05/14.
 */
public class SPCredentials {
	private final static Logger logger = LoggerFactory.getLogger(SPCredentials.class);

    private static final Credential credential;

    static {
        try {
            KeyStore keystore = readKeystoreFromClasspath(ConfigManager.getIDPKeystoreLocation(), ConfigManager.getIDPKeystorePassword());
            Map<String, String> passwordMap = new HashMap<String, String>();
            passwordMap.put(ConfigManager.getIDPCertificateAlias(), ConfigManager.getIDPEntryPassword());
            KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

            Criteria criteria = new EntityIDCriteria(ConfigManager.getIDPCertificateAlias());
            CriteriaSet criteriaSet = new CriteriaSet(criteria);

            credential = resolver.resolveSingle(criteriaSet);
        } catch (org.opensaml.xml.security.SecurityException e) {
            throw new RuntimeException("Something went wrong reading credentials", e);
        }
    }

    private static KeyStore readKeystoreFromClasspath(String pathToKeyStore, String keyStorePassword) {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
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
          	  fis = SPCredentials.class.getResourceAsStream(ConfigManager.getIDPKeystoreLocation().substring(Constants.CLASSPATH.length()));
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
            InputStream inputStream = null;
            
            if(ConfigManager.getIDPKeystoreLocation().startsWith(Constants.CLASSPATH))
            	inputStream = SPCredentials.class.getResourceAsStream(ConfigManager.getIDPKeystoreLocation().substring(Constants.CLASSPATH.length()));
              else if(ConfigManager.getIDPKeystoreLocation().startsWith(Constants.FILESYSTEM))
        		try {
        			inputStream = new FileInputStream(ConfigManager.getIDPKeystoreLocation().substring(Constants.FILESYSTEM.length()));
        		} catch (FileNotFoundException e1) {
        			logger.error("Keystore file missing! "+e1);
        		}
            ks.load(inputStream, keyStorePassword.toCharArray());
            inputStream.close();
            return ks;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong reading keystore", e);
        }
    }

    public static Credential getCredential() {
        return credential;
    }
}
