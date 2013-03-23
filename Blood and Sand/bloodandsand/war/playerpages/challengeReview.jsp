<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<link href="/stylesheets/main.css" type="text/css" rel="stylesheet" />
<title>Challenges for upcoming events</title>
</head>
<body>
<a href="/logout" class="login-link">logout</a>
<span class="main-title">Your Current Challenges for the Next Tournament</span>
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
<h3>Next tournament: ${nextTournament}</h3>
<hr>
<h3>Challenges awaiting a decision</h3>
<c:forEach var="gladiator" items="${UserData.ludus.gladiators}">
	<table class="table-results" border="1">
		<caption><h3>${gladiator.name}</h3></caption>
		<hr>	

		<c:choose>
			<c:when test="${gladiator.challenges != null}">
				<tr>
					<th class="table-header">Challenger</th><th class="table-header">Challenged</th><th class="table-header">Wager</th><th class="table-header">Status</th><th class="table-header">Action</th>
				</tr>	
				<c:forEach var="challenge" items="${gladiator.challenges}">
					<form action="/challenges" method="POST" ><input type="hidden" name="accepted" value="${challenge.gladiatorChallengeKey}"/>
						<tr><td>${challenge.challenger.name}</td><td>${challenge.incumbant.name}</td><td>${challenge.wager}</td><td>${challenge.status}</td>
							<c:choose>
								<c:when test="${challenge.challenger.name != gladiator.name && challenge.status !='ACCEPTED' && challenge.status !='DECLINED'}">
									<td><input type="submit" class="otherbutton" value="Accept"/></td>
								</c:when>
								<c:when test="${challenge.status =='ACCEPTED'}">
									<td>Waiting for tournament</td>
								</c:when>
								<c:when test="${challenge.status =='DECLINED'}">
									<td>N/A</td>
								</c:when>
								<c:otherwise>
									<td>Awaiting Opponent</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</form>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<tr><td colspan="4">No outstanding challenges</td></tr>
			</c:otherwise>
		</c:choose>
	</table>
</c:forEach>
<hr>

<div class="basediv">	<a class="otherbutton" href="/createChallenge">Click here to send a new challenge!</a></div>

</body>
</html>