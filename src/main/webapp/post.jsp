<%@ page language="java" contentType="text/html"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<link rel="shortcut icon" href="/idpsp/favicon.ico" type="image/x-icon">
<link rel="icon" href="/idpsp/favicon.ico" type="image/x-icon">
    <title>POST data</title>
</head>
<body onload="document.getElementsByTagName('input')[0].click();">

    <noscript>
        <p><strong>Note:</strong> Since your browser does not support JavaScript, you must
            press the button below once to proceed.</p>
    </noscript>

    <form method="post" action="<% out.println(request.getAttribute("url")); %>">
        <input type="submit" style="display:none;" />
        <input type="hidden" name="<%=request.getAttribute("parameterName")%>" value="<%=request.getAttribute("assertion")%>" />
        <input type="hidden" name="protocol" value="<%=request.getAttribute("protocol")%>" />

        <noscript>
            <input type="submit" value="Submit" />
        </noscript>

    </form>
</body>
</html>