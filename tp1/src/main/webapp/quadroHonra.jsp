<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.w3c.dom.*" %>
<%@ page import="java.util.*" %>
<%
	if (session.getAttribute("user") == null) {
	    response.sendRedirect("login.jsp");
	    return;
	}
	
	String xmlPath = (String) application.getAttribute("xmlPath");
	Document doc = XMLReader.loadXML(xmlPath);
	NodeList listaJogadores = doc.getElementsByTagName("jogador");
	
	List<Map<String, Object>> dadosJogadores = new ArrayList<>();
	
	for (int i = 0; i < listaJogadores.getLength(); i++) {
	    Element jog = (Element) listaJogadores.item(i);
	    
	    String nick = jog.getElementsByTagName("nickname").item(0).getTextContent();
	    String foto = jog.getElementsByTagName("foto").item(0).getTextContent();
	    String nac = jog.getElementsByTagName("nacionalidade").item(0).getTextContent();
	    
	    int vitorias = Integer.parseInt(jog.getElementsByTagName("vitorias").item(0).getTextContent());
	    int derrotas = Integer.parseInt(jog.getElementsByTagName("derrotas").item(0).getTextContent());
	    long tempoTotal = Long.parseLong(jog.getElementsByTagName("tempoTotal").item(0).getTextContent());
	    
	    int jogosTotais = vitorias + derrotas;
	    double tempoMedio = 0.0;
	    if (jogosTotais > 0) {
	        tempoMedio = (double) tempoTotal / jogosTotais;
	    }
	
	    Map<String, Object> mapa = new HashMap<>();
	    mapa.put("nick", nick);
	    mapa.put("foto", foto);
	    mapa.put("nac", nac.toLowerCase().trim());
	    mapa.put("vitorias", vitorias);
	    mapa.put("derrotas", derrotas);
	    mapa.put("tempoMedio", tempoMedio);
	    
	    dadosJogadores.add(mapa);
	}
	
	dadosJogadores.sort((j1, j2) -> Integer.compare((int)j2.get("vitorias"), (int)j1.get("vitorias")));
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dots and Boxes - Quadro de Honra</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 30px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }
        th { background-color: #f2f2f2; }
        .avatar { width: 50px; height: 50px; border-radius: 50%; object-fit: cover; }
        .flag { width: 30px; vertical-align: middle; }
    </style>
</head>
<body>

    <h2>Quadro de Honra</h2>
    <a href="lobby.jsp">Voltar ao Lobby</a>
    
    <table>
        <thead>
            <tr>
                <th>Posição</th>
                <th>Foto</th>
                <th>Nickname</th>
                <th>Nacionalidade</th>
                <th>Vitórias</th>
                <th>Derrotas</th>
                <th>Tempo Médio (s)</th>
            </tr>
        </thead>
        <tbody>
            <% 
                int posicao = 1;
                for (Map<String, Object> j : dadosJogadores) { 
            %>
                <tr>
                    <td><strong><%= posicao++ %>º</strong></td>
                    <td>
                        <img class="avatar" src="imagens/fotos/<%= j.get("foto") %>" 
                             onerror="this.src='imagens/fotos/default.jpg';" alt="Foto">
                    </td>
                    <td><%= j.get("nick") %></td>
                    <td>
                        <img class="flag" src="https://flagcdn.com/w40/<%= j.get("nac") %>.png" 
                             alt="<%= j.get("nac") %>" title="<%= j.get("nac").toString().toUpperCase() %>">
                        <%= j.get("nac").toString().toUpperCase() %>
                    </td>
                    <td><strong><%= j.get("vitorias") %></strong></td>
                    <td><%= j.get("derrotas") %></td>
                    <td><%= String.format("%.2f", j.get("tempoMedio")) %> s</td>
                </tr>
            <% } %>
        </tbody>
    </table>
</body>
</html>