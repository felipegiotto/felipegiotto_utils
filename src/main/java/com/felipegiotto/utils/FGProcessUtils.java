package com.felipegiotto.utils;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FGProcessUtils {

	private static final Logger LOGGER = LogManager.getLogger(FGProcessUtils.class);
	
	/**
	 * Confere se um processo retornou o valor esperado. Se não, lança uma exceção.
	 * 
	 * @param process
	 * @param valorEsperado
	 */
	public static void conferirRetornoProcesso(Process process, int valorEsperado) {
		int exitValue = process.exitValue();
		if (exitValue != valorEsperado) {
			throw new RuntimeException("ERRO! Processo deveria retornar " + valorEsperado + ", mas retornou " + exitValue + "!");
		}
	}

	/**
	 * Confere se um processo retornou 0. Se não, lança uma exceção.
	 * 
	 * @param process
	 */
	public static void conferirRetornoProcesso(Process process) {
		conferirRetornoProcesso(process, 0);
	}

	/**
	 * Executa um determinado comando com seus parâmetros (dispara um processo)
	 * 
	 * @param parametros
	 * @return
	 * @throws IOException
	 */
	public static Process executarComando(List<String> parametros) throws IOException {

		ProcessBuilder processBuilder = new ProcessBuilder(parametros);

		LOGGER.debug("Executando comando " + StringUtils.join(parametros, " ") + "...");
		Process p = processBuilder.start();
		return p;
	}
	
	/**
	 * Executa um determinado comando com seus parâmetros (dispara um processo),
	 * consome seus streams e aguarda seu término
	 * 
	 * @param parametros
	 * @param escreverSaidaNosLogs : indica se cada linha retornada pelo processo
	 * deverá ser registrada nos logs
	 * @return
	 * @throws IOException
	 */
	public static Process executarComandoAguardarTermino(List<String> parametros, boolean escreverSaidaNosLogs) throws IOException, InterruptedException {

		// Executa o comando
		Process p = executarComando(parametros);

		// Mostra o resultado do comando
		FGStreamUtils.consomeStream(p.getInputStream(), escreverSaidaNosLogs ? "STDOUT" : null);
		FGStreamUtils.consomeStream(p.getErrorStream(), escreverSaidaNosLogs ? "STDERR" : null);

		// Aguarda o termino e verifica se ocorreu erro
		p.waitFor();

		return p;
	}
}
