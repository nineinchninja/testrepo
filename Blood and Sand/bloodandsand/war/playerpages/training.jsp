<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<link href="/stylesheets/main.css" type="text/css" rel="stylesheet" />
<title>Training</title>
</head>
<body>
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Your Training Centre</span>
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
<hr>
<table class="table-results">
<tr>
	<th class="table-header">Name</th>
	<th class="table-header">Strength</th>
	<th class="table-header">Agility</th>
	<th class="table-header">Speed</th>
	<th class="table-header">Intelligence</th>
	<th class="table-header">Constitution</th>
	<th class="table-header">Willpower</th>
	<th class="table-header">Sword</th>
	<th class="table-header">Great Sword</th>
	<th class="table-header">Great Axe</th>
	<th class="table-header">Daggers</th>
	<th class="table-header">Spear</th>
	<th class="table-header">Quarterstaff</th>
	<th class="table-header">Hand to Hand</th>
</tr>
<c:forEach var="gladiator" items='${UserData.ludus.gladiators}'>
<tr class="table-results">	
	<td ><c:out value='${gladiator.name}' /></td>
	<td ><c:out value='${gladiator.strength}' /></td>
	<td ><c:out value='${gladiator.agility}' /></td>
	<td ><c:out value='${gladiator.speed}' /></td>
	<td ><c:out value='${gladiator.intelligence}' /></td>
	<td ><c:out value='${gladiator.constitution}' /></td>
	<td ><c:out value='${gladiator.willpower}' /></td>
	<td ><c:out value='${gladiator.weaponSkills.sword}' /></td>	
	<td ><c:out value='${gladiator.weaponSkills.greatsword}'></c:out></td>	
	<td><c:out value="${gladiator.weaponSkills.greataxe}"></c:out></td>
	<td><c:out value="${gladiator.weaponSkills.daggers}"></c:out></td>
	<td><c:out value="${gladiator.weaponSkills.spear}"></c:out></td>
	<td><c:out value="${gladiator.weaponSkills.quarterstaff}"></c:out></td>
	<td><c:out value="${gladiator.weaponSkills.handToHand}"></c:out></td></tr>
</c:forEach></table>
<br>
<hr>
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
		<td><c:out value="${gladiator.name}" /></td>
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
				  <option value="HandToHand">Hand to Hand Skill</option>
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