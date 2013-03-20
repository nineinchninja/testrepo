<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<link href="/stylesheets/main.css" type="text/css" rel="stylesheet" />
<title>Recent Results</title>
</head>
<body>
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Recent Tournament Results</span>
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
<c:forEach var="tournament" items="${ResultsBeanData}">
	<tr>
		<th class="table-header" colspan="4">Tournament ${tournament.eventDate}</th>
	</tr>
	<tr>
		<th class="table-header">Challenger</th>
		<th class="table-header">Incumbant</th>
		<th class="table-header">Number of Rounds</th>
		<th class="table-header">Outcome</th>	
	</tr>
	<c:forEach var="match" items="${tournament.results}">
		<tr>	
			<td >${match.challengerName}</td>
			<td >${match.incumbantName}</td>
			<td >${match.round}</td>
			<td >${match.winner}</td>			
		</tr>
	</c:forEach>
</c:forEach></table>