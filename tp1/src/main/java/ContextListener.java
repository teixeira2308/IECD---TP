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
		
		context.setAttribute("xmlPath", xmlPath);
		socketServerThread = new Thread(() -> {
			try {
				serverSocket = new ServerSocket(5025);
				while (!Thread.currentThread().isInterrupted()) {
					Socket clientSocket = serverSocket.accept();
				}
			} catch (IOException e) {
				
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