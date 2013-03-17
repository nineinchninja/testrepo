<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<link href="/stylesheets/main.css" type="text/css" rel="stylesheet" />
<title>Challenge Another Gladiator</title>
</head>
<body>


<!-- navigation bar -->
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Challenge a Gladiator</span>
<hr>
<table class="table-navigation">
<tr>
<td><a href="/login" class="navigation-link">Home</a></td>
<td><a href="/gladiatormarket" class="navigation-link">Buy new gladiators</a></td>
<td><a href="/gladiatortraining" class="navigation-link">Train and manage your gladiators</td>
<td><a href="/challenges" class="navigation-link">Arrange fights</a></td>
<td>Manage your school</td>
</tr>
</table>
<hr>

<form method="post" action="/createChallenge" id="challengeform">
<table >
<tr><td>

	<h3>Select your gladiator</h3>


<select name="challenger" autofocus="autofocus" size="5" class="smallselect" required="required">
	<c:forEach var="gladiator" items="${MyChallengers}">
	
		<option>${gladiator.name} </option>	

	</c:forEach>
</select>

</td>
<td rowspan="2">

	<h3>Select your opponent</h3>


<select name="opponent"  size="20" class="largeselect" required="required">
	<c:forEach var="gladiator" items="${Opponents}">
	
		<option class="largetext">${gladiator.name} </option>	

	</c:forEach>
</select>

</td>
<td>

	<h3>Enter a wager (optional)</h3>
	(coming soon)

	

</td>
</tr>
<tr><td></td><td></td><td><div class="basediv">	<input type="submit" class="otherbutton" form="challengeform" value="Send your challenge to your opponent"></div></td>
</tr>
<hr>
</table>
 </form>
</body>
</html>