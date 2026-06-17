<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="tp2.GestorPartida, tp2.Jogo" %>
<%
    // 1. DECLARAÇÃO ÚNICA DAS VARIÁVEIS DE CONTROLO DO TOPO
    String nicknameSessao = (String) session.getAttribute("user"); // Alinhado com "user"
    String idStr = request.getParameter("id");
    GestorPartida partida = null;
    
    if (idStr != null && !idStr.trim().isEmpty()) {
        partida = GestorPartida.encontrarPartidaPorId(idStr);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dots and Boxes - Tabuleiro Interativo</title>
    <% if (partida != null) { %>
        <meta http-equiv="refresh" content="3">
    <% } %>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 30px; text-align: center; }
        .container { max-width: 700px; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        .placar { display: flex; justify-content: space-around; margin: 20px 0; }
        .jogador { padding: 15px; border-radius: 8px; border: 2px solid #e0e0e0; width: 40%; background: #fff; }
        .vez-atual { background-color: #fff3cd; border-color: #ffc107; font-weight: bold; }
        .pontos { display: block; font-size: 1.8em; font-weight: bold; color: #28a745; }
        
        /* Estilos do Tabuleiro Visual de PC */
        .tabuleiro-grid { display: inline-block; margin: 20px auto; background: #fafafa; padding: 20px; border-radius: 8px; border: 1px solid #ccc; }
        .ponto-dot { width: 12px; height: 12px; background-color: #333; border-radius: 50%; display: inline-block; vertical-align: middle; }
        
        /* Arestas Horizontais */
        .aresta-h { width: 60px; height: 12px; display: inline-block; vertical-align: middle; transition: 0.2s; }
        .aresta-h.vazia { background-color: #e0e0e0; border: 1px dashed #bbb; }
        .aresta-h.vazia:hover { background-color: #bfe3ff; border-color: #007bff; cursor: pointer; }
        .aresta-h.preenchida { background-color: #007bff; }
        
        /* Arestas Verticais e Caixas */
        .aresta-v { width: 12px; height: 60px; display: inline-block; vertical-align: middle; }
        .aresta-v.vazia { background-color: #e0e0e0; border: 1px dashed #bbb; }
        .aresta-v.vazia:hover { background-color: #bfe3ff; border-color: #007bff; cursor: pointer; }
        .aresta-v.preenchida { background-color: #007bff; }
        
        .quadrado-box { width: 60px; height: 60px; display: inline-block; vertical-align: middle; background: #f0f0f0; font-size: 1.4em; font-weight: bold; line-height: 60px; text-align: center; }
	   	.aresta-h.p1, .aresta-v.p1 { background-color: #007bff; }
    	.aresta-h.p2, .aresta-v.p2 { background-color: #dc3545; }
   		.quadrado-box.j2 { background-color: #f8d7da; color: #721c24; }
        .linha-tabuleiro { white-space: nowrap; height: auto; margin: 0; padding: 0; line-height: 0; }
        .voltar { display: inline-block; margin-top:25px; padding: 10px 20px; background-color: #6c757d; color: white; text-decoration: none; border-radius: 5px; }
    </style>
</head>
<body>

<div class="container">
    <% if (partida == null) { %>
        <h1 style="color:red;">Partida não encontrada</h1>
        <a class="voltar" href="jogos.jsp">Voltar à lista</a>
    <% } else { 
        Jogo jogo = partida.getJogo(); 
        
        // Resolve de forma limpa o nome de quem joga lendo a vezAtual direto do Jogo
        String nomeQuemJoga = (jogo.getVezAtual() == '1') ? partida.getNick1() : partida.getNick2();
    %>
        <h2>Ligado como: <span style="color:#007bff;"><%= nicknameSessao %></span></h2>
        <h3 style="background: #e9ecef; padding: 10px; border-radius: 6px;">
            A JOGAR AGORA: <span style="color: #dc3545;"><%= nomeQuemJoga %></span>
        </h3>
        
        <div class="placar">
            <div class="jogador <%= (jogo.getVezAtual() == '1') ? "vez-atual" : "" %>">
                <strong><%= partida.getNick1() %> (J1)</strong>
                <span class="pontos"><%= jogo.getPontos1() %></span>
                <%= (jogo.getVezAtual() == '1') ? "👉 A JOGAR" : "" %>
            </div>
            <div class="jogador <%= (jogo.getVezAtual() == '2') ? "vez-atual" : "" %>">
                <strong><%= partida.getNick2() %> (J2)</strong>
                <span class="pontos"><%= jogo.getPontos2() %></span>
                <%= (jogo.getVezAtual() == '2') ? "👉 A JOGAR" : "" %>
            </div>
        </div>

        <div class="tabuleiro-grid">
        <%
            int[][] matTab = null;
            int[][] matQuad = null;
            int totalLinhas = 0;
            int tam = 0;
            
            try {
                java.lang.reflect.Field fTab = Jogo.class.getDeclaredField("tabuleiro");
                fTab.setAccessible(true);
                matTab = (int[][]) fTab.get(jogo);
                
                java.lang.reflect.Field fQuad = Jogo.class.getDeclaredField("quadrados");
                fQuad.setAccessible(true);
                matQuad = (int[][]) fQuad.get(jogo);
                
                java.lang.reflect.Field fLinhas = Jogo.class.getDeclaredField("linhas");
                fLinhas.setAccessible(true);
                totalLinhas = (int) fLinhas.get(jogo);
                
                java.lang.reflect.Field fTamanho = Jogo.class.getDeclaredField("tamanho");
                fTamanho.setAccessible(true);
                tam = (int) fTamanho.get(jogo);
                
            } catch(Exception e) { 
                if (matTab != null) {
                    totalLinhas = matTab.length;
                    tam = matTab[0].length + 1;
                }
            }
            
            for (int i = 0; i < totalLinhas; i++) {
                out.print("<div class='linha-tabuleiro'>");
                if (i % 2 == 0) { // Horizontais
                    for (int j = 0; j < tam; j++) {
                        out.print("<div class='ponto-dot'></div>");
                        if (j < tam - 1) {
                            int valor = matTab[i][j];
                            if (valor == 0) {
                                out.print("<a href='SubmeterJogadaServlet?id="+idStr+"&l="+i+"&c="+j+"' class='aresta-h vazia'></a>");
                            } else {
                                // Aplica 'p1' se for 1, 'p2' se for 2
                                String cor = (valor == 1) ? "p1" : "p2";
                                out.print("<div class='aresta-h preenchida " + cor + "'></div>");
                            }
                        }
                    }
                } else { // Verticais
                    for (int j = 0; j < tam; j++) {
                        int valor = matTab[i][j];
                        if (valor == 0) {
                            out.print("<a href='SubmeterJogadaServlet?id="+idStr+"&l="+i+"&c="+j+"' class='aresta-v vazia'></a>");
                        } else {
                            String cor = (valor == 1) ? "p1" : "p2";
                            out.print("<div class='aresta-v preenchida " + cor + "'></div>");
                        }
                        
                        if (j < tam - 1) {
                            int dono = matQuad[i/2][j];
                            // Aplica j1 ou j2 sempre que o quadrado estiver preenchido
                            if (dono != 0) {
                                String classeBox = (dono == 1) ? "j1" : "j2";
                                out.print("<div class='quadrado-box " + classeBox + "'>" + dono + "</div>");
                            } else {
                                out.print("<div class='quadrado-box'></div>");
                            }
                        }
                    }
                }
                out.print("</div>");
            }
        %>
        </div>

        <br>
        <p style="color:#666;">Clica nos traços cinzentos pontilhados para fechar os quadrados!</p>
        <a class="voltar" href="jogos.jsp">Sair do Jogo</a>
    <% } %>
</div>

</body>
</html>