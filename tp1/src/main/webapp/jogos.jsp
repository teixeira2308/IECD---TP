<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, tp2.GestorPartida" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Jogos em Execução</title>
    <meta http-equiv="refresh" content="10">
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f4f9; }
        h1 { color: #333; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; background: white; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #007bff; color: white; }
        tr:hover { background-color: #f1f1f1; }
        .btn-assistir { background-color: #28a745; color: white; padding: 6px 12px; text-decoration: none; border-radius: 4px; font-weight: bold; }
        .btn-assistir:hover { background-color: #218838; }
        .no-games { padding: 20px; color: #666; font-style: italic; }
    </style>
</head>
<body>

    <h1>Partidas Ativas (Consola TCP)</h1>
    <p>Esta página atualiza-se automaticamente a cada 10 segundos.</p>

<div style="margin-bottom: 20px;">
        <a href="criarPartida" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;">
            + Criar Nova Partida pelo Browser
        </a>
    </div>
    <table>
        <thead>
            <tr>
                <th>ID da Partida</th>
                <th>Jogador 1 (Simbolo 1)</th>
                <th>Jogador 2 (Simbolo 2)</th>
                <th>Estado do Jogo</th>
                <th>Ações</th>
            </tr>
        </thead>
        <tbody>
            <%
                // Obtém a lista centralizada de jogos a decorrer no Servidor de Sockets
                List<GestorPartida> lista = GestorPartida.getJogosAtivos();
                if (lista == null || lista.isEmpty()) {
            %>
                <tr>
                    <td colspan="5" class="no-games">Não existem partidas a decorrer de momento na Consola.</td>
                </tr>
            <%
                } else {
                    for (GestorPartida gp : lista) {
            %>
                <tr>
                    <td><code><%= gp.getIdPartida() %></code></td>
                    <td><strong><%= gp.getNick1() %></strong></td>
                    <td><strong><%= gp.getNick2() %></strong></td>
                    <td>
                        Vez de: Jogador <%= gp.getJogo().getVezAtual() %>
                    </td>
                    <td>
                        <a class="btn-assistir" href="assistir.jsp?id=<%= gp.getIdPartida() %>">Assistir</a>
                    </td>
                </tr>
            <%
                    }
                }
            %>
        </tbody>
    </table>

</body>
</html>