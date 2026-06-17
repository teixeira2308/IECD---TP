package tp2;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {
	private Thread socketServerThread;
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		String xmlPath = context.getRealPath("/WEB-INF/jogadores.xml");
		String xsdPath = context.getRealPath("/WEB-INF/jogadores.xsd"); 
		
		context.setAttribute("xmlPath", xmlPath);
		
		socketServerThread = new Thread(() -> {
			try {
				// O Server.java trata de iniciar o porto 5025 e o ciclo aceitador
				Server.iniciarServidor(xmlPath, xsdPath);
			} catch (Exception e) {
				System.out.println("Erro crítico no Servidor de Sockets: " + e.getMessage());
			}
		});
		socketServerThread.start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// 1. Força o fecho do ServerSocket no Server.java para quebrar o accept()
		Server.pararServidor();
		
		// 2. Interrompe a Thread associada
		if (socketServerThread != null) {
			socketServerThread.interrupt();
		}
	}
}