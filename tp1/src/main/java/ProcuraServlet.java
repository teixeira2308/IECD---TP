import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/procurarOponentes")
public class ProcuraServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain; charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		String termo = request.getParameter("termo");
		String xmlPath = (String) getServletContext().getAttribute("xmlPath");
		
		try {
			org.w3c.dom.Document doc = XMLReader.loadXML(xmlPath);
			
			List <String> resultados = XMLReader.procurarPorNome(doc, termo);
			
			for (String nick: resultados) {
				out.println(nick);
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}