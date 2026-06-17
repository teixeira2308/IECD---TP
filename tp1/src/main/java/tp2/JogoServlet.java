package tp2;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@WebServlet("/jogar")
public class JogoServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String nick = (String) session.getAttribute("user");
        
        try {
            System.out.println("[BROWSER] O utilizador " + nick + " clicou para entrar na partida TCP...");
            
            // 1. O SEU BROWSER LIGA-SE AO SERVIDOR TCP (Porto 5025)
            Socket socketTCP = new Socket("localhost", 5025);
            PrintWriter out = new PrintWriter(socketTCP.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socketTCP.getInputStream(), "UTF-8"));
            
            // 2. Monta o XML de autenticação de acordo com o vosso protocolo
            String xmlLogin = "<mensagem xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"protocolo.xsd\"><pedidoLogin><nickname>" 
                              + nick + "</nickname><password>A_TUA_LOGICA_WEB_NAO_ENVIA_PASS_AQUI</password></pedidoLogin></mensagem>";
            
            // DICA DE OURO: Como o Servidor TCP pede nickname e password, mas aqui na Web o utilizador 
            // já está logado, para este teste funcionar, garante que o teu Server.java aceita o login 
            // ou altera temporariamente a String do XML acima para passar uma password válida da BD/XML.
            
            // 3. Envia o XML para o Server.java
            out.println(xmlLogin);
            out.flush();
            
            // 4. Lê a resposta imediata do Server.java
            StringBuilder sb = new StringBuilder();
            String linha;
            while ((linha = in.readLine()) != null) {
                sb.append(linha);
                if (linha.contains("</mensagem>")) break;
            }
            
            System.out.println("[BROWSER] Resposta do Servidor TCP: " + sb.toString());
            
            if (sb.toString().contains("sucesso")) {
                // Guarda o socket e os buffers na sessão para as próximas páginas Web poderem jogar!
                session.setAttribute("socketJogo", socketTCP);
                
                // Redireciona o utilizador para a página onde está o tabuleiro de jogo
                response.sendRedirect("tabuleiro.jsp");
            } else {
                socketTCP.close();
                request.setAttribute("erro", "O servidor TCP recusou a autenticação.");
                request.getRequestDispatcher("lobby.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("[BROWSER] Erro ao ligar ao Servidor TCP: " + e.getMessage());
            request.setAttribute("erro", "Não foi possível ligar ao servidor de jogo (Porto 5025).");
            request.getRequestDispatcher("lobby.jsp").forward(request, response);
        }
    }
}