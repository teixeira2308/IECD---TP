import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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


/**
 * Classe Server implementa um servidor TCP que copia 
 * bytes de um socket de entrada para outro de saída e vice-versa.
 */

public class Server {
		
	//Configurar o port
	public final static int DEFAULT_PORT = 5025;
	private static Schema protocoloSchema;
	
	//Método principal do servidor. Throws IOException 
	//para conseguir implementar os sockets
	
	public static void main(String[] args) throws IOException {
		
		//Cria um, socket de servidor na porta previamente definida
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			protocoloSchema = factory.newSchema(new File("src/main/webapp/WEB-INF/protocolo.xsd"));
		} catch (Exception e) {
			System.err.println("Erro critico: Não foi possível carregar o protocolo.xsd");
			e.printStackTrace();
			return;
		}
		
		ServerSocket serversocket = new ServerSocket(DEFAULT_PORT);
		System.out.println("Servidor TCP iniciado no porto " + DEFAULT_PORT);
		
		//Loop inifnito para esperar por conexão de clientes
		while(true) {
			try {
				System.out.println("A aguardar Jogador 1...");
				Socket socket1 = serversocket.accept();
				PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
				BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
				String nick1 = autenticarCliente(in1, out1);
				System.out.println("Jogador 1 autenticado: " + nick1);
				
				System.out.println("A aguardar Jogador 2...");
				Socket socket2 = serversocket.accept();
				PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
				BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
				String nick2 = autenticarCliente(in2, out2);
				System.out.println("Jogador2 autenticado: " + nick2);
				
				// Cria uma única partida centralizada com tamanho 5
				Jogo jogoPartida = new Jogo(5);
				
				System.out.println("Iniciando partida entre " + nick1 + " e " + nick2);
				
				// Inicia a Thread que vai gerir a sessão deste par de jogadores
				new Thread(new GestorPartida(socket1, in1, out1, nick1, socket2, in2, out2, nick2, jogoPartida)).start();
			} catch (Exception e) {
				System.out.println("Erro ao estabelecer sessão com clientes: " + e.getMessage());
			}
		}
	}
	
	private static String autenticarCliente(BufferedReader in, PrintWriter out) throws Exception {
		while(true) {
			String xmlRecebido = in.readLine();
			if (xmlRecebido == null) throw new IOException("Cliente desconectou-se durante a autenticação.");
			
			// 1. Validar obrigatoriamente contra o XSD do protocolo
			Document docMsg = validarEParsarXML(xmlRecebido);
			if (docMsg == null) {
				out.println(criarXmlRespostaAutenticacao("erro", "XML inválido face ao protocolo.xsd"));
				continue;
			}
			
			Element raiz = docMsg.getDocumentElement();
			
			// Caso 1: Pedido de Login:
			if (raiz.getElementsByTagName("pedidoLogin").getLength() > 0) {
				Element loginNode = (Element) raiz.getElementsByTagName("pedidoLogin").item(0);
				String nick = loginNode.getElementsByTagName("nickname").item(0).getTextContent();
				String pass = loginNode.getElementsByTagName("password").item(0).getTextContent();
				
				// Base de dados centralizada no Servidor
				Document docJogadores = XMLReader.loadXML("src/main/webapp/WEB-INF/jogadores.xml");
				if (XMLReader.validarLogin(docJogadores, nick, pass)) {
					out.println(criarXmlRespostaAutenticacao("sucesso", "Login efetuado com sucesso."));
					return nick;
				} else {
					out.println(criarXmlRespostaAutenticacao("erro", "Credenciais incorretas."));
				}
			}
			
			// Caso 2: Pedido de Registo
			else if (raiz.getElementsByTagName("pedidoRegisto").getLength() > 0) {
				Element registoNode = (Element) raiz.getElementsByTagName("pedidoRegisto").item(0);
				String nick = registoNode.getElementsByTagName("nickname").item(0).getTextContent();
				String pass = registoNode.getElementsByTagName("password").item(0).getTextContent();
				String nac = registoNode.getElementsByTagName("nacionalidade").item(0).getTextContent();
				int idade = Integer.parseInt(registoNode.getElementsByTagName("idade").item(0).getTextContent());
				
				Document docJogadores = XMLReader.loadXML("src/main/webapp/WEB-INF/jogadores.xml");
				if (XMLReader.getJogador(docJogadores, nick) != null) {
					out.println(criarXmlRespostaAutenticacao("erro", "O nickname já existe."));
				} else {
					XMLReader.addJogador(docJogadores, nick, pass, nac, idade);
					XMLReader.saveXML(docJogadores, "src/main/webapp/WEB-INF/jogadores.xml");
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
			
			// Executa a validação estrita baseada no esquema
			Validator validator = protocoloSchema.newValidator();
			validator.validate(new DOMSource(doc));
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