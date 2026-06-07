import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;


@WebServlet("/registo")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024, // 1MB
		maxFileSize = 1024 * 1024 * 10, // 10MB
		maxRequestSize = 1024 * 1024 * 15 //15MB
		)

public class RegistoServlet extends HttpServlet {
	@Override
	protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String nick = request.getParameter("nickname");
		String pass = request.getParameter("password");
		String nac = request.getParameter("nacionalidade");
		int idade = Integer.parseInt(request.getParameter("idade"));
		
		Part filePart = request.getPart("foto");
		String nomeFicheiroOriginal = Paths.get(filePart.getSubmittedFileName()). getFileName().toString();
		String nomeFotoFinal = "default.png";
		
		if (nomeFicheiroOriginal != null && !nomeFicheiroOriginal.isEmpty()) {
			String extensao = nomeFicheiroOriginal.substring(nomeFicheiroOriginal.lastIndexOf("."));
			nomeFotoFinal = nick + extensao;
			
			String uploadPath = getServletContext().getRealPath("/imagens/fotos");
			File uploadDir = new File(uploadPath);
			if (!uploadDir.exists()) uploadDir.mkdirs();
			
			filePart.write(uploadPath + File.separator + nomeFotoFinal);
		}
		
		String xmlPath = (String) getServletContext().getAttribute("xmlPath");
		try {
			org.w3c.dom.Document doc = XMLReader.loadXML(xmlPath);
			
			if (XMLReader.getJogador(doc, nick) != null) {
				request.setAttribute("erro", "O nickname já se encontra registado");
				request.getRequestDispatcher("registo.jsp").forward(request, response);
				return;
			}
			
			XMLReader.addJogador(doc, nick, pass, nac, idade);
			
			org.w3c.dom.Element novoJogador = XMLReader.getJogador(doc, nick);
			if (novoJogador != null && !nomeFotoFinal.equals("default.jpg")) {
				novoJogador.getElementsByTagName("foto").item(0).setTextContent(nomeFotoFinal);
			}
			
			XMLReader.saveXML(doc, xmlPath);
			
			response.sendRedirect("login.jsp?sucesso=true");
		} catch (Exception e) {
			throw new ServletException("Erro ao processar o registo do utilizador");
		}
		
	}
}
		
