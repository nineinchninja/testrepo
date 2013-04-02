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
<title>Match Description</title>
</head>
<body>
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Recent Tournament Results</span>
<hr>

<div id="navbar"></div>
<script>$("#navbar").load("/admin/navbar.html");</script>
<hr>
<table cellpadding="10">
<tr>
		<th class="table-header" colspan="3">Match held ${ResultsDetailData.matchDate}</th>
	</tr>
	<tr>
		<th class="table-header">${ResultsDetailData.challengerName}</th>
		<th class="table-header">Versus</th>
		<th class="table-header">${ResultsDetailData.incumbantName}</th>			
	</tr>
	<tr>
		<th colspan = 3>Potential Winnings: ${ResultsDetailData.winnings}</th>
	</tr>
	<tr>
		<td colspan="3" cellpadding="20"><div class="match-detail" >${ResultsDetailData.fightDescription}</div></td>
	</tr>
	<tr><td class="match-detail"> ${ResultsDetailData.challengerStats}</td><td></td><td class="match-detail">${ResultsDetailData.incumbantStats}</td>
	</tr>
	
</table>
</body>
</html>