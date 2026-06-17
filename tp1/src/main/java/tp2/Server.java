package tp2;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.transform.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class Server {
		
	public final static int DEFAULT_PORT = 5025;
	private static Schema protocoloSchema;
	
	private static ServerSocket serverSocket;
	private static volatile boolean running = false;
	
	public static void iniciarServidor(String xmlPath, String xsdProtocoloPath) {
		try {
			running = true;
			
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			File xsdFile = new File(xsdProtocoloPath);
			if (xsdFile.exists()) {
				protocoloSchema = sf.newSchema(xsdFile);
				System.out.println("[SERVER TCP] protocolo.xsd carregado com sucesso via Tomcat.");
			} else {
                System.out.println("[SERVER TCP] AVISO: protocolo.xsd não encontrado em: " + xsdProtocoloPath);
            }
			
			serverSocket = new ServerSocket(DEFAULT_PORT);
			System.out.println("[SERVER TCP] Ativo no porto " + DEFAULT_PORT);
			
			while (running && !Thread.currentThread().isInterrupted()) {
				try {
					System.out.println("[SERVER TCP] A aguardar Jogador 1...");
					Socket socket1 = serverSocket.accept();
					PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
					BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
					String nick1 = autenticarClienteTomcat(in1, out1, xmlPath);
					System.out.println("[SERVER TCP] Jogador 1 autenticado: " + nick1);
					
					System.out.println("[SERVER TCP] A aguardar Jogador 2...");
					Socket socket2 = serverSocket.accept();
					PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
					BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
					String nick2 = autenticarClienteTomcat(in2, out2, xmlPath);
					System.out.println("[SERVER TCP] Jogador 2 autenticado: " + nick2);
					
					Jogo jogoPartida = new Jogo(5);
					System.out.println("[SERVER TCP] Iniciando partida entre " + nick1 + " e " + nick2);
					
					new Thread(new GestorPartida(socket1, in1, out1, nick1, socket2, in2, out2, nick2, jogoPartida)).start();
					
				} catch (IOException e) {
					if (!running) {
						System.out.println("[SERVER TCP] ServerSocket fechado pelo contexto Web.");
						break;
					}
					System.out.println("[SERVER TCP] Erro ao estabelecer sessão com clientes: " + e.getMessage());
				} catch (Exception e) {
					System.out.println("[SERVER TCP] Erro no fluxo de emparelhamento: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			System.out.println("[SERVER TCP] Erro fatal no servidor TCP: " + e.getMessage());
		} catch (Throwable t) {
			System.out.println("[SERVER TCP] Erro inesperado: " + t.getMessage());
		} finally {
			pararServidor();
		}
	}
	
	public static void pararServidor() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[SERVER TCP] ServerSocket encerrado com sucesso.");
            }
        } catch (IOException e) {
            System.out.println("[SERVER TCP] Erro ao fechar ServerSocket: " + e.getMessage());
        }
    }
	
	private static String autenticarClienteTomcat(BufferedReader in, PrintWriter out, String xmlPath) throws Exception {
		while(true) {
			String xmlRecebido = in.readLine();
			if (xmlRecebido == null) throw new IOException("Cliente desconectou-se durante a autenticação.");
			
			Document docMsg = validarEParsarXML(xmlRecebido);
			if (docMsg == null) {
				out.println(criarXmlRespostaAutenticacao("erro", "XML inválido face ao protocolo.xsd"));
				continue;
			}
			
			Element raiz = docMsg.getDocumentElement();
			
			if (raiz.getElementsByTagName("pedidoLogin").getLength() > 0) {
				Element loginNode = (Element) raiz.getElementsByTagName("pedidoLogin").item(0);
				String nick = loginNode.getElementsByTagName("nickname").item(0).getTextContent();
				String pass = loginNode.getElementsByTagName("password").item(0).getTextContent();
				
				Document docJogadores = XMLReader.loadXML(xmlPath);
				if (XMLReader.validarLogin(docJogadores, nick, pass)) {
					out.println(criarXmlRespostaAutenticacao("sucesso", "Login efetuado com sucesso."));
					return nick;
				} else {
					out.println(criarXmlRespostaAutenticacao("erro", "Credenciais incorretas."));
				}
			}
			
			else if (raiz.getElementsByTagName("pedidoRegisto").getLength() > 0) {
				Element registoNode = (Element) raiz.getElementsByTagName("pedidoRegisto").item(0);
				String nick = registoNode.getElementsByTagName("nickname").item(0).getTextContent();
				String pass = registoNode.getElementsByTagName("password").item(0).getTextContent();
				String nac = registoNode.getElementsByTagName("nacionalidade").item(0).getTextContent();
				int idade = Integer.parseInt(registoNode.getElementsByTagName("idade").item(0).getTextContent());
				
				Document docJogadores = XMLReader.loadXML(xmlPath);
				if (XMLReader.getJogador(docJogadores, nick) != null) {
					out.println(criarXmlRespostaAutenticacao("erro", "O nickname já existe."));
				} else {
					XMLReader.addJogador(docJogadores, nick, pass, nac, idade);
					XMLReader.saveXML(docJogadores, xmlPath);
					out.println(criarXmlRespostaAutenticacao("sucesso", "Registo efetuado com sucesso."));
					return nick;
				}
			}
		}
	}
	
	public static Document validarEParsarXML(String xmlString) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(xmlString)));
			
			if (protocoloSchema != null) {
				Validator validator = protocoloSchema.newValidator();
				validator.validate(new DOMSource(doc));
			}
			return doc;
		} catch(Exception e) {
			System.out.println("Falha na validação XML contra o protocolo.xsd: " + e.getMessage());
			return null;
		}
	}
	
	public static String criarXmlRespostaAutenticacao(String status, String detail) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc = dbf.newDocumentBuilder().newDocument();
		
		Element mensagem = doc.createElement("mensagem");
		Element resposta = doc.createElement("respostaAutenticacao");
		
		Element statusElem =  doc.createElement("status");
		statusElem.setTextContent(status);
		resposta.appendChild(statusElem);
		
		Element detalheElem = doc.createElement("detalhe");
		detalheElem.setTextContent(detail);
		resposta.appendChild(detalheElem);
		
		mensagem.appendChild(resposta);
		doc.appendChild(mensagem);
		return converterDocParaString(doc);
	}
	
	public static String converterDocParaString(Document doc) throws Exception {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString().replaceAll("\n", "").replaceAll("\r", "");
	}
}