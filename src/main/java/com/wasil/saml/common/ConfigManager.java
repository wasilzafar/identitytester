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

public class ConfigManager {
	
	private final static Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	
	public static final String APPLICATION_PROPERTIES = "/com/wasil/resources/app.properties";
	
	public static final String KEYSTORE_LOCATION = "idp.keystore.location";
	public static final String KEYSTORE_PASSWORD = "idp.keystore.password";
	public static final String ENTRY_PASSWORD = "idp.keystore.entry.password";
	public static final String CERTIFICATE_ALIAS = "certificate.alias";
	
	public static final String SINGLESIGNON_DESTINATION_URL = "singlesignon-destination.url";
	public static final String RELAY_STATE = "relay.state";
	public static final String SPNAMEQUALIFIER_SPENTITYID_ISSUER = "spnamequalifier.spentityid.issuer";
	public static final String IDP_ISSUER = "idp.issuer";
	public static final String ACS_RECIPIENT_URL = "acs.recipient.url";
	public static final String IDP_ISSUER_QUALIFIER = "idp.issuer.qualifier";

	private static Properties  prop = new Properties();
	
	public static Properties getProp() {
		Properties newProperties = new Properties();
		newProperties.putAll(prop);
		return newProperties;
	}

	public static void load(){
        boolean loaded = false;
		String userHome = System.getProperty("user.home");
		String propertiesFileString = userHome + "/"
				+ APPLICATION_PROPERTIES.substring(APPLICATION_PROPERTIES.lastIndexOf("/"));
		File propertiesFile = new File(propertiesFileString);
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
			InputStream in = ConfigManager.class.getResourceAsStream(APPLICATION_PROPERTIES);
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

	public static String getKeystoreLocation() {
		return prop.getProperty(KEYSTORE_LOCATION);
	}

	public static String getKeystorePassword() {
		return prop.getProperty(KEYSTORE_PASSWORD);
	}

	public static String getEntryPassword() {
		return prop.getProperty(ENTRY_PASSWORD);
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

	public static String getIdpIssuerQualifier() {
		return prop.getProperty(IDP_ISSUER_QUALIFIER);
	}

	public static String getCertificateAlias() {
		return prop.getProperty(CERTIFICATE_ALIAS);
	}
}
