<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Dots and Boxes - Lobby</title>
</head>
<body>
	<h1>Autenticação no Jogo</h1>
	<% if (request.getAttribute("erro") != null) { %>
		<p style="color: red;"><b>Erro:</b> <%= request.getAttribute("erro") %></p>
	<% } %>
	
	<form action="login" method="POST">
		<label>Alcunha (Nickname):</label><br>
		<input type="text" name="nickname" required><br><br>
		
		<label>Palavra-passe:</label><br>
		<input type="password" name="password" required><br><br>
		
		<input type="submit" value="Entrar no Jogo">
	</form>
	
	<p>Ainda não tem conta? <a href="registo.jsp">Registe-se aqui</a></p>
</body>
</html>	