<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.w3c.dom.*" %>
<%@ page import="java.util.*, tp2.XMLReader" %>
<%!
	// A tua função global declarada corretamente
	public String obterEmojiBandeira(String nacionalidade) {
	    if (nacionalidade == null) return "🏳️";
	    
	    String nac = nacionalidade.trim().toLowerCase();
	    String codigoPais = "";
	    
	    // Mapeamento de nomes completos para códigos ISO de 2 letras
	    if (nac.equals("portugal")) {
	        codigoPais = "PT";
	    } else if (nac.equals("brasil") || nac.equals("brazil")) {
	        codigoPais = "BR";
	    } else if (nac.equals("espanha") || nac.equals("spain")) {
	        codigoPais = "ES";
	    } else if (nac.equals("frança") || nac.equals("franca") || nac.equals("france")) {
	        codigoPais = "FR";
	    } else if (nac.equals("angola")) {
	        codigoPais = "AO";
	    } else if (nac.equals("moçambique") || nac.equals("mocambique")) {
	        codigoPais = "MZ";
	    } else if (nac.equals("itália") || nac.equals("italia") || nac.equals("italy")) {
	        codigoPais = "IT";
	    } else if (nac.equals("reino unido") || nac.equals("uk") || nac.equals("england")) {
	        codigoPais = "GB";
	    } else if (nac.length() == 2) {
	        codigoPais = nac.toUpperCase();
	    } else {
	        return "🏳️"; 
	    }
	    
	    try {
	        int cp1 = Character.codePointAt(codigoPais, 0) - 'A' + 0x1F1E6;
	        int cp2 = Character.codePointAt(codigoPais, 1) - 'A' + 0x1F1E6;
	        return new String(Character.toChars(cp1)) + new String(Character.toChars(cp2));
	    } catch (Exception e) {
	        return "🏳️";
	    }
	}
%>
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
	    mapa.put("nac", nac); // Mantemos o nome tal como vem do XML
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
        
        /* Ajuste crucial para os SVGs do Twemoji não estourarem o layout */
        img.emoji {
            height: 1.3em;
            width: 1.3em;
            margin: 0 .1em 0 .1em;
            vertical-align: -0.15em;
        }
        .emoji-flag { font-size: 24px; vertical-align: middle; margin-right: 8px; }
    </style>
    
    <script src="https://cdn.jsdelivr.net/npm/@twemoji/api@14.1.0/dist/twemoji.min.js" crossorigin="anonymous"></script>
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
                       <%
                       String fotoJogador = (String) j.get("foto");
                       String urlFoto = "imagens/fotos/" + fotoJogador;
                       
                       if (fotoJogador == null || fotoJogador.equals("default.jpg") || fotoJogador.trim().isEmpty()) {
                    	   urlFoto = "https://www.w3schools.com/howto/img_avatar.png";
                       }
                       %>
                       <img class="avatar" src="<%= urlFoto %>" alt="Foto">
                    </td>
                    <td><%= j.get("nick") %></td>
                    <td>
                        <%
                        // CORRIGIDO: Agora chamamos a tua função inteligente!
                        String paisXml = (String) j.get("nac");
                        String emojiBandeira = obterEmojiBandeira(paisXml);
                        %>
                        <span class="emoji-flag" title="<%= paisXml %>"><%= emojiBandeira %></span>
                        <%= paisXml %>
                    </td>
                    <td><strong><%= j.get("vitorias") %></strong></td>
                    <td><%= j.get("derrotas") %></td>
                    <td><%= String.format("%.2f", j.get("tempoMedio")) %> s</td>
                </tr>
            <% } %>
        </tbody>
    </table>

    <script>
        window.onload = function() {
            twemoji.parse(document.body, { folder: 'svg', ext: '.svg' });
        }
    </script>
</body>
</html>