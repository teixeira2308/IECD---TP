package tp2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteControlado {

    public static void main(String[] args) {
        System.out.println("=== CLIENTE INTERATIVO TCP JOGADOR 2 ===");
        
        // AJUSTA AQUI: Credenciais de um jogador registado no teu jogadores.xml
        // (Diferente daquele que vais usar para logar no Browser!)
        String nicknameOp = "aaa";
        String passwordOp = "111";
        
        String xmlLogin = "<mensagem><pedidoLogin><nickname>" + nicknameOp + "</nickname><password>111</password></pedidoLogin></mensagem>";

        try {
            System.out.println("A ligar ao Servidor TCP (porto 5025)...");
            Socket socket = new Socket("localhost", 5025);
            System.out.println("Ligação estabelecida!");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            Scanner teclado = new Scanner(System.in);

            // 1. Enviar Login
            System.out.println("A enviar credenciais de '" + nicknameOp + "'...");
            out.println(xmlLogin);
            out.flush();

            // 2. Thread para ficar sempre a ouvir o Servidor (mensagens de jogo)
            Thread escutaServidor = new Thread(() -> {
                try {
                    String linhaDoServidor;
                    while ((linhaDoServidor = in.readLine()) != null) {
                        System.out.println("\n[SERVIDOR DIZ]: " + linhaDoServidor);
                        System.out.print("Tua jogada (linha,coluna) ou XML livre: ");
                    }
                } catch (Exception e) {
                    System.out.println("\nConexão com o servidor encerrada.");
                }
            });
            escutaServidor.start();

            // 3. Loop Principal na consola para TU mandares ordens
            System.out.println("\n--- Modo de Comando Ativo ---");
            System.out.println("Podes digitar o XML completo ou usar o atalho rápido: linha,coluna (Ex: 1,2)");
            System.out.println("Digita 'sair' para fechar.");
            
            while (escutaServidor.isAlive()) {
                System.out.print("Tua jogada: ");
                String comando = teclado.nextLine().trim();

                if (comando.equalsIgnoreCase("sair")) {
                    break;
                }

                if (comando.isEmpty()) continue;

                String xmlAEnviar = "";

                // Atalho prático: se digitares "1,2", o programa monta o XML da jogada automaticamente
                if (comando.contains(",")) {
                    try {
                        String[] partes = comando.split(",");
                        int linha = Integer.parseInt(partes[0].trim());
                        int coluna = Integer.parseInt(partes[1].trim());
                        
                        // Monta o XML da jogada de acordo com o teu protocolo.xsd
                        xmlAEnviar = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mensagem><jogada><linha>" 
                                     + linha + "</linha><coluna>" + coluna + "</coluna><jogador>" 
                                     + nicknameOp + "</jogador></jogada></mensagem>";
                    } catch (Exception e) {
                        System.out.println("Erro no formato. Usa: linha,coluna (ex: 0,1)");
                        continue;
                    }
                } else {
                    // Se digitares o XML completo à mão, ele envia direto
                    xmlAEnviar = comando;
                }

                // Envia o comando para o Servidor
                System.out.println("[ENVIANDO]: " + xmlAEnviar);
                out.println(xmlAEnviar);
                out.flush();
            }

            socket.close();
            teclado.close();
            System.out.println("Cliente encerrado.");

        } catch (Exception e) {
            System.err.println("Erro no Cliente: " + e.getMessage());
        }
    }
}