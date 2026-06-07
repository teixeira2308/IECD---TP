import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener{
	private Thread socketServerThread;
	private ServerSocket serverSocket;
	
	@Override
	public void contextInitialized (ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		String xmlPath = context.getRealPath("/WEB-INF/jogadores.xml");
		String xsdPath = context.getRealPath("/WEB-INF/protocolo.xsd"); 
		
		context.setAttribute("xmlPath", xmlPath);
		
		try {
			Server.iniciarServidor(xmlPath, xsdPath);
		} catch (Exception e) {
			System.out.println("Aviso: Falha ao iniciar esquema de validação estática: " + e.getMessage());
		}
		
		socketServerThread = new Thread(() -> {
			try {
				serverSocket = new ServerSocket(5025);
				System.out.println("Servidor de Sockets embutido ativo na porta 5025.");
				
				while (!Thread.currentThread().isInterrupted()) {
					Socket clientSocket = serverSocket.accept();
					System.out.println("Nova conexão remota recebida pelo ContextListener Web.");
					
					Thread t = new Thread(() -> {
						try {
						} catch (Exception e) {
							System.out.println("Erro ao processar cliente Socket: " + e.getMessage());
						}
					});
					t.start();
				}
			} catch (IOException e) {
				System.out.println("Servidor de Sockets do Contexto Web terminado.");
			}
		});
		socketServerThread.start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (IOException e) {
			
		}
		
		if (socketServerThread != null) {
			socketServerThread.interrupt();
		}
	}
}