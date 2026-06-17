package tp2;
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

	private static final Object fileLock = new Object();
    public static Document loadXML(String path) throws Exception {
    	synchronized (fileLock) {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setValidating(false);
	        factory.setNamespaceAware(true);
	        
	        factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	        factory.setAttribute("http://xml.org/sax/features/validation", false);
	        
	        
	        File xmlFile = new File(path);
	        File xsdFile = null;
	        if (xmlFile.getParentFile() != null) {
	            xsdFile = new File(xmlFile.getParentFile(), "jogadores.xsd");
	        } else {
	            xsdFile = new File("jogadores.xsd");
	        }

	        if (xsdFile != null && xsdFile.exists() && xsdFile.length() > 0) {
	            try {
	                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	                factory.setSchema(schemaFactory.newSchema(xsdFile));
	            } catch (Exception e) {
	                System.out.println("Aviso: Falha técnica ao processar a estrutura do jogadores.xsd.");
	            }
	        } else {
	            System.out.println("Aviso: jogadores.xsd não encontrado em " + (xsdFile != null ? xsdFile.getAbsolutePath() : "caminho local") + ". Carregando em modo de compatibilidade.");
	        }
	        
	  
	
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document doc = builder.parse(xmlFile);
	        doc.getDocumentElement().normalize();
	        return doc;
        }
    }
    
    public static Element getJogador(Document doc, String nickname) {
        NodeList jogadores = doc.getElementsByTagName("jogador");
        for(int i = 0; i < jogadores.getLength(); i++) {
            Element j = (Element) jogadores.item(i);
            String nick = j.getElementsByTagName("nickname").item(0).getTextContent();
            if (nick.equalsIgnoreCase(nickname)) { // Ignora maiúsculas/minúsculas no Login
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
    
    public static void atualizarPerfil(Document doc, Element jogador, String novaFoto, String novaCor) {
        if (novaFoto != null && !novaFoto.trim().isEmpty()) {
            NodeList fotos = jogador.getElementsByTagName("foto");
            if (fotos.getLength() > 0) {
                fotos.item(0).setTextContent(novaFoto);
            } else {
                Element fotoElem = doc.createElement("foto");
                fotoElem.setTextContent(novaFoto);
                NodeList vitorias = jogador.getElementsByTagName("vitorias");
                if (vitorias.getLength() > 0) {
                    jogador.insertBefore(fotoElem, vitorias.item(0));
                } else {
                    jogador.appendChild(fotoElem);
                }
            }
        }

        if (novaCor != null && !novaCor.trim().isEmpty()) {
            NodeList cores = jogador.getElementsByTagName("corFundo");
            if (cores.getLength() > 0) {
                cores.item(0).setTextContent(novaCor);
            } else {
                Element corElem = doc.createElement("corFundo");
                corElem.setTextContent(novaCor);
                jogador.appendChild(corElem); 
            }
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
        
        synchronized (fileLock) {
        	File fileDestino = new File(path);
	        if (fileDestino.getParentFile() != null && !fileDestino.getParentFile().exists()) {
	            fileDestino.getParentFile().mkdirs();
	        }
	       
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        
	        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	        transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
	
	        DOMSource source = new DOMSource(doc);
	        StreamResult result = new StreamResult(fileDestino);
	        transformer.transform(source, result);
        }
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