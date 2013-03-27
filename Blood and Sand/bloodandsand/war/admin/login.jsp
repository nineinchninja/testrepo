<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="/stylesheets/main.css" type="text/css" rel="stylesheet" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<title>Login Page</title>
</head>
<body>
<a href="/" class="main-title">Home</a>
<hr>
<div class="signup-area">
<form method="post" action="/login">
<h1>Login!</h1>
<table align="center">
<tr>
	<td><label>Username</td>
		<td><input class="default" type="text" name="username" value="<c:out value='${username}' />"  autofocus="autofocus">
	</label></td>
</tr>
<tr>
	<td><label>Password</td>
		<td><input type="password" name = "password">
	</label></td>
</tr>
<tr>
    <td colspan=2><label class="error"><c:out value='${LoginError}'/></label></td>
</tr>
<tr>
<td colspan=2><input type="submit"></td>
</tr>
<tr>
<td colspan=2><a href="/signup">Create an account</a></td>
</tr>
</table>
</form>
</div>
</body>
</html>