<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Captura o ID da partida enviado pelo botão da lista de jogos
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
    <title>A assistir partida</title>
    <% if (partida != null) { %>
        <meta http-equiv="refresh" content="2">
    <% } %>
    <style>
        body { font-family: Arial, sans-serif; margin: 30px; background-color: #fafafa; text-align: center; }
        .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        .placar { display: flex; justify-content: space-around; margin: 20px 0; font-size: 1.2em; }
        .jogador { padding: 10px 20px; border-radius: 4px; border: 1px solid #ddd; }
        .vez-atual { background-color: #fff3cd; border-color: #ffeba2; font-weight: bold; }
        .tabuleiro-box { background: #eee; padding: 20px; border-radius: 6px; font-family: monospace; font-size: 1.5em; line-height: 1.2; text-align: left; display: inline-block; white-space: pre; }
        .voltar { display: inline-block; margin-top: 20px; padding: 10px 20px; background-color: #6c757d; color: white; text-decoration: none; border-radius: 4px; }
        .voltar:hover { background-color: #5a6268; }
        .erro { color: red; font-weight: bold; }
    </style>
</head>
<body>

<div class="container">
    <%
        if (partida == null) {
    %>
        <h1 class="erro">Partida não encontrada</h1>
        <p>O jogo selecionado pode ter terminado ou a ligação foi interrompida.</p>
        <a class="voltar" href="jogos.jsp">Voltar à lista</a>
    <%
        } else {
            Jogo jogo = partida.getJogo();
    %>
        <h1>A assistir: <%= partida.getNick1() %> VS <%= partida.getNick2() %></h1>
        <p>ID: <code><%= partida.getIdPartida() %></code></p>
        
        <div class="placar">
            <div class="jogador <%= (jogo.getVezAtual() == '1') ? "vez-atual" : "" %>">
                J1: <%= partida.getNick1() %>
            </div>
            <div class="jogador <%= (jogo.getVezAtual() == '2') ? "vez-atual" : "" %>">
                J2: <%= partida.getNick2() %>
            </div>
        </div>

        <h3>Estado Atual do Tabuleiro</h3>
        <div class="tabuleiro-box"><%
            // Algoritmo visual adaptado para desenhar o tabuleiro baseado na matriz interna do Jogo.java
            int tamanho = jogo.tamanho;
            int linhas = jogo.linhas;
            
            // Vamos usar o motor do teu Jogo para gerar a representação em texto equivalente ao printTabuleiro
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream ps = new java.io.PrintStream(baos);
            jogo.printTabuleiro(ps);
            String tabuleiroTexto = baos.toString("UTF-8");
            
            out.print(tabuleiroTexto);
        %></div>

        <br>
        <p>O ecrã atualiza-se automaticamente à medida que os jogadores jogam na consola.</p>
        <a class="voltar" href="jogos.jsp">Sair da Partida</a>
    <%
        }
    %>
</div>

</body>
</html>