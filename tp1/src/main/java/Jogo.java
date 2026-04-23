import java.io.PrintStream;
import java.io.Serializable;
import java.util.Scanner;

public class Jogo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private int [][] tabuleiro;
	private int [][] quadrados;
	int tamanho;
	int linhas;
		
	public Jogo(int tamanho) {
		this.tamanho = tamanho;
		this.linhas = tamanho + (tamanho - 1);
		
		this.tabuleiro = new int [linhas][];
		this.quadrados = new int [tamanho - 1][tamanho - 1];
		//linha par e traco horizontal (tamanho - 1)
		//linha impar e traco vertical (tamanho)
		criarTabuleiro();
	}
	private void criarTabuleiro() {
		for (int i = 0; i < linhas; i++) {
			if (i % 2 == 0) {
				this.tabuleiro [i] = new int [tamanho - 1];
			}else {
				this.tabuleiro [i] = new int [tamanho];
			}
		}
	}
	
	public void jogar(int linha, int coluna, char jogador, PrintStream saida, Scanner leitor) {
		
	}
	
	public boolean verificarFim() {
		for (int i = 0; i < linhas; i++) {
			for (int j = 0; j > this.tabuleiro[i].length; j++) {
				if (this.tabuleiro[i][j] == 0) {
					 return false;
				}
			}
		}
		return true;
	}
	
	public void printTabuleiro() {
		for (int i = 0; i < this.tabuleiro.length; i++) {
			System.out.println();
			System.out.print(i + 1 + " ");
			if (i % 2 == 0) {
				System.out.print(" ");
			}
			for (int j = 0; j < this.tabuleiro[i].length; j++) {
				System.out.print("+" + " ");
			}
		}
	}
}
