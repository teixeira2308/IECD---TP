import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


public class GestorPartida implements Runnable {
	
	private static final List<GestorPartida> jogosAtivos = Collections.synchronizedList(new ArrayList<>());
	
	private final String idPartida;

    private Socket socket1;
    private BufferedReader in1;
    private PrintWriter out1;
    private String nick1;

    private Socket socket2;
    private BufferedReader in2;
    private PrintWriter out2;
    private String nick2;
    private long tempoInicio;

    private Jogo jogoPartida;

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
        this.tempoInicio = System.currentTimeMillis();
        
        this.idPartida = UUID.randomUUID().toString();
        
        jogosAtivos.add(this);
    }

    @Override
    public void run() {
    	try {
			System.out.println("Sessão iniciada entre " + nick1 + " e " + nick2 + " [ID: " + idPartida + "]");
			
			while (!jogoPartida.verificarFim()) {
				// Verifica se há dados do Jogador 1 para o Jogador 2
				if (in1.ready()) {
					String msg1 = in1.readLine();
					if (msg1 != null) {
						processarJogadaNoServidor(msg1, '1');
						out2.println(msg1);
					}
				}
				if (in2.ready()) {
					String msg2 = in2.readLine();
					if (msg2 != null) {
						processarJogadaNoServidor(msg2, '2');
						out1.println(msg2);
					}
				}
				
				Thread.sleep(100);
			}

			System.out.println("\nPartida " + idPartida + " terminada. A calcular estatísticas...");
			long tempoTotalSegundos = (System.currentTimeMillis() - tempoInicio) / 1000;
			
			String xmlFim = "<mensagem><fimJogo><vencedor>Jogo Concluído</vencedor></fimJogo></mensagem>";
			out1.println(xmlFim);
			out2.println(xmlFim);

			try {
				String xmlPath = "src/main/webapp/WEB-INF/jogadores.xml"; 
				Document docCentral = XMLReader.loadXML(xmlPath);
				
				Element elementJ1 = XMLReader.getJogador(docCentral, nick1);
				Element elementJ2 = XMLReader.getJogador(docCentral, nick2);
				
				if (elementJ1 != null && elementJ2 != null) {
					
					int pts1 = jogoPartida.getPontos1(); 
			        int pts2 = jogoPartida.getPontos2();
			        
			        if (pts1 > pts2) {
			            XMLReader.atualizarStats(elementJ1, true); 
			            XMLReader.atualizarStats(elementJ2, false); 
			            
			            XMLReader.atualizarStats(elementJ1, false); 
			            XMLReader.atualizarStats(elementJ2, true); 
			        } else {
			            XMLReader.atualizarStats(elementJ1, true);
			            XMLReader.atualizarStats(elementJ2, true);
			        }
					
					XMLReader.addTempo(elementJ1, (int) tempoTotalSegundos);
					XMLReader.addTempo(elementJ2, (int) tempoTotalSegundos);
					
					XMLReader.saveXML(docCentral, xmlPath);
					System.out.println("Estatísticas e tempos de jogo dos nicks sincronizados no ficheiro central.");
				}
			} catch (Exception ex) {
				System.out.println("Aviso ao persistir dados da partida no XML central: " + ex.getMessage());
			}

		} catch (Exception e) {
			System.out.println("Exceção na execução da partida: " + e.getMessage());
		} finally {
			jogosAtivos.remove(this);
			try {
				if (socket1 != null && !socket1.isClosed()) socket1.close();
				if (socket2 != null && !socket2.isClosed()) socket2.close();
				System.out.println("Sockets da sessão " + idPartida + " libertados com sucesso.");
			} catch (Exception ex) {
			}
		}
	}

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

                jogoPartida.joga(linha, coluna, simboloJogador);
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar/sincronizar XML da jogada no Servidor: " + e.getMessage());
        }
    }

    public static List<GestorPartida> getJogosAtivos() {
        synchronized (jogosAtivos) {
            return new ArrayList<>(jogosAtivos);
        }
    }

    public static GestorPartida encontrarPartidaPorId(String id) {
        synchronized (jogosAtivos) {
            for (GestorPartida gp : jogosAtivos) {
                if (gp.getIdPartida().equals(id)) {
                    return gp;
                }
            }
        }
        return null;
    }


    public String getIdPartida() { return idPartida; }
    public String getNick1() { return nick1; }
    public String getNick2() { return nick2; }
    public Jogo getJogo() { return jogoPartida; }
}