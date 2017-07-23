<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.wasil.saml.common.ConfigManager,
org.opensaml.saml2.core.AuthnRequest,
org.opensaml.saml2.core.Response"  %>
<!DOCTYPE html>
<html>
<head>
<link rel="shortcut icon" href="/idpsp/favicon.ico" type="image/x-icon">
<link rel="icon" href="/idpsp/favicon.ico" type="image/x-icon">
<style type="text/css">

/*
* multi-line comment
*/
p{ line-height: 1em; }
h1, h2, h3, h4{
    color: orange;
	font-weight: normal;
	line-height: 1.1em;
	margin: 0 0 .5em 0;
}
h1{ font-size: 1.7em; }
h2{ font-size: 1.5em; }
a{
	color: black;
	text-decoration: none;
}
	a:hover,
	a:active{ text-decoration: underline; }

/* you can structure your code's white space so that it is as readable for when you come back in the future or for other people to read and edit quickly */

body{
    font-family: arial; font-size: 80%; line-height: 1.2em; width: 100%; margin: 0; background: #eee;
}
/* you can put your code all in one line like above */
#page{ margin: 20px; }

/* or on different lines like below */
#logo{
	width: 35%;
	margin-top: 5px;
	font-family: georgia;
	display: inline-block;
}
/* but try and be as concise as possible */
#nav{
	display: inline-block;
	text-align: right;
}
	#nav ul{}
		#nav ul li{
			display: inline-block;
			height: 62px;
		}
			#nav ul li a{
				padding: 20px;
				background: orange;
				color: white;
			}
			#nav ul li a:hover{
				background-color: #ffb424;
				box-shadow: 0px 1px 1px #666;
			}
			#nav ul li a:active{ background-color: #ff8f00; }

#content{
	margin: 30px 0;
	background: white;
	padding: 20px;
	clear: both;
}
#footer{
	border-bottom: 1px #ccc solid;
	margin-bottom: 10px;
}
	#footer p{
		text-align: right;
		text-transform: uppercase;
		font-size: 80%;
		color: grey;
	}

/* multiple styles seperated by a , */
#content,
ul li a{ box-shadow: 0px 1px 1px #999; }

</style>
<!-- your webpage info goes here -->

    <title>SAML IDP Page</title>
	
	<meta name="author" content="Wasil Zafar" />
	<meta name="description" content="Identity Provider Home" />

	
</head>
<body>

<!-- webpage content goes here in the body -->

	<div id="page">
		<div id="logo">
			<h1><a href="/idpsp" id="logoLink">Identity Tester </a></h1>
		</div>
		<div id="content">
			<h2>IDP Home</h2>
			<p>
				This is landing page for a sample IDP implementation</p>
			<p> 
				This page is analogous to IDP login page. </p>
			<p>	But here it gives useful information about SAML authentication request received. Press 'Continue' button at the bottom.</p>	
			
			<table style="width:100%">
  <tr>
    <th>Property</th>
    <th>Value</th> 
  </tr>
  <tr>
    <td><%=AuthnRequest.ASSERTION_CONSUMER_SERVICE_URL_ATTRIB_NAME%></td>
    <td><%=request.getAttribute(AuthnRequest.ASSERTION_CONSUMER_SERVICE_URL_ATTRIB_NAME) %></td>
  </tr>
  <tr>
    <td><%=AuthnRequest.ID_ATTRIB_NAME%></td>
    <td><%=request.getAttribute(AuthnRequest.ID_ATTRIB_NAME) %></td>
  </tr>
  <tr>
    <td><%=AuthnRequest.ISSUE_INSTANT_ATTRIB_NAME%></td>
    <td><%=request.getAttribute(AuthnRequest.ISSUE_INSTANT_ATTRIB_NAME) %></td>
  </tr>
    <tr>
    <td>Issuer - Entity ID</td>
    <td><%=request.getAttribute(ConfigManager.SPNAMEQUALIFIER_SPENTITYID_ISSUER) %></td>
  </tr>
    <tr>
    <td>Destination URL</td>
    <td><%=request.getAttribute(ConfigManager.SINGLESIGNON_DESTINATION_URL) %></td>
  </tr>
 
</table>
<div style="overflow-y: scroll">
			<p>Response be like : </p><b><%=request.getAttribute(Response.DEFAULT_ELEMENT_LOCAL_NAME).toString().replace("<", "&lt;").replace(">", "&gt;") %></b>
</div>  	<br>
			<form action='<%=ConfigManager.getSinglesignonDestinationUrl()%>'>
			<input type='hidden' name='PreservedAuthRequest' value='<%=request.getAttribute("originalRequest")%>'>
			<input type='submit' title='Click to continue ....' name='continue' value='Continue ...'/>
			</form>

			
		</div>
		<div id="footer">
			<p>
				Webpage created by <a href="mailto:wasil.zafar@gmail.com" target="_blank">Wasil Zafar</a>
			</p>
		</div>
	</div>
</body>
</html>