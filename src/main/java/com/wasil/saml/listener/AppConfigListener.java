package com.wasil.saml.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;

import com.wasil.saml.common.ConfigManager;

public class AppConfigListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		//System.setProperty("http.proxyHost", "localhost");
		//System.setProperty("http.proxyPort", "8888");
		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
		ConfigManager.load();
		try {
		    DefaultBootstrap.bootstrap();
		}
		catch (ConfigurationException ce) {
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {

	}

}
