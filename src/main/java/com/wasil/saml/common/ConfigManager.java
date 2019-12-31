package com.wasil.saml.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wasil.saml.idp.Constants;

public class ConfigManager {
	
	private final static Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	
	public static final String APPLICATION_PROPERTIES = "classpath:///com/wasil/resources/app.properties";
	
	public static final String SP_KEYSTORE_LOCATION = "sp.keystore.location";
	public static final String SP_KEYSTORE_PASSWORD = "sp.keystore.password";
	public static final String SP_ENTRY_PASSWORD = "sp.keystore.entry.password";
	public static final String SP_CERTIFICATE_ALIAS = "sp.certificate.alias";
	
	
	public static final String IDP_KEYSTORE_LOCATION = "idp.keystore.location";
	public static final String IDP_KEYSTORE_PASSWORD = "idp.keystore.password";
	public static final String IDP_ENTRY_PASSWORD = "idp.keystore.entry.password";
	public static final String IDP_CERTIFICATE_ALIAS = "idp.certificate.alias"; 
	
	public static final String SINGLESIGNON_DESTINATION_URL = "singlesignon-destination.url";
	public static final String RELAY_STATE = "relay.state";
	public static final String SPNAMEQUALIFIER_SPENTITYID_ISSUER = "spnamequalifier.spentityid.issuer";
	public static final String IDP_ISSUER = "idp.issuer";
	public static final String ACS_RECIPIENT_URL = "acs.recipient.url";
	public static final String ASSERTION_RESOLUTION_SERVICE_URL = "ars.url";
	public static final String IDP_ISSUER_QUALIFIER = "idp.issuer.qualifier";

	public static final String propertiesFileString = System.getProperty("user.home") + "/"
			+ APPLICATION_PROPERTIES.substring(APPLICATION_PROPERTIES.lastIndexOf("/"));
	static File propertiesFile = new File(propertiesFileString);
	
	private static Properties  prop = new Properties();
	
	public static Properties getProp() {
		Properties newProperties = new Properties();
		newProperties.putAll(prop);
		return newProperties;
	}

	public static void load(){
        boolean loaded = false;		
		if (propertiesFile.exists()) {
			InputStream in;
			try {
				in = new FileInputStream(propertiesFile);
				prop.load(in);
				in.close();
			} catch (Exception e1) {
				// Exception while loading existing properties file from file system.					
			} 
			loaded = true;
		} 
		if(!loaded) {
			InputStream in = ConfigManager.class.getResourceAsStream(APPLICATION_PROPERTIES.substring(Constants.CLASSPATH.length()));
			try {
				prop.load(in);
				in.close();
			} catch (Exception e) {
				// Exception while loading default properties file.				
			}
		}
		
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(propertiesFile);
			prop.store(new FileOutputStream(propertiesFile), "Current configurations");
		} catch (Exception e) {
			// Exception while storing properties on file system.
		}finally {
			try {
				os.close();
			} catch (IOException e) {
			}
		}
		

		logger.info("Loaded properties : ");
		Enumeration keys = prop.keys();
		while (keys.hasMoreElements()) {
		    String key = (String)keys.nextElement();
		    String value = (String)prop.get(key);
		    logger.info(key + ": " + value);
		}
	}
	
	public static void unload(Properties props){
		FileOutputStream os = null;
	try {
		os = new FileOutputStream(propertiesFile);
		props.store(new FileOutputStream(propertiesFile), "Current configurations");
	} catch (Exception e) {
		// Exception while storing properties on file system.
	}finally {
		try {
			os.close();
		} catch (IOException e) {
		}
	}
	
		
	}
	
	public static String get(String property) {
		return prop.getProperty(property);
	}


	public static String getIDPKeystoreLocation() {
		return prop.getProperty(IDP_KEYSTORE_LOCATION);
	}

	public static String getIDPKeystorePassword() {
		return prop.getProperty(IDP_KEYSTORE_PASSWORD);
	}

	public static String getIDPEntryPassword() {
		return prop.getProperty(IDP_ENTRY_PASSWORD);
	}
	
	public static String getSPKeystoreLocation() {
		return prop.getProperty(SP_KEYSTORE_LOCATION);
	}

	public static String getSPKeystorePassword() {
		return prop.getProperty(SP_KEYSTORE_PASSWORD);
	}

	public static String getSPEntryPassword() {
		return prop.getProperty(SP_ENTRY_PASSWORD);
	}

	public static String getSinglesignonDestinationUrl() {
		return prop.getProperty(SINGLESIGNON_DESTINATION_URL);
	}

	public static String getRelayState() {
		return prop.getProperty(RELAY_STATE);
	}

	public static String getSpnamequalifierSpentityidIssuer() {
		return prop.getProperty(SPNAMEQUALIFIER_SPENTITYID_ISSUER);
	}

	public static String getIdpIssuer() {
		return prop.getProperty(IDP_ISSUER);
	}

	public static String getAcsRecipientUrl() {
		return prop.getProperty(ACS_RECIPIENT_URL);
	}

	public static String getAssertionResolutionServiceUrl() {
		return prop.getProperty(ASSERTION_RESOLUTION_SERVICE_URL);
	}

	public static String getIdpIssuerQualifier() {
		return prop.getProperty(IDP_ISSUER_QUALIFIER);
	}

	public static String getIDPCertificateAlias() {
		return prop.getProperty(IDP_CERTIFICATE_ALIAS);
	}
	
	public static String getSPCertificateAlias() {
		return prop.getProperty(SP_CERTIFICATE_ALIAS);
	}
}
