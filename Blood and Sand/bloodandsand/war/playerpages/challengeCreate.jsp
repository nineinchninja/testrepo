<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<link href="/stylesheets/main.css" type="text/css" rel="stylesheet" />
<title>Challenge Another Gladiator</title>
</head>
<body>


<!-- navigation bar -->
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Challenge a Gladiator</span>
<hr>

<div id="navbar"></div>
<script>$("#navbar").load("/admin/navbar.html");</script>

<hr>

<form method="post" action="/createChallenge" id="challengeform">
<table >
<tr><td>

	<h3>Select your gladiator</h3>


<select name="challenger" autofocus="autofocus" class="largeselect" required="required" size="10">
	<c:forEach var="gladiator" items="${MyChallengers}">
	
		<option>${gladiator.capitalizedName} </option>	

	</c:forEach>
</select>

</td>
<td >

	<h3>Select your opponent</h3>


<select name="opponent"  class="largeselect" required="required" size="10">
	<c:forEach var="gladiator" items="${Opponents}">
	
		<option class="largetext">${gladiator.capitalizedName} </option>	

	</c:forEach>
</select>

</td>
<td>

	<h3>Enter a wager (optional)</h3>
	<INPUT type="text" name="wagerAmount" class="wagerfield" value="0"/>

	

</td>
</tr>
<hr>
<tr><td><br />Total gold available: <c:out value='${UserData.ludus.availableGold}' />
<br />Total gold in wagers: <c:out value='${UserData.ludus.wageredGold}' /><td colspan="2"><div class="basediv">	<input type="submit" class="otherbutton" form="challengeform" value="Send your challenge to your opponent"></div></td>
</tr>
<hr>
</table>
 </form>
</body>
</html>