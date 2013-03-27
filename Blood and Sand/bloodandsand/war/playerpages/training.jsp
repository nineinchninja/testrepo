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
<title>Training</title>
</head>
<body>
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Your Training Centre</span>
<hr>

<div id="navbar"></div>
<script>$("#navbar").load("/admin/navbar.html");</script>
<hr>
<h1>Attributes</h1>
<table class="table-results" caption="Attributes">
<tr>
	<th class="table-header">Name</th>
	<th class="table-header">Strength</th>
	<th class="table-header">Agility</th>
	<th class="table-header">Speed</th>
	<th class="table-header">Intelligence</th>
	<th class="table-header">Constitution</th>
	<th class="table-header">Willpower</th>
</tr>
<c:forEach var="gladiator" items='${UserData.ludus.gladiators}'>
	<tr class="table-results">	
		<td ><c:out value='${gladiator.capitalizedName}' /></td>
		<td ><c:out value='${gladiator.strengthString}' /></td>
		<td ><c:out value='${gladiator.agilityString}' /></td>
		<td ><c:out value='${gladiator.speedString}' /></td>
		<td ><c:out value='${gladiator.intelligenceString}' /></td>
		<td ><c:out value='${gladiator.constitutionString}' /></td>
		<td ><c:out value='${gladiator.willpowerString}' /></td>
	</tr>
</c:forEach>
</table>
<hr>
<h1>Weapon Skills</h1>
<table class="table-results" caption="Weapon Skills">
	<tr>
		<th class="table-header">Name</th>
		<th class="table-header">Sword</th>
		<th class="table-header">Great Sword</th>
		<th class="table-header">Great Axe</th>
		<th class="table-header">Daggers</th>
		<th class="table-header">Spear</th>
		<th class="table-header">Quarterstaff</th>
		<th class="table-header">Maul</th>
	</tr>
<c:forEach var="gladiator" items='${UserData.ludus.gladiators}'>
	<tr class="table-results">
		<td ><c:out value='${gladiator.capitalizedName}'/></td>

		<td ><c:out value='${gladiator.weaponSkills.swordString}' /></td>	
		<td ><c:out value='${gladiator.weaponSkills.greatswordString}'/></td>	
		<td><c:out value="${gladiator.weaponSkills.greataxeString}"/></td>
		<td><c:out value="${gladiator.weaponSkills.daggersString}"/></td>
		<td><c:out value="${gladiator.weaponSkills.spearString}"/></td>
		<td><c:out value="${gladiator.weaponSkills.quarterstaffString}"/></td>
		<td><c:out value="${gladiator.weaponSkills.maulString}"/></td>
	</tr>
</c:forEach>
</table>
<br>
<hr>
<h1>Training Assignments</h1>
<form method="post" action="/gladiatortraining">
<table class="table-results">
	<tr>
		<th class="table-header">Name</th>
		<th class="table-header">Current training focus</th>
		<th class="table-header">Started</th>
		<th class="table-header">New Assignment</th>		
	</tr>
	
	<c:forEach var="gladiator" items='${UserData.ludus.gladiators}'>
	
	<tr>	
		<input type="hidden" name="gladKey" value="${gladiator.key}" />
		<td><c:out value="${gladiator.capitalizedName}" /></td>
		<td><c:out value="${gladiator.currentTrainingFocus }" /></td>
		<td><c:out value="${gladiator.lastTrainingChangeDate}" /></td>
		<td><select name="${gladiator.key}">
				  <option value="No change" selected="selected">No Change</option>
				  <option value="Strength">Strength Training</option>
				  <option value="Agility">Agility Training</option>
				  <option value="Speed">Speed Training</option>
				  <option value="Intelligence">Intelligence Training</option>
				  <option value="Constitution">Constitution Training</option>
				  <option value="Willpower">Willpower Training</option>
				  <option value="Sword">Sword Skill</option>
				  <option value="Greatsword">Great Sword Skill</option>
				  <option value="Greataxe">Great Axe Skill</option>
				  <option value="Daggers">Dagger Skill</option>
				  <option value="Spear">Spear Skill</option>
				  <option value="Quarterstaff">Quarterstaff Skill</option>
				  <option value="Maul">Maul</option>
				  <option value="None">No training</option>
			</select>
		</td>
	</tr>
	</c:forEach>
	<tr><th colspan="4" align="right" class="table-bottom"><input class="otherbutton" type="submit" value="Update Training"/></th></tr>
</table>
</form>		

	
</body>
</html>