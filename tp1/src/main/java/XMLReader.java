
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLReader {

	public static Document loadXML(String path) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(path));
		doc.getDocumentElement().normalize();
		return doc;
	}
	
	public static Element getJogador(Document doc, String nickname) {
		NodeList jogadores = doc.getElementsByTagName("jogador");
		
		for(int i = 0; i < jogadores.getLength(); i++) {
			Element j = (Element) jogadores.item(i);
			
			String nick = j.getElementsByTagName("nickname").item(0).getTextContent();
			
			if (nick.equals(nickname)) {
				return j;
			}
		}
		return null;
	}
	
	public static boolean validarLogin(Document doc, String nickname, String password) {
		Element jogador = getJogador(doc, nickname);
		
		if (jogador == null) return false;
		
		String pass = jogador.getElementsByTagName("password").item(0).getTextContent();
		
		return pass.equals(password);
	}
	
	public static void addJogador(Document doc, String nickname, String password) {
		Element root = doc.getDocumentElement();
		
		Element jogador = doc.createElement("jogador");
		
		Element nick = doc.createElement("nickname");
		nick.setTextContent(nickname);
		
		Element pass = doc.createElement("password");
		pass.setTextContent(password);
		
		Element nacionalidade = doc.createElement("nacionalidade");
		nacionalidade.setTextContent("Unknown");
		
		Element idade = doc.createElement("idade");
		idade.setTextContent("0");
		
		Element foto = doc.createElement("foto");
		foto.setTextContent("default.jpg");
		
		Element vitorias = doc.createElement("vitorias");
		vitorias.setTextContent("0");
		
		Element derrotas = doc.createElement("derrotas");
		derrotas.setTextContent("0");
		
		Element tempoTotal= doc.createElement("tempoTotal");
		tempoTotal.setTextContent("0");
		
		jogador.appendChild(nick);
		jogador.appendChild(pass);
		jogador.appendChild(nacionalidade);
		jogador.appendChild(idade);
		jogador.appendChild(foto);
		jogador.appendChild(vitorias);
		jogador.appendChild(derrotas);
		jogador.appendChild(tempoTotal);
		
		
		root.appendChild(jogador);
	}
	
	public synchronized static void saveXML(Document doc, String path) throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(doc), new StreamResult(new File(path)));
	}
	
	public static void atualizarStats(Element jogador, boolean venceu) {
		int vitorias = Integer.parseInt(jogador.getElementsByTagName("vitorias").item(0).getTextContent());
		int derrotas = Integer.parseInt(jogador.getElementsByTagName("derrotas").item(0).getTextContent());
		
		if (venceu) vitorias++;
		else derrotas++;
		
		jogador.getElementsByTagName("vitorias").item(0).setTextContent(String.valueOf(vitorias));
		jogador.getElementsByTagName("derrotas").item(0).setTextContent(String.valueOf(derrotas));
	}
	
	public static void addTempo(Element jogador, long tempo) {
		long tempoAtual = Long.parseLong(jogador.getElementsByTagName("tempoTotal").item(0).getTextContent());
		
		tempoAtual += tempo;
		
		jogador.getElementsByTagName("tempoTotal").item(0).setTextContent(String.valueOf(tempoAtual));
	}
}