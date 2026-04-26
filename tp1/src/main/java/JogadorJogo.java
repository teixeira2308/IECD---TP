import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Cliente para o jogo Dots and Boxes utilizando serialização.
 */
public class JogadorJogo {

    private final static String DEFAULT_HOST = "localhost";
    private final static int DEFAULT_PORT = 5025;
    private static PrintStream saida = System.out;
    private static Scanner leitor = new Scanner(System.in);
    private static boolean primeiraJogada = true;

    public static void main(String[] args) throws Exception {
    	String nickLogado = "";
    	try {
    		Document doc = XMLReader.loadXML("jogadores.xml");
    		
    		System.out.println("1. Login");
    		System.out.println("2. Registo");
    		int opcao = leitor.nextInt();
    		leitor.nextLine();
    		
    		if (opcao == 1) {
    			System.out.print("Nickname: "); nickLogado = leitor.nextLine();
    			System.out.print("Password: "); String pass = leitor.nextLine();
    			
    			if (!XMLReader.validarLogin(doc, nickLogado, pass)) {
    				System.out.println("Login falhou!");
    				return;
    			}
    		} else {
    			System.out.print("Novo Nickname: "); nickLogado = leitor.nextLine();
    			System.out.print("Nova Password: "); String pass = leitor.nextLine();
    			System.out.print("Nacionalidade: "); String nac = leitor.nextLine(); 
    			System.out.print("Idade: "); int idade = leitor.nextInt();
    			leitor.nextLine();
    			
    			XMLReader.addJogador(doc, nickLogado, pass, nac, idade);
    			XMLReader.saveXML(doc, "jogadores.xml");
    		}
    		Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT);
    	
            saida.println("🚀 Ligação estabelecida: " + socket);

            // 1. Receber o símbolo (X ou O) via stream básico
            char simbolo = (char) socket.getInputStream().read();
            saida.println("🎭 Jogas com o símbolo: '" + simbolo + "'");
            
            // X começa sempre
            boolean minhaVez = (simbolo == '1');

            // 2. Streams para serialização de objetos
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // Inicializa o jogo (tamanho 5 conforme o teu construtor)
                Jogo objetoJogo = new Jogo(5);
                long tempoInicio = System.currentTimeMillis();

                while (true) {
                    if (objetoJogo.getVezAtual() == simbolo) {
                        // TURNO LOCAL
                    	if (primeiraJogada && simbolo == '1') {
                            objetoJogo.printTabuleiro(saida);
                            primeiraJogada = false;
                    	}
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
                        
                        objetoJogo.printTabuleiro(saida);

                        if (objetoJogo.verificarFim()) break;

                        // Só passa a vez se NÃO fechou um quadrado
                        if (!ganhouVezExtra) {
                            minhaVez = false;
                        } else {
                            saida.println("Fechaste um quadrado! Joga de novo.");
                        }

                    } else {
                        // TURNO REMOTO
                        saida.println("⏳ Aguardando a jogada do oponente...");
                        objetoJogo = (Jogo) in.readObject(); // Recebe o objeto do oponente
                        
                        if (objetoJogo.verificarFim()) break;
                        
                        objetoJogo.printTabuleiro(saida);
                        
                        minhaVez = true; 
                    }
                }
                
                saida.println("Jogo Terminado!");
                long tempoGasto = (System.currentTimeMillis() - tempoInicio) / 1000;
                Document docFinal = XMLReader.loadXML("jogadores.xml");
                Element eu = XMLReader.getJogador(docFinal, nickLogado);
                
                if (eu != null) {
                           
	                boolean venci = objetoJogo.euGanhei(simbolo);
	                
	                XMLReader.atualizarStats(eu, venci);
	                XMLReader.addTempo(eu, tempoGasto);
	                XMLReader.saveXML(docFinal, "jogadores.xml");
	                saida.println("Estatisticas atualizadas localmente");
	                
	                // Oponente joga de novo se os pontos dele aumentaram
	                // Aqui o servidor/lógica controla quem é o próximo a agir
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            saida.println("💥 Erro na ligação: " + e.getMessage());
        }
        saida.println("🏁 Jogo terminado!");
    }
}