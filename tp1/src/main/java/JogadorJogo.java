import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Cliente para o jogo Dots and Boxes utilizando protocolo XML Interoperável.
 */
public class JogadorJogo {

    private final static String DEFAULT_HOST = "localhost";
    private final static int DEFAULT_PORT = 5025;
    private static Scanner leitor = new Scanner(System.in);
    private static boolean primeiraJogada = true;

    public static void main(String[] args) {
        String nickLogado = "";
        
        try (Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            System.out.println("Ligação estabelecida com o servidor central!");
        
            boolean autenticado = false;
            while (!autenticado) {
                System.out.println("\n1. Login");
                System.out.println("2. Registo");
                System.out.print("Escolha uma opção: ");
                int opcao = leitor.nextInt();
                leitor.nextLine(); // Limpar buffer do Scanner

                String xmlEnviar = "";
                if (opcao == 1) {
                    System.out.print("Nickname: "); String nick = leitor.nextLine();
                    System.out.print("Password: "); String pass = leitor.nextLine();
                    
                    // Constrói a mensagem de pedido de login conforme definido no protocolo.xsd
                    xmlEnviar = "<mensagem><pedidoLogin><nickname>" + nick + "</nickname><password>" + pass + "</password></pedidoLogin></mensagem>";
                    nickLogado = nick;
                } else if (opcao == 2) {
                    System.out.print("Novo Nickname: "); String nick = leitor.nextLine();
                    System.out.print("Nova Password: "); String pass = leitor.nextLine();
                    System.out.print("Nacionalidade: "); String nac = leitor.nextLine();
                    System.out.print("Idade: "); int idade = leitor.nextInt();
                    leitor.nextLine(); // Limpar buffer
                    
                    // Constrói a mensagem de pedido de registo conforme definido no protocolo.xsd
                    xmlEnviar = "<mensagem><pedidoRegisto><nickname>" + nick + "</nickname><password>" + pass + "</password><nacionalidade>" + nac + "</nacionalidade><idade>" + idade + "</idade></pedidoRegisto></mensagem>";
                    nickLogado = nick;
                } else {
                    System.out.println("Opção inválida.");
                    continue;
                }

                // Envia a String XML gerada numa única linha para a stream de rede
                out.println(xmlEnviar);

                // Aguarda a resposta estruturada em XML enviada pelo servidor
                String respostaXml = in.readLine();
                if (respostaXml == null) {
                    System.out.println("O servidor fechou a ligação inesperadamente.");
                    return;
                }

                // Efetua o parsing do XML de resposta recebido da rede
                Document docResposta = parsarXML(respostaXml);
                Element raiz = docResposta.getDocumentElement();
                String status = raiz.getElementsByTagName("status").item(0).getTextContent();
                String detalhe = raiz.getElementsByTagName("detalhe").item(0).getTextContent();

                if ("sucesso".equals(status)) {
                    System.out.println(detalhe);
                    autenticado = true;
                } else {
                    System.out.println("Erro na operação: " + detalhe);
                }
            }

         
            System.out.println("A aguardar o emparelhamento com outro jogador...");
            String xmlInicio = in.readLine();
            if (xmlInicio == null) return;
            
            Document docInicio = parsarXML(xmlInicio);
            Element inicioNode = (Element) docInicio.getElementsByTagName("inicioJogo").item(0);
            
            char simbolo = inicioNode.getElementsByTagName("simboloAtribuido").item(0).getTextContent().charAt(0);
            int tamanho = Integer.parseInt(inicioNode.getElementsByTagName("tamanhoTabuleiro").item(0).getTextContent());
            
            System.out.println("Partida Iniciada! O teu símbolo de jogador é: '" + simbolo + "'");
            
            // Instancia o objeto de jogo puramente local apenas para gerir e pintar o ecrã
            Jogo objetoJogo = new Jogo(tamanho);
          
            while (!objetoJogo.verificarFim()) {
                char vezAtual = objetoJogo.getVezAtual();

                if (vezAtual == simbolo) {
                    // TURNO LOCAL (A minha vez de jogar)
                    if (primeiraJogada) {
                        objetoJogo.printTabuleiro(System.out);
                        primeiraJogada = false;
                    }
                    System.out.println("\nSua vez! Digite a LINHA e a COLUNA da aresta:");
                    System.out.print("Linha: "); int r = leitor.nextInt();
                    System.out.print("Coluna: "); int c = leitor.nextInt();
                    leitor.nextLine(); // Limpar buffer

                    // Executa a jogada na instância interna local
                    boolean jogadaEfetuada = objetoJogo.joga(r, c, simbolo);

                    if (jogadaEfetuada) {
                        // Constrói e envia a jogada codificada em XML para o servidor central validar
                        String xmlJogada = "<mensagem><jogada><linha>" + r + "</linha><coluna>" + c + "</coluna><jogador>" + nickLogado + "</jogador></jogada></mensagem>";
                        out.println(xmlJogada);
                        
                        // Atualiza visualmente o tabuleiro local
                        objetoJogo.printTabuleiro(System.out);
                        
                        if (objetoJogo.getVezAtual() != simbolo) {
                        	primeiraJogada = true;
                        }
                    } else {
                        System.out.println("Jogada inválida ou posição já ocupada! Tente novamente.");
                        primeiraJogada = false;
                    }
                } else {
                    // TURNO REMOTO (Aguardar ação do oponente que será distribuída pelo servidor)
                    System.out.println("Aguardando a jogada do oponente...");
                    String xmlRecebido = in.readLine();
                    if (xmlRecebido == null) break;

                    Document docMsg = parsarXML(xmlRecebido);
                    Element raiz = docMsg.getDocumentElement();

                    // Verifica se o servidor enviou uma jogada ou diretamente um sinal de fim
                    if (raiz.getElementsByTagName("jogada").getLength() > 0) {
                        Element jogadaNode = (Element) raiz.getElementsByTagName("jogada").item(0);
                        int linhaOpo = Integer.parseInt(jogadaNode.getElementsByTagName("linha").item(0).getTextContent());
                        int colunaOpo = Integer.parseInt(jogadaNode.getElementsByTagName("coluna").item(0).getTextContent());
                        
                        // Sincroniza a instância local de jogo com o movimento efetuado pelo oponente
                        objetoJogo.joga(linhaOpo, colunaOpo, vezAtual);
                        objetoJogo.printTabuleiro(System.out);
                        primeiraJogada = false;
                    } else if (raiz.getElementsByTagName("fimJogo").getLength() > 0) {
                        break; // Sai do ciclo de jogadas, o jogo terminou
                    }
                }
            }


            System.out.println("\nO jogo terminou!");
            String xmlFim = in.readLine();
            if (xmlFim != null) {
                Document docFim = parsarXML(xmlFim);
                String vencedor = docFim.getElementsByTagName("vencedor").item(0).getTextContent();
                System.out.println("Vencedor validado pelo Servidor: " + vencedor);
                System.out.println("Informação: As estatísticas da partida foram atualizadas com sucesso no XML central.");
            }

        } catch (Exception e) {
            System.out.println("Erro na ligação com o sistema distribuído: " + e.getMessage());
        }
        System.out.println("Aplicação de cliente terminada!");
    }

    /**
     * Converte uma String XML recebida do Socket para uma estrutura de dados DOM Document.
     */
    private static Document parsarXML(String xmlString) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new InputSource(new StringReader(xmlString)));
    }
}