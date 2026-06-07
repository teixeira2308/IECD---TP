<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String utilizador = (String) session.getAttribute("user");
	if (utilizador == null) {
		response.sendRedirect("login.jsp");
	}
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Dots and Boxes - Lobby</title>
</head>
<body>
	<h1>Bem Vindo, <%= utilizador %>!</h1>
	
	<ul>
        <li><a href="perfil.jsp">Alterar Dados do Perfil</a></li>
        <li><a href="procura.jsp">Procurar Oponentes (AutoComplete)</a></li>
        <li><a href="jogos.jsp">Partidas Ativas / Jogar</a></li>
        <li><a href="quadroHonra.jsp">Quadro de Honra</a></li>
        <li><a href="logout">Terminar Sessão (Logout)</a></li>
    </ul>
</body>
</html>		