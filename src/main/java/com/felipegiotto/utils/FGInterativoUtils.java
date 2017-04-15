package com.felipegiotto.utils;

import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class FGInterativoUtils {

	/**
	 * Faz uma "pergunta" para o usuário (stdout) e aguarda sua resposta (stdin)
	 * @param pergunta
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

}
