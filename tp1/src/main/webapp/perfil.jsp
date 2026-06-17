<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.w3c.dom.*, tp2.XMLReader" %>

<%
	String utilizador = (String) session.getAttribute("user");
	if (utilizador == null) {
	    response.sendRedirect("login.jsp");
	    return;
	}
	
	String xmlPath = (String) application.getAttribute("xmlPath");
	Document doc = XMLReader.loadXML(xmlPath);
	
	Element jogador = XMLReader.getJogador(doc, utilizador);
	
	String fotoAtual = "default.jpg";
	if (jogador.getElementsByTagName("foto").getLength() > 0) {
	    fotoAtual = jogador.getElementsByTagName("foto").item(0).getTextContent();
	}
	
	String corAtual = "#ffffff";
	if (jogador.getElementsByTagName("corFundo").getLength() > 0) {
	    corAtual = jogador.getElementsByTagName("corFundo").item(0).getTextContent();
	}
	
	String nacionalidade = "Desconhecida";
	if (jogador.getElementsByTagName("nacionalidade").getLength() > 0) {
	    nacionalidade = jogador.getElementsByTagName("nacionalidade").item(0).getTextContent();
	}
	
	String idade = "0";
	if (jogador.getElementsByTagName("idade").getLength() > 0) {
	    idade = jogador.getElementsByTagName("idade").item(0).getTextContent();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Perfil do Jogador</title>
    <style>
        body {
            background-color: <%= corAtual %>; /* Aplica dinamicamente a cor preferida */
            font-family: Arial, sans-serif;
        }
        .container {
            background: rgba(255, 255, 255, 0.9);
            padding: 20px;
            max-width: 400px;
            margin: 30px auto;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Editar Perfil - <%= utilizador %></h2>
        <a href="lobby.jsp">Voltar ao Lobby</a>
        <br><br>

        <div>
            <p><strong>Foto Atual:</strong></p>
            <img src="imagens/fotos/<%= fotoAtual %>" alt="Foto Perfil" width="120" style="border-radius: 50%;"><br><br>
        </div>

        <form action="atualizarPerfil" method="post" enctype="multipart/form-data">
            <label for="foto">Alterar Foto de Perfil:</label><br>
            <input type="file" id="foto" name="foto" accept="image/*"><br><br>

            <label for="corFundo">Escolha a Cor de Fundo:</label><br>
            <input type="color" id="corFundo" name="corFundo" value="<%= corAtual %>"><br><br>

            <input type="submit" value="Gravar Alterações">
        </form>
    </div>
</body>
</html>