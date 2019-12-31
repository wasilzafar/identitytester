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
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wasil.saml.idp.Constants;

public class CertificateManager {
	private final static Logger logger = LoggerFactory.getLogger(CertificateManager.class);

	public Credential getSigningCredential(String keystorePassword, String privateKeyPassword,
			String entity) throws Throwable {
		KeyStore ks = null;
		InputStream fis = null;
		char[] password = keystorePassword.toCharArray();

		// Get Default Instance of KeyStore
		try {
			ks = KeyStore.getInstance("jks");
		} catch (KeyStoreException e) {
			logger.error("Error while Intializing Keystore", e);
		}
		if (ConfigManager.get(entity == Constants.ENTITY_IDP ? ConfigManager.IDP_KEYSTORE_LOCATION
				: ConfigManager.SP_KEYSTORE_LOCATION).startsWith(Constants.CLASSPATH))
			fis = getClass()
					.getResourceAsStream(
							ConfigManager
									.get(entity == Constants.ENTITY_IDP ? ConfigManager.IDP_KEYSTORE_LOCATION
											: ConfigManager.SP_KEYSTORE_LOCATION)
									.substring(Constants.CLASSPATH.length()));
		else if (ConfigManager.get(entity == Constants.ENTITY_IDP ? ConfigManager.IDP_KEYSTORE_LOCATION
				: ConfigManager.SP_KEYSTORE_LOCATION).startsWith(Constants.FILESYSTEM))
			try {
				fis = new FileInputStream(
						ConfigManager.get(entity == Constants.ENTITY_IDP ? ConfigManager.IDP_KEYSTORE_LOCATION
								: ConfigManager.SP_KEYSTORE_LOCATION).substring(Constants.FILESYSTEM.length()));
			} catch (FileNotFoundException e1) {
				logger.error("Keystore file missing! " + e1);
			}
		// Load KeyStore
		try {
			ks.load(fis, password);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to Load the KeyStore:: ", e);
		} catch (CertificateException e) {
			logger.error("Failed to Load the KeyStore:: ", e);
		} catch (IOException e) {
			logger.error("Failed to Load the KeyStor7e:: ", e);
		}

		// Close InputFileStream
		try {
			fis.close();
		} catch (IOException e) {
			logger.error("Failed to close file stream:: ", e);
		}

		// Get Private Key Entry From Certificate
		KeyStore.PrivateKeyEntry pkEntry = null;
		try {
			pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(
					entity == Constants.ENTITY_IDP ? ConfigManager.getIDPCertificateAlias()
							: ConfigManager.getSPCertificateAlias(),
					new KeyStore.PasswordProtection(privateKeyPassword.toCharArray()));
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to Get Private Entry From the keystore:: "
					+ ConfigManager.get(entity == Constants.ENTITY_IDP ? ConfigManager.IDP_KEYSTORE_LOCATION
							: ConfigManager.SP_KEYSTORE_LOCATION),
					e);
		} catch (UnrecoverableEntryException e) {
			logger.error("Failed to Get Private Entry From the keystore:: "
					+ ConfigManager.get(entity == Constants.ENTITY_IDP ? ConfigManager.IDP_KEYSTORE_LOCATION
							: ConfigManager.SP_KEYSTORE_LOCATION),
					e);
		} catch (KeyStoreException e) {
			logger.error("Failed to Get Private Entry From the keystore:: "
					+ ConfigManager.get(entity == Constants.ENTITY_IDP ? ConfigManager.IDP_KEYSTORE_LOCATION
							: ConfigManager.SP_KEYSTORE_LOCATION),
					e);
		}
		PrivateKey pk = pkEntry.getPrivateKey();

		X509Certificate certificate = (X509Certificate) pkEntry.getCertificate();
		BasicX509Credential credential = new BasicX509Credential();
		credential.setEntityCertificate(certificate);
		credential.setPrivateKey(pk);

		logger.info("Private Key" + pk.toString());
		logger.info("Certificate :" + certificate.toString());

		return credential;
	}
}
