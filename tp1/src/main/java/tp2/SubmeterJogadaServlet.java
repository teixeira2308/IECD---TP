package tp2;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/SubmeterJogadaServlet")
public class SubmeterJogadaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        String nickname = (session != null) ? (String) session.getAttribute("user") : null;
        String idPartida = request.getParameter("id");
        String linhaStr = request.getParameter("l");
        String colunaStr = request.getParameter("c");

        if (nickname == null || idPartida == null || linhaStr == null || colunaStr == null) {
            response.sendRedirect("jogos.jsp");
            return;
        }

        // 1. Localizar a partida ativa na memória do Servidor Web
        GestorPartida partida = GestorPartida.encontrarPartidaPorId(idPartida);
        
        if (partida != null) {
            Jogo jogo = partida.getJogo(); 
            
            // Determinar se este utilizador Web é o Jogador 1 ou o Jogador 2
            char numeroJogador = '0';
            if (nickname.equals(partida.getNick1())) {
                numeroJogador = '1';
            } else if (nickname.equals(partida.getNick2())) {
                numeroJogador = '2';
            }

            // 2. Validação de Turno: Só avança se for a vez real dele no motor
            if (jogo.getVezAtual() == numeroJogador) {
                int linha = Integer.parseInt(linhaStr);
                int coluna = Integer.parseInt(colunaStr);
                
                // 3. INVOCAÇÃO DO TEU MÉTODO NATIVO!
                // O teu método joga(r, c, simbolo) valida a jogada, preenche a matriz,
                // atualiza os pontos e decide se passa o turno ou dá vez extra!
                boolean jogadaEfetuada = jogo.joga(linha, coluna, numeroJogador);
                
                if (jogadaEfetuada) {
                    System.out.println("[MOTOR WEB] Jogada processada com sucesso para " + nickname);
                    System.out.println("[PONTOS ATUAIS] J1: " + jogo.getPontos1() + " | J2: " + jogo.getPontos2());
                    System.out.println("[PRÓXIMA VEZ]: " + jogo.getVezAtual());
                } else {
                    System.out.println("[MOTOR WEB] Jogada considerada inválida pelo Jogo.java (Traço já ocupado)");
                }
                
            } else {
                System.out.println("[JOGADA RECUSADA] " + nickname + " tentou jogar fora da sua vez!");
            }
        }

        // 4. Redireciona imediatamente de volta para o tabuleiro atualizado
        response.sendRedirect("assistir.jsp?id=" + idPartida);
    }
}