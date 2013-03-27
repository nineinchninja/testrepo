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
<title>Gladiator Market</title>
</head>
<body>
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Available New Recruits</span>
<hr>

<div id="navbar"></div>
<script>$("#navbar").load("/admin/navbar.html");</script>
<hr>
<h2>Your available funds: ${UserData.ludus.availableGold} gold</h2>
<hr>
<table class="table-results">
<tr>
	<th class="table-header">Gender</th>
	<th class="table-header">Strength</th>
	<th class="table-header">Agility</th>
	<th class="table-header">Speed</th>
	<th class="table-header">Intelligence</th>
	<th class="table-header">Constitution</th>
	<th class="table-header">Willpower</th>
	<th class="table-header">Price (gold)</th>
</tr>
<c:forEach var="gladiator" items='${Recruits.gladiators}'>
<tr class="table-results">
<form action="/buygladiator" method="POST">
	<input type="hidden" name="gladKey" value='${gladiator.key}'/>
	<td ><c:out value='${gladiator.gender}' /></td>
	<td ><c:out value='${gladiator.strengthString}' /></td>
	<td ><c:out value='${gladiator.agilityString}' /></td>
	<td ><c:out value='${gladiator.speedString}' /></td>
	<td ><c:out value='${gladiator.intelligenceString}' /></td>
	<td ><c:out value='${gladiator.constitutionString}' /></td>
	<td ><c:out value='${gladiator.willpowerString}' /></td>
	<td ><c:out value='${gladiator.price}' /></td>
	<td> <input type="Submit" value="buy" class="otherbutton" ></td>
</form>
</tr>
</c:forEach>

</table>
</body>
</html>