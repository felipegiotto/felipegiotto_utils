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

import com.felipegiotto.misc.FGMenuConsole;

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
	 * @return string digitada pelo usuário ou NULL se ele deixou em branco.
	 */
	public static String aguardarRespostaUsuario() {
		return aguardarRespostaUsuario(null);
	}
	
	/**
	 * Aguarda uma resposta do usuário (seguida de enter) na "stdin"
	 * 
	 * @return string digitada pelo usuário ou o parâmetro "valorPadrao" se ele deixou em branco.
	 */
	public static String aguardarRespostaUsuario(String valorPadrao) {
		
		// Mantém scanner aberto, pois senão não dá para chamar mais de uma vez
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		String line = scanner.nextLine();
		if (StringUtils.isBlank(line)) {
			return valorPadrao;
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
		int respostaInt = perguntarNumeroInteiroParaUsuario(pergunta.toString(), false, (numero) -> (numero >= 1 && numero <= opcoes.length) || (numero == 0 && exibirOpcaoVoltar));
		if (respostaInt != 0) {
			LOGGER.info("Resposta: " + opcoes[respostaInt-1]);
		}
		return respostaInt;
	}

	public static int perguntarOpcoesParaUsuario(String tituloPergunta, String... opcoes) {
		return perguntarOpcoesParaUsuario(tituloPergunta, false, opcoes);
	}
	
	public static boolean perguntarSimOuNaoParaUsuario(String pergunta) {
		return perguntarSimOuNaoParaUsuario(pergunta, null);
	}
	
	/**
	 * Faz uma pergunta para o usuário, mostrando uma série de opções. 
	 * Em seguida, pede para que o usuário informe o número da opção selecionada.
	 * 
	 * @param pergunta
	 * @param valorPadrao : se informado, indica o valor padrão se o usuário simplesmente pressionar ENTER. Se não informado, o usuário será obrigado a digitar S ou N.
	 * @return
	 */
	public static boolean perguntarSimOuNaoParaUsuario(String pergunta, Boolean valorPadrao) {
		
		// Faz um loop até que o usuário responda SIM ou NAO
		while(true) {
			
			// Mostra a pergunta e as opções para o usuário
			StringBuilder mensagem = new StringBuilder("\n" + pergunta + " (S/N");
			if (valorPadrao != null) {
				mensagem.append(" - Padrão=");
				mensagem.append(valorPadrao ? "S" : "N");
			}
			mensagem.append(")");
			System.out.println(mensagem);
	
			// Identifica o item selecionado pelo usuário.
			String resposta = aguardarRespostaUsuario();
			if (resposta != null) {
				resposta = resposta.toUpperCase();
				if ("S".equals(resposta) || "Y".equals(resposta)) {
					return true;
					
				} else if ("N".equals(resposta)) {
					return false;
					
				}
			}
			
			if (valorPadrao != null) {
				return valorPadrao;
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
	 * @param aceitarNull : permite aceitar resposta em branco (será retornado null)
	 * @param aceitarNumero : permite definir uma regra de aceite. Utilizar lambda expression, ex: "(numero) -> numero > 0"
	 * @return
	 */
	public static Integer perguntarNumeroInteiroParaUsuario(String pergunta, boolean aceitarBranco, AceitarNumero aceitarNumero) {
		while (true) {
			System.out.println("");
			System.out.println(pergunta);
			String resposta = aguardarRespostaUsuario();
			if (resposta == null && aceitarBranco) {
				return null;
			}
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
		return perguntarNumeroInteiroParaUsuario(pergunta, false, null);
	}
	
	/**
	 * Exibe um submenu com um título e uma lista de itens que podem ser selecionados e executados.
	 * 
	 * Este submenu será persistente, ou seja, quando a operação terminar ele será exibido novamente.
	 * 
	 * Exemplo de utilização com lambda expressions:
	 * <pre>
		exibirSubmenuPersistente("Selecione item", true, (itens) -> {
			itens.put("Operação 1", () -> { System.out.println("Executando operação 1"); });
			itens.put("Operação 2", () -> { System.out.println("Executando operação 2"); });
			itens.put("Operação 3", () -> { System.out.println("Executando operação 3"); });
		});
	 * </pre>
	 * @deprecated Utilizar {@link FGMenuConsole}
	 * @param titulo
	 * @param descartarExceptions indica se exceções disparadas durante as operações serão descartadas (depois de registrar no log)
	 * @param coletorSubmenus
	 */
	public static void exibirSubmenuPersistente(String titulo, boolean descartarExceptions, ColetorSubmenus coletorSubmenus) throws Exception {
		while (exibirSubmenu(titulo, descartarExceptions, coletorSubmenus)) {
			// Retornará automaticamente quando usuário selecionar "Voltar"
		}
	}
	
	/**
	 * Exibe um submenu com um título e uma lista de itens que podem ser selecionados e executados.
	 * 
	 * Este submenu será exibido uma única vez.
	 * @see #exibirSubmenuPersistente(String, boolean, ColetorSubmenus)
	 * 
	 * Exemplo de utilização com lambda expressions:
	 * <pre>
		exibirSubmenu("Selecione item", true, (itens) -> {
			itens.put("Operação 1", () -> { System.out.println("Executando operação 1"); });
			itens.put("Operação 2", () -> { System.out.println("Executando operação 2"); });
			itens.put("Operação 3", () -> { System.out.println("Executando operação 3"); });
		});
	 * </pre>
	 * 
	 * @deprecated Utilizar {@link FGMenuConsole}
	 * @param titulo
	 * @param descartarExceptions indica se exceções disparadas durante as operações serão descartadas (depois de registrar no log)
	 * @param coletorSubmenus
	 */
	public static boolean exibirSubmenu(String titulo, boolean descartarExceptions, ColetorSubmenus coletorSubmenus) throws Exception {
		
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
			return false;
			
		} else {
			RunnableComException runnable = targets.get(opcao - 1);
			if (runnable != null) {
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
			return true;
		}
	}

	/**
	 * Exibe um submenu com um título e uma lista de itens que podem ser selecionados e executados.
	 * Qualquer exceção lançada durante a execução dos submenus será logada e, em seguida, descartada.
	 * 
	 * Ver {@link #exibirSubmenuPersistente(String, boolean, ColetorSubmenus)}
	 * 
	 * @deprecated Utilizar {@link FGMenuConsole}
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
		public void coletar(Map<String, RunnableComException> lista) throws Exception;
	}
	
	public interface RunnableComException {
		public void run() throws Exception;
	}
}
