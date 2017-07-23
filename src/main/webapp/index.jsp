<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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

    <title>SAML</title>
	
	<meta name="author" content="Wasil Zafar" />
	<meta name="description" content="Application Home Page" />

	
</head>
<body>

<!-- webpage content goes here in the body -->

	<div id="page">
		<div id="logo">
			<h1><a href="/idpsp" id="logoLink">Identity Tester </a></h1>
		</div>
		<div id="content">
			<h2>Application Home</h2>
			<p>
				This is home page for Identity Tester - A sample IDP implementation. Currently working only in SP-Initiated mode</p>
			<p> 
				This web page also acts like a <b>resource at service provider</b> which triggers SAML authentication.
				If successful, we return back to this page (URL sent as relay state).</p>
			<p>Click the buttons below to trigger SAML Authentication</p>	
			<div id="nav">
			<ul>
				<li><a href="/idpsp/trigger?p=redirect">HTTP-Redirect</a></li>
				<li><a href="/idpsp/trigger?p=post">HTTP-POST</a></li>
			</ul>
			</div>
			<p>
				When application starts, properties file is written to Java user home directory. Make changes in the file and click below button to reload </p>
			<div id="nav">
			<ul>
				<li><a href="/idpsp/trigger?p=reload">Reload</a></li>
			</ul>
			</div>	
		</div>
		<div id="footer">
			<p>
				Webpage made by <a href="mailto:wasil.zafar@gmail.com" target="_blank">Wasil Zafar</a>
			</p>
		</div>
	</div>
</body>
</html>