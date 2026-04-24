import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente para o jogo Dots and Boxes utilizando serialização.
 */
public class JogadorJogo {

    private final static String DEFAULT_HOST = "localhost";
    private final static int DEFAULT_PORT = 5025;
    private static PrintStream saida = System.out;
    private static Scanner leitor = new Scanner(System.in);

    public static void main(String[] args) {
        try (Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT)) {
            saida.println("🚀 Ligação estabelecida: " + socket);

            // 1. Receber o símbolo (X ou O) via stream básico
            char simbolo = (char) socket.getInputStream().read();
            saida.println("🎭 Jogas com o símbolo: '" + simbolo + "'");
            
            // X começa sempre
            boolean minhaVez = (simbolo == 'X');

            // 2. Streams para serialização de objetos
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // Inicializa o jogo (tamanho 5 conforme o teu construtor)
                Jogo objetoJogo = new Jogo(5);

                while (true) {
                    if (minhaVez) {
                        // TURNO LOCAL
                        objetoJogo.printTabuleiro(); // Usa o teu método de desenho
                        saida.println("\nSua vez! Digite a LINHA e a COLUNA da aresta:");
                        
                        int r = leitor.nextInt();
                        int c = leitor.nextInt();

                        // Executa a jogada e verifica se ganhou turno extra
                        // Nota: Precisas implementar o retorno 'boolean' no método jogar() do Jogo.java
                        boolean ganhouVezExtra = objetoJogo.joga(r, c, simbolo);

                        // Envia o estado do jogo atualizado para o servidor
                        out.writeObject(objetoJogo);
                        out.flush();
                        out.reset(); // Limpa cache para garantir envio da nova versão

                        if (objetoJogo.verificarFim()) break;

                        // Só passa a vez se NÃO fechou um quadrado
                        if (!ganhouVezExtra) {
                            minhaVez = false;
                        } else {
                            saida.println("🌟 Fechaste um quadrado! Joga de novo.");
                        }

                    } else {
                        // TURNO REMOTO
                        saida.println("⏳ Aguardando a jogada do oponente...");
                        objetoJogo = (Jogo) in.readObject(); // Recebe o objeto do oponente
                        
                        if (objetoJogo.verificarFim()) break;
                        
                        // Oponente joga de novo se os pontos dele aumentaram
                        // Aqui o servidor/lógica controla quem é o próximo a agir
                        minhaVez = true; 
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            saida.println("💥 Erro na ligação: " + e.getMessage());
        }
        saida.println("🏁 Jogo terminado!");
    }
}