package tp2;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String nick = request.getParameter("nickname");
		String pass = request.getParameter("password");
		String xmlPath = (String) getServletContext().getAttribute("xmlPath");
		
		try {
			org.w3c.dom.Document doc = XMLReader.loadXML(xmlPath);
			
			if (XMLReader.validarLogin(doc, nick, pass)) {
				HttpSession session = request.getSession();
				session.setAttribute("user", nick);
				
				response.sendRedirect("lobby.jsp");
			} else {
				request.setAttribute("erro", "Credenciais inválidas");
				request.getRequestDispatcher("login.jsp").forward(request, response);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
	}
}