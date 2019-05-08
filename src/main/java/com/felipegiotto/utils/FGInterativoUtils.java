package com.felipegiotto.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Métodos auxiliares referentes a interação com usuário do sistema, através da stdout e
 * stdin.
 * 
 * @author felipegiotto@gmail.com
 */
public class FGInterativoUtils {

	/**
	 * Não instanciar - utilizar somente métodos estáticos
	 */
	private FGInterativoUtils() { }
	
	private static final Logger LOGGER = LogManager.getLogger(FGInterativoUtils.class);
	
	/**
	 * Aguarda uma resposta do usuário (seguida de enter) na "stdin"
	 * 
	 * @return
	 */
	public static String aguardarRespostaUsuario() {
		
		// Mantém scanner aberto, pois senão não dá para chamar mais de uma vez
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		String line = scanner.nextLine();
		if (StringUtils.isBlank(line)) {
			return null;
		} else {
			return line;
		}
	}

	/**
	 * Faz uma pergunta para o usuário, mostrando uma série de opções. 
	 * Em seguida, pede para que o usuário informe o número da opção selecionada.
	 * 
	 * @param pergunta
	 * @param exibirOpcaoVoltar : indica se a última opção (com valor 0) será "(Voltar)"
	 * @param opcoes
	 * @return um número de "1" até o tamanho de "opcoes", ou "0" caso o usuário escolha "Voltar" (ver parâmetro "exibirOpcaoVoltar")
	 */
	public static int perguntarOpcoesParaUsuario(String tituloPergunta, boolean exibirOpcaoVoltar, String... opcoes) {
		
		// Mostra a pergunta e os itens para o usuário
		StringBuilder pergunta = new StringBuilder(tituloPergunta);
		int opcaoAtual = 0;
		for (String opcao: opcoes) {
			opcaoAtual++;
			pergunta.append("\n" + opcaoAtual + ": " + opcao);
		}
		if (exibirOpcaoVoltar) {
			pergunta.append("\n0: (Voltar)");
		}

		// Identifica o item selecionado pelo usuário.
		int respostaInt = perguntarNumeroInteiroParaUsuario(pergunta.toString(), (numero) -> (numero >= 1 && numero <= opcoes.length) || (numero == 0 && exibirOpcaoVoltar));
		if (respostaInt != 0) {
			LOGGER.info("Resposta: " + opcoes[respostaInt-1]);
		}
		return respostaInt;
	}

	public static int perguntarOpcoesParaUsuario(String tituloPergunta, String... opcoes) {
		return perguntarOpcoesParaUsuario(tituloPergunta, false, opcoes);
	}
	/**
	 * Faz uma pergunta para o usuário, mostrando uma série de opções. 
	 * Em seguida, pede para que o usuário informe o número da opção selecionada.
	 * 
	 * @param pergunta
	 * @param opcoes
	 * @return
	 */
	public static boolean perguntarSimOuNaoParaUsuario(String pergunta) {
		
		// Faz um loop até que o usuário responda SIM ou NAO
		while(true) {
			
			// Mostra a pergunta para o usuário
			System.out.println("");
			System.out.println(pergunta + " (S/N)");
	
			// Identifica o item selecionado pelo usuário.
			String resposta = aguardarRespostaUsuario();
			if (resposta != null) {
				resposta = resposta.toUpperCase();
				if ("S".equals(resposta)) {
					return true;
				} else if ("N".equals(resposta)) {
					return false;
				}
			}
			System.out.println("Resposta inválida! Responda com 'S' ou 'N'.");
		}
	}
	
	public static interface AceitarNumero {
		boolean aceitar(int numero);
	}
	
	/**
	 * Faz uma pergunta para o usuário, aceitando somente números inteiros como resposta
	 * 
	 * @param pergunta
	 * @param aceitarNumero : permite definir uma regra de aceite. Utilizar lambda expression, ex: "(numero) -> numero > 0"
	 * @return
	 */
	public static int perguntarNumeroInteiroParaUsuario(String pergunta, AceitarNumero aceitarNumero) {
		while (true) {
			System.out.println("");
			System.out.println(pergunta);
			String resposta = aguardarRespostaUsuario();
			try {
				int numero = Integer.parseInt(resposta);
				if (aceitarNumero != null && !aceitarNumero.aceitar(numero)) {
					throw new NumberFormatException();
				}
				return numero;
			} catch (NumberFormatException ex) {
				System.out.println("Valor inválido!");
			}
		}
	}
	
	/**
	 * Faz uma pergunta para o usuário, aceitando somente números inteiros como resposta
	 * 
	 * @param pergunta
	 * @return
	 */
	public static int perguntarNumeroInteiroParaUsuario(String pergunta) {
		return perguntarNumeroInteiroParaUsuario(pergunta, null);
	}
	
	/**
	 * Exibe um submenu com um título e uma lista de itens que podem ser selecionados e executados.
	 * 
	 * Exemplo de utilização com lambda expressions:
	 * <pre>
		exibirSubmenuPersistente("Selecione item", true, (itens) -> {
			itens.put("Operação 1", () -> { System.out.println("Executando operação 1"); });
			itens.put("Operação 2", () -> { System.out.println("Executando operação 2"); });
			itens.put("Operação 3", () -> { System.out.println("Executando operação 3"); });
		});
	 * </pre>
	 * 
	 * @param titulo
	 * @param descartarExceptions indica se exceções disparadas durante as operações serão descartadas (depois de registrar no log)
	 * @param coletorSubmenus
	 */
	public static void exibirSubmenuPersistente(String titulo, boolean descartarExceptions, ColetorSubmenus coletorSubmenus) throws Exception {
		while (true) {
			Map<String, RunnableComException> lista = new LinkedHashMap<>();
			coletorSubmenus.coletar(lista);
			List<String> titulos = new ArrayList<>();
			List<RunnableComException> targets = new ArrayList<>();
			for (Entry<String, RunnableComException> entry : lista.entrySet()) {
				titulos.add(entry.getKey());
				targets.add(entry.getValue());
			}
			int opcao = perguntarOpcoesParaUsuario(titulo, true, titulos.toArray(new String[0]));
			if (opcao == 0) {
				return;
				
			} else {
				RunnableComException runnable = targets.get(opcao - 1);
				try {
					runnable.run();
				} catch (Exception ex) {
					if (descartarExceptions) {
						LOGGER.error(ex.getLocalizedMessage(), ex);
					} else {
						throw ex;
					}
				}
			}
		}
	}
	
	/**
	 * Exibe um submenu com um título e uma lista de itens que podem ser selecionados e executados.
	 * Qualquer exceção lançada durante a execução dos submenus será logada e, em seguida, descartada.
	 * 
	 * Ver {@link #exibirSubmenuPersistente(String, boolean, ColetorSubmenus)}
	 * 
	 * @param titulo
	 * @param coletorSubmenus
	 */
	public static void exibirSubmenuPersistente(String titulo, ColetorSubmenus coletorSubmenus) {
		try {
			exibirSubmenuPersistente(titulo, true, coletorSubmenus);
		} catch (Exception e) {
			// Não deve entrar neste catch por causa do parâmetro "true" acima.
			e.printStackTrace();
		}
	}
	
	public interface ListaItens {
		public void add(String titulo);
	}
	
	public interface ColetorSubmenus {
		public void coletar(Map<String, RunnableComException> lista);
	}
	
	public interface RunnableComException {
		public void run() throws Exception;
	}
}
