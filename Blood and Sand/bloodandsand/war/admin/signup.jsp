<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>Sign Up</title>
<link href="/stylesheets/main.css" type="text/css" rel="stylesheet" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

</head>
<body>
<a href="/" class="main-title">Home page</a>
<hr>
<div class="signup-area">
<form method="post" action="/signup">
<h1>Sign up!</h1>
<table align="center">
<tr>
	<td><label>Choose a User Name</td>
		<td><input type="text" name="username" value="<c:out value='${username}'/>"autofocus="autofocus">
	</label></td>
    <td><label style="color: red"><c:out value='${errorMessageName}' /></label></td>
</tr>
<tr>
	<td><label>Password</td>
		<td><input type="password" name = "password" >
	</label></td>
    <td><label style="color: red"><c:out value='${errorMessagePassword}' /></label></td>
</tr>
<tr>
<td><label>Verify Password</td>
<td><input type="password" name="verify">
	</label></td>
</tr>
<tr>
<td><label>Email address</td>
<td><input type="text" name = "email" value="<c:out value='${emailAddress}' />">
	</label></td>
	<td><label style="color: red"><c:out value='${errorMessageEmail}' /></label></td>
</tr>
<tr>
<td colspan=3>
<input type="submit"></td>
</tr>
<tr>
<td colspan=3>
<a href="/login">Already have an account?</a></td>
</tr>
</table>
</form>
</div>
</body>
</html>