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
<title>Recent Results</title>
</head>
<body>
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Recent Tournament Results</span>
<hr>

<div id="navbar"></div>
<script>$("#navbar").load("/admin/navbar.html");</script>
<hr>

<table class="table-results">
<c:forEach var="tournament" items="${ResultsBeanData}">
	<tr>
		<th class="table-header" colspan="4">Tournament held ${tournament.eventDate}</th>
	</tr>
	<tr>
		<th class="table-header">Challenger</th>
		<th class="table-header">Incumbant</th>
		<th class="table-header">Winner</th>	
	</tr>
	<c:forEach var="match" items="${tournament.results}">
	<form method="post" action="/results">
		<tr>
		<input type="hidden" name="resultKey" value="${match.resultKey}" />
			<td >${match.challengerName}</td>
			<td >${match.incumbantName}</td>			
			<td >${match.winner}</td>	
			<td><input class="otherbutton" type="submit" value="View Details"/></td>			
		</tr>
		</form>
	</c:forEach>
	
</c:forEach></table>