package com.wasil.saml.common;

import java.util.Map;

public class SAMLInputContainer
{

	private String strIssuer;
	private String strNameID;
	private String strNameQualifier;
	private String sessionId;
	private int maxSessionTimeoutInMinutes = 15; // default is 15 minutes

	private Map attributes;

	/**
	 * Returns the strIssuer.
	 *
	 * @return the strIssuer
	 */
	public String getStrIssuer()
	{
		return strIssuer;
	}

	/**
	 * Sets the strIssuer.
	 *
	 * @param strIssuer
	 *            the strIssuer to set
	 */
	public void setStrIssuer(String strIssuer)
	{
		this.strIssuer = strIssuer;
	}

	/**
	 * Returns the strNameID.
	 *
	 * @return the strNameID
	 */
	public String getStrNameID()
	{
		return strNameID;
	}

	/**
	 * Sets the strNameID.
	 *
	 * @param strNameID
	 *            the strNameID to set
	 */
	public void setStrNameID(String strNameID)
	{
		this.strNameID = strNameID;
	}

	/**
	 * Returns the strNameQualifier.
	 *
	 * @return the strNameQualifier
	 */
	public String getStrNameQualifier()
	{
		return strNameQualifier;
	}

	/**
	 * Sets the strNameQualifier.
	 *
	 * @param strNameQualifier
	 *            the strNameQualifier to set
	 */
	public void setStrNameQualifier(String strNameQualifier)
	{
		this.strNameQualifier = strNameQualifier;
	}

	/**
	 * Sets the attributes.
	 *
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map attributes)
	{
		this.attributes = attributes;
	}

	/**
	 * Returns the attributes.
	 *
	 * @return the attributes
	 */
	public Map getAttributes()
	{
		return attributes;
	}

	/**
	 * Sets the sessionId.
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	/**
	 * Returns the sessionId.
	 * @return the sessionId
	 */
	public String getSessionId()
	{
		return sessionId;
	}

	/**
	 * Sets the maxSessionTimeoutInMinutes.
	 * @param maxSessionTimeoutInMinutes the maxSessionTimeoutInMinutes to set
	 */
	public void setMaxSessionTimeoutInMinutes(int maxSessionTimeoutInMinutes)
	{
		this.maxSessionTimeoutInMinutes = maxSessionTimeoutInMinutes;
	}

	/**
	 * Returns the maxSessionTimeoutInMinutes.
	 * @return the maxSessionTimeoutInMinutes
	 */
	public int getMaxSessionTimeoutInMinutes()
	{
		return maxSessionTimeoutInMinutes;
	}

}