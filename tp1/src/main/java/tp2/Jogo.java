package tp2;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Scanner;

public class Jogo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private int [][] tabuleiro;
	private int [][] quadrados;
	
	private char vezAtual = '1';
	
	int tamanho;
	int linhas;
		
	private int pontos1 = 0;
	private int pontos2 = 0;
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
	
	public boolean joga(int r, int c, char simbolo) {
        if (r < 0 || r >= linhas || c < 0 || c >= tabuleiro[r].length || tabuleiro[r][c] != 0) {
            return false; // Jogada inválida ou já preenchida
        }

        // Marca a aresta (1 para X, 2 para O - ou apenas 1 para ocupado)
        tabuleiro[r][c] = (simbolo == '1') ? 1 : 2;
        boolean vezExtra = verificarEPreencherQuadrados(r, c, simbolo);
        
        if (!vezExtra) {
        	vezAtual = (simbolo == '1') ? '2' : '1'; 
        	
        }

        return true;
    }
	
	public boolean euGanhei(char simbolo) {
		if (simbolo == '1') {
			return pontos1 > pontos2;
		} else {
			return pontos2 > pontos1;
		}
	}
	
	private boolean verificarEPreencherQuadrados(int r, int c, char simbolo) {
        boolean fechouAlgum = false;
        int valorJogador = (simbolo == '1') ? 1 : 2;

        if (r % 2 == 0) { // Linha Horizontal
            //quadrado acima
            if (r > 0 && verificarQuadrado(r - 1, c)) {
                quadrados[(r/2)-1][c] = valorJogador;
                atualizarPontos(simbolo);
                fechouAlgum = true;
            }
            //quadrado abaixo
            if (r < linhas - 1 && verificarQuadrado(r + 1, c)) {
                quadrados[r/2][c] = valorJogador;
                atualizarPontos(simbolo);
                fechouAlgum = true;
            }
        } else { // Linha Vertical
            //quadrado a esquerda
            if (c > 0 && verificarQuadrado(r, c - 1)) {
                quadrados[r/2][c-1] = valorJogador;
                atualizarPontos(simbolo);
                fechouAlgum = true;
            }
            //quadrado a direita
            if (c < tamanho - 1 && verificarQuadrado(r, c)) {
                quadrados[r/2][c] = valorJogador;
                atualizarPontos(simbolo);
                fechouAlgum = true;
            }
        }
        return fechouAlgum;
    }
	
	private boolean verificarQuadrado(int rVertical, int cEsquerda) {
        return tabuleiro[rVertical-1][cEsquerda] != 0 &&
               tabuleiro[rVertical+1][cEsquerda] != 0 &&
               tabuleiro[rVertical][cEsquerda] != 0 &&
               tabuleiro[rVertical][cEsquerda+1] != 0;
    }
	
	private void atualizarPontos(char simbolo) {
        if (simbolo == '1') pontos1++; else pontos2++;
    }
	
	public boolean terminou(PrintStream saida) {
        for (int[] linha : tabuleiro) {
            for (int aresta : linha) {
                if (aresta == 0) return false;
            }
        }
        saida.println("Fim de jogo! J1: " + pontos1 + " | J2: " + pontos2);
        return true;
    }
	
	public boolean verificarFim() {
		for (int i = 0; i < linhas; i++) {
			for (int j = 0; j < this.tabuleiro[i].length; j++) {
				if (this.tabuleiro[i][j] == 0) {
					 return false;
				}
			}
		}
		return true;
	}
	
	public void printTabuleiro(PrintStream saida) {
		saida.println();
        for (int i = 0; i < linhas; i++) {
            saida.print(i + " ");
            if (i % 2 == 0) { // Linhas horizontais: .---.---.
                for (int j = 0; j < tamanho; j++) {
                    saida.print(".");
                    if (j < tamanho - 1) {
                        saida.print(tabuleiro[i][j] != 0 ? "---" : "   ");
                    }
                }
            } else { // Linhas verticais: |   |   |
                for (int j = 0; j < tamanho; j++) {
                    saida.print(tabuleiro[i][j] != 0 ? "|" : " ");
                    if (j < tamanho - 1) {
                        char dono = (quadrados[i/2][j] == 1) ? '1' : (quadrados[i/2][j] == 2 ? '2' : ' ');
                        saida.print(" " + dono + " ");
                    }
                }
            }
            saida.println();
        }
    }
	
	public char getVezAtual() {
		return vezAtual;
	}
	
	public int getPontos1() {
		return this.pontos1;
	}
	public int getPontos2() {
		return this.pontos2;
	}
}
