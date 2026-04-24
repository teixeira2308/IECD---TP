import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe Server implementa um servidor TCP que copia 
 * bytes de um socket de entrada para outro de saída e vice-versa.
 */

public class Server {
		
	//Configurar o port
	public final static int DEFAULT_PORT = 5025;
	
	//Método principal do servidor. Throws IOException 
	//para conseguir implementar os sockets
	
	public static void main(String[] args) throws IOException {
		
		//Cria um, socket de servidor na porta previamente definida
		ServerSocket serversocket = new ServerSocket(DEFAULT_PORT);
		System.out.println("Servidor TCP iniciado no porto " + DEFAULT_PORT);
		
		//Loop inifnito para esperar por conexão de clientes
		while(true) {
			//Aceita nova ligação de cliente
			Socket socket1 = serversocket.accept();
			//mostra informação sobre a ligação de saida
			System.out.println("Ligação estabelecida com: " + socket1);
			
			//Aceita uma nova ligação de cliente
			Socket socket2 = serversocket.accept();
			//mostra informação sobre a ligação de saida
			System.out.println("Ligação estabelecida com: " + socket2);
			
			//Cria threads para copiar caracteres em ambas as direções
			//Uma thread para copiar da entrada para a saida
			//Player1 começa, Player2 espera pela sua vez	
			new Thread(() -> copiarCaracteres(socket1, socket2, 'X')).start();
			//Outra thread para o Player2 fazer a jogada
			new Thread(() -> copiarCaracteres(socket2, socket1, 'O')).start();
		}
	}
	
	/**
	 * Copia o caracter de um socket de entrada para um socket de saida.
	 * 	 */

	private static void copiarCaracteres(final Socket entrada, final Socket saida, final char numberPlayer) {
		//Cria streams de entrada e saída para os sockets
		try (InputStream inputStream = entrada.getInputStream();
				OutputStream outputStream = saida.getOutputStream();) {
			//Atribui o simbolo
			outputStream.write(numberPlayer);
			outputStream.flush();
			//Enquanto houverem, lê bytes do socket de entrada
			//e escreve no socket de saida
			int umByte;
			while((umByte = inputStream.read()) != -1) {
				//Escreve o byte lido no socket de saida
				outputStream.write(umByte);
				outputStream.flush();
			}
		} catch (IOException e) {
			System.out.println("Terminou " + entrada+ "<->" + saida + ": " + e.getLocalizedMessage());
			
		}
		finally {
			//Fecha os sockets
			try {
				entrada.close();
				saida.close();
			} catch(IOException e) {
				
			}
		}
	}
}