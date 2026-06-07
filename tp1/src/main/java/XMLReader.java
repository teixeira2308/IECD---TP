
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLReader {

	public static Document loadXML(String path) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		
		File xmlFile = new File(path);
		File pastaWebInf = xmlFile.getParentFile();
		File xsdFile = new File(pastaWebInf, "jogadores.xsd");
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		factory.setSchema(schemaFactory.newSchema(xsdFile));
		
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
	
	public static void addJogador(Document doc, String nickname, String password, String nacionalidade, int idade) {
		Element root = doc.getDocumentElement();
		
		Element jogador = doc.createElement("jogador");
		
		Element nick = doc.createElement("nickname");
		nick.setTextContent(nickname);
		
		Element pass = doc.createElement("password");
		pass.setTextContent(password);
		
		Element nac = doc.createElement("nacionalidade");
		nac.setTextContent(nacionalidade);
		
		Element idad = doc.createElement("idade");
		idad.setTextContent(String.valueOf(idade));
		
		Element foto = doc.createElement("foto");
		foto.setTextContent("default.jpg");
		
		Element vitorias = doc.createElement("vitorias");
		vitorias.setTextContent("0");
		
		Element derrotas = doc.createElement("derrotas");
		derrotas.setTextContent("0");
		
		Element tempoTotal= doc.createElement("tempoTotal");
		tempoTotal.setTextContent("0");
		
		Element corFundo = doc.createElement("corFundo");
		corFundo.setTextContent("#ffffff");
		
		jogador.appendChild(nick);
		jogador.appendChild(pass);
		jogador.appendChild(nac);
		jogador.appendChild(idad);
		jogador.appendChild(foto);
		jogador.appendChild(vitorias);
		jogador.appendChild(derrotas);
		jogador.appendChild(tempoTotal);
		jogador.appendChild(corFundo);
		
		
		root.appendChild(jogador);
	}
	
	public static void atualizarPerfil(Element jogador, String novaFoto, String novaCor) {
		if (novaFoto != null && !novaFoto.trim().isEmpty()) {
			jogador.getElementsByTagName("foto").item(0).setTextContent(novaCor);
		}
		if (novaCor != null && !novaCor.trim().isEmpty()) {
			jogador.getElementsByTagName("corFundo").item(0).setTextContent(novaCor);
		}
	}
	
	public static List<String> procurarPorNome(Document doc, String termo){
		List<String> resultados = new ArrayList<>();
		if (termo == null || termo.trim().isEmpty()) return resultados;
		
		NodeList jogadores = doc.getElementsByTagName("jogador");
		String termoMinusculo = termo.toLowerCase();
		
		for (int i = 0; i < jogadores.getLength(); i++) {
			Element j = (Element) jogadores.item(i);
			String nick = j.getElementsByTagName("nickname").item(0).getTextContent();
			
			if (nick.toLowerCase().contains(termoMinusculo)) {
				resultados.add(nick);
			}
		}
		return resultados;
	}
	
	public synchronized static void saveXML(Document doc, String path) throws Exception {
	    doc.getDocumentElement().normalize();
	    
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    
	    transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	    
	    transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");

	    DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(new File(path));
	    transformer.transform(source, result);
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