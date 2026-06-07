import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * GestorPartida controla a Thread dedicada a gerir o fluxo de rede
 * e o protocolo XML entre um par de jogadores (abc e aaa).
 */
public class GestorPartida implements Runnable {

    // Variáveis de ligação do Jogador 1
    private Socket socket1;
    private BufferedReader in1;
    private PrintWriter out1;
    private String nick1;

    // Variáveis de ligação do Jogador 2
    private Socket socket2;
    private BufferedReader in2;
    private PrintWriter out2;
    private String nick2;

    // Instância central do jogo para controlo de turnos no Servidor
    private Jogo jogoPartida;

    // Construtor obrigatório para bater certo com a chamada do Server.java
    public GestorPartida(Socket socket1, BufferedReader in1, PrintWriter out1, String nick1,
                         Socket socket2, BufferedReader in2, PrintWriter out2, String nick2, 
                         Jogo jogoPartida) {
        this.socket1 = socket1;
        this.in1 = in1;
        this.out1 = out1;
        this.nick1 = nick1;
        this.socket2 = socket2;
        this.in2 = in2;
        this.out2 = out2;
        this.nick2 = nick2;
        this.jogoPartida = jogoPartida;
    }

    @Override
    public void run() {
        long tempoInicio = System.currentTimeMillis();
        try {
            System.out.println("Partida iniciada no Gestor entre " + nick1 + " e " + nick2);

            // 1. Enviar mensagem protocolar de início de jogo para ambos os clientes
            out1.println("<mensagem><inicioJogo><simboloAtribuido>1</simboloAtribuido><tamanhoTabuleiro>5</tamanhoTabuleiro></inicioJogo></mensagem>");
            out2.println("<mensagem><inicioJogo><simboloAtribuido>2</simboloAtribuido><tamanhoTabuleiro>5</tamanhoTabuleiro></inicioJogo></mensagem>");

            // 2. Ciclo principal de turnos gerido pelo Servidor
            while (!jogoPartida.verificarFim()) {
                char vez = jogoPartida.getVezAtual();
                String msgRecebida = "";

                if (vez == '1') {
                    // Vez do Jogador 1: Servidor lê a jogada do J1
                    msgRecebida = in1.readLine();
                    if (msgRecebida == null) {
                        System.out.println("Jogador 1 (" + nick1 + ") desconectou-se.");
                        break;
                    }
                    
                    // Processa e sincroniza a jogada no motor do servidor antes de retransmitir
                    processarJogadaNoServidor(msgRecebida, '1');
                    
                    // Envia a jogada para o Jogador 2
                    out2.println(msgRecebida);
                } else {
                    // Vez do Jogador 2: Servidor lê a jogada do J2
                    msgRecebida = in2.readLine();
                    if (msgRecebida == null) {
                        System.out.println("Jogador 2 (" + nick2 + ") desconectou-se.");
                        break;
                    }
                    
                    // Processa e sincroniza a jogada no motor do servidor antes de retransmitir
                    processarJogadaNoServidor(msgRecebida, '2');
                    
                    // Envia a jogada para o Jogador 1
                    out1.println(msgRecebida);
                }
            }

            // 3. O jogo terminou! Processamento de Fim de Jogo e Atualização de Estatísticas
            System.out.println("Partida terminada entre " + nick1 + " e " + nick2 + ". A processar resultados...");
            long tempoGasto = (System.currentTimeMillis() - tempoInicio) / 1000; // tempo em segundos

            // Determinar o vencedor de acordo com o motor de jogo central
            String nickVencedor = "Empate";
            boolean j1Venceu = jogoPartida.euGanhei('1');
            boolean j2Venceu = jogoPartida.euGanhei('2');

            if (j1Venceu && !j2Venceu) {
                nickVencedor = nick1;
            } else if (j2Venceu && !j1Venceu) {
                nickVencedor = nick2;
            }

            // Criar mensagem XML protocolar de fim de jogo
            String xmlFim = "<mensagem><fimJogo><vencedor>" + nickVencedor + "</vencedor></fimJogo></mensagem>";
            
            // Notificar ambos os clientes que o jogo acabou e quem ganhou
            out1.println(xmlFim);
            out2.println(xmlFim);

            // 4. Atualizar a base de dados centralizada no servidor (jogadores.xml) de forma segura
            synchronized (XMLReader.class) {
                Document docFinal = XMLReader.loadXML("src/main/webapp/WEB-INF/jogadores.xml");
                
                Element j1Node = XMLReader.getJogador(docFinal, nick1);
                Element j2Node = XMLReader.getJogador(docFinal, nick2);

                if (j1Node != null && j2Node != null) {
                    if (nickVencedor.equals(nick1)) {
                        XMLReader.atualizarStats(j1Node, true);  // J1 ganhou
                        XMLReader.atualizarStats(j2Node, false); // J2 perdeu
                    } else if (nickVencedor.equals(nick2)) {
                        XMLReader.atualizarStats(j1Node, false); // J1 perdeu
                        XMLReader.atualizarStats(j2Node, true);  // J2 ganhou
                    } else {
                        XMLReader.atualizarStats(j1Node, false);
                        XMLReader.atualizarStats(j2Node, false);
                    }

                    // Adiciona o tempo de jogo a ambos os perfis
                    XMLReader.addTempo(j1Node, tempoGasto);
                    XMLReader.addTempo(j2Node, tempoGasto);

                    // Salva fisicamente as alterações no XML central do servidor
                    XMLReader.saveXML(docFinal, "src/main/webapp/WEB-INF/jogadores.xml");
                    System.out.println("Estatísticas da partida gravadas com sucesso no jogadores.xml!");
                }
            }

        } catch (Exception e) {
            System.out.println("Exceção na execução da partida: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Garantir que os sockets são fechados no final
            try {
                if (socket1 != null && !socket1.isClosed()) socket1.close();
                if (socket2 != null && !socket2.isClosed()) socket2.close();
                System.out.println("Sockets da sessão encerrados.");
            } catch (Exception ex) {
                // Ignora erro ao fechar
            }
        }
    }

    /**
     * Método auxiliar para efetuar o parsing do XML recebido do cliente e aplicar
     * a jogada na instância do jogo que corre dentro do Servidor.
     */
    private void processarJogadaNoServidor(String xmlString, char simboloJogador) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlString)));
            Element raiz = doc.getDocumentElement();

            if (raiz.getElementsByTagName("jogada").getLength() > 0) {
                Element jogadaNode = (Element) raiz.getElementsByTagName("jogada").item(0);
                int linha = Integer.parseInt(jogadaNode.getElementsByTagName("linha").item(0).getTextContent());
                int coluna = Integer.parseInt(jogadaNode.getElementsByTagName("coluna").item(0).getTextContent());

                // Executa a jogada na instância do servidor.
                // Isto vai atualizar os pontos e alternar a vezAtual de forma idêntica à do cliente!
                jogoPartida.joga(linha, coluna, simboloJogador);
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar/sincronizar XML da jogada no Servidor: " + e.getMessage());
        }
    }
}