package tp2;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClienteControlado {

    public static void main(String[] args) {
        System.out.println("=== CLIENTE INTERATIVO TCP ===");
        
        try (Socket socket = new Socket("localhost", 5025);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             Scanner teclado = new Scanner(System.in)) {

            // 1. Menu de Login Manual
            System.out.print("Nickname: ");
            String nick = teclado.nextLine();
            System.out.print("Password: ");
            String pass = teclado.nextLine();

            String xmlLogin = "<mensagem><pedidoLogin><nickname>" + nick + "</nickname><password>" + pass + "</password></pedidoLogin></mensagem>";
            out.println(xmlLogin);

            // 2. Thread para ouvir respostas do servidor
            Thread escuta = new Thread(() -> {
                try {
                    String linha;
                    while ((linha = in.readLine()) != null) {
                        System.out.println("\n[SERVER]: " + linha);
                        if (linha.contains("<status>")) {
                        	System.out.print("Jogada (l,c): ");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\nConexão perdida.");
                }
            });
            escuta.setDaemon(true);
            escuta.start();

            // 3. Loop de comandos
            while (true) {
                String comando = teclado.nextLine();
                if (comando.equalsIgnoreCase("sair")) break;

                if (comando.contains(",")) {
                    String[] p = comando.split(",");
                    String xml = "<mensagem><jogada><linha>" + p[0].trim() + 
                                 "</linha><coluna>" + p[1].trim() + 
                                 "</coluna><jogador>" + nick + "</jogador></jogada></mensagem>";
                    out.println(xml);
                } else {
                    out.println(comando); // Enviar XML bruto se necessário
                }
            }

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}