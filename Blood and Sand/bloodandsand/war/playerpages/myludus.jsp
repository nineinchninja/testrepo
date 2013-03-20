<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />

<title>My Ludus</title>
<link href="/stylesheets/main.css" type="text/css" rel="stylesheet" />

</head>
<body>
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Welcome to your Ludus ${UserData.userName}</span>
<hr>
<table class="table-navigation">
<tr>
	<td><a href="/login" class="navigation-link">Home</a></td>
	<td><a href="/gladiatormarket" class="navigation-link">Buy new gladiators</a></td>
	
	<td><a href="/gladiatortraining" class="navigation-link">Train and manage your gladiators</td>
	<td><a href="/challenges" class="navigation-link">Arrange fights</a></td>
	<td><a href="/results" class="navigation-link">Recent Results</a></td>
</tr>
</table>
<h2>The status of your gladiator business:</h2>
<h3>Finances: </h3>
<br />Total gold available: <c:out value='${UserData.ludus.availableGold}' />
<br />Total gold in wagers: <c:out value='${UserData.ludus.wageredGold}' />
<br />Total weekly costs: <c:out value='${UserData.ludus.weeklyCost}' />
	<h3>Recent Results</h3>
<hr>
<table class="table-results">
<tr>
<th class="table-header">Gladiator</th><th class="table-header">Match Type</th><th class="table-header">Result</th><th class="table-header">Winnings</th>
</tr>

</table>
<h3>Gladiators</h3>
<hr>
<c:set var="nameneeded" scope="session" value="False"/>
<c:set var="none" scope="session" value="No name selected"/>
<c:forEach var="gladiator" items='${UserData.ludus.gladiators}'>
<c:choose>
	<c:when test="${gladiator.name == none}">
	<c:set var="nameneeded" scope="session" value="True"/>
	</c:when>
</c:choose>
</c:forEach>
<c:if test="${nameneeded == 'True'}">
	<form action="/namechange" method="post">
</c:if>
<table class="table-results">
<tr>
<th class="table-header">Name</th><th class="table-header">Status</th><th class="table-header">Wins</th><th class="table-header">Losses</th><th class="table-header">Ties</th><th class="table-header">Popularity</th>
</tr>
<c:forEach var="gladiator" items='${UserData.ludus.gladiators}'>

<tr class="table-results">
<td >
	<c:choose>
		<c:when test="${gladiator.name == none}">
		<input type="text" name='${gladiator.key}' placeholder="Enter a name and hit enter"/>
		</c:when>
		<c:otherwise>
			<c:out value='${gladiator.name}' />
		</c:otherwise>
	</c:choose>
</td>
<td ><c:out value='${gladiator.status}' /></td>
<td ><c:out value='${gladiator.wins}' /></td>
<td ><c:out value='${gladiator.losses}' /></td>
<td ><c:out value='${gladiator.ties}' /></td>
<td ><c:out value='${gladiator.popularity}' /></td>
</tr>
</c:forEach>
<c:if test="${nameneeded == 'True'}">
<tr><td colspan="2"><label class="error"> <c:out value="${nameError}"/></label></td>
<td colspan="3"><input type="submit" value="Apply name(s) to your gladiator(s)"/>
</td>
</tr>
</c:if>
</table>
<c:if test="${nameneeded == 'True'}">
	</form>
</c:if>
</body>
</html>