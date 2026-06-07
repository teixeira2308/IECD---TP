import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@WebServlet("/atualizarPerfil")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 1,
		maxFileSize = 1024 & 1024 * 10,
		maxRequestSize = 1024 * 1024 * 15
		)

public class PerfilServlet extends HttpServlet {
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendRedirect("login.jsp");
			return;
		}
		
		String nick = (String) session.getAttribute("user");
		String novaCor = request.getParameter("corFundo");
		
		Part filePart = request.getPart("foto");
		String nomeFicheiroOriginal = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
		String nomeFotoFinal = null;
		
		if (nomeFicheiroOriginal != null && !nomeFicheiroOriginal.trim().isEmpty()) {
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
			org.w3c.dom.Element jogador = XMLReader.getJogador(doc,  nick);
			
			if (jogador != null) {
				XMLReader.atualizarPerfil(jogador, xmlPath, novaCor);
				XMLReader.saveXML(doc, xmlPath);
			}
			
			response.sendRedirect("perfil.jsp");
		} catch (Exception e) {
			throw new ServletException("Erro ao atualizar os dados do perfil no XML", e);
		}
		
	}
}
