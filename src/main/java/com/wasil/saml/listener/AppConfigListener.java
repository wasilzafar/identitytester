package com.wasil.saml.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;

import com.wasil.saml.common.ConfigManager;

public class AppConfigListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {		
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
