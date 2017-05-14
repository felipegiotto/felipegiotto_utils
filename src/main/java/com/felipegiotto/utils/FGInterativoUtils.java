package com.felipegiotto.utils;

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
	 * @param opcoes
	 * @return
	 */
	public static int perguntarOpcoesParaUsuario(String pergunta, String... opcoes) {
		
		// Mostra a pergunta e os itens para o usuário
		System.out.println("");
		System.out.println(pergunta);
		int opcaoAtual = 0;
		for (String opcao: opcoes) {
			opcaoAtual++;
			System.out.println(opcaoAtual + ": " + opcao);
		}

		// Identifica o item selecionado pelo usuário.
		String resposta = aguardarRespostaUsuario();
		int respostaInt;
		try {
			respostaInt = Integer.parseInt(resposta);
			if (respostaInt > opcaoAtual || respostaInt < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException ex) {
			throw new RuntimeException("Resposta deve ser um número entre 1 e " + opcaoAtual);
		}

		LOGGER.info("Resposta: " + opcoes[respostaInt-1]);
		return respostaInt;
	}

}
