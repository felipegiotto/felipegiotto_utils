package com.felipegiotto.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Métodos auxiliares referentes a streams
 * 
 * @author felipegiotto@gmail.com
 */
public class FGStreamUtils {

	public interface LineFound {
		public void process(String line);
	}
	
	private static final Logger LOGGER = LogManager.getLogger(FGStreamUtils.class);
	
	/**
	 * Não instanciar - utilizar somente métodos estáticos
	 */
	private FGStreamUtils() { }
	
	
	/**
	 * Consome um stream de forma assíncrona, chamando um callback para cada linha encontrada.
	 * 
	 * Sugestão: utilizar com lambda expressions, ex:
	 * 
	 * <pre>consomeStream(inputStream, (line) -> System.out.println(line));</pre>
	 * 
	 * @param inputStream que será lido
	 * @param lineFound callback chamado para cada linha encontrada
	 */
	public static void consomeStream(final InputStream inputStream, LineFound lineFound) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				    String line;
				    while ((line = reader.readLine()) != null) {
				    	if (lineFound != null) {
				    		lineFound.process(line);
				    	}
				    }
				} catch (IOException ex) {
					LOGGER.debug("Erro ignorado ao ler Stream: " + ex.getLocalizedMessage(), ex);
				}
			}
		}).start();
	}
	
	
	/**
	 * Consome um stream de forma assíncrona, logando (ou não) cada linha que é lida
	 *
	 * Se prefixo == null || level == null, não escreve nada no log
	 * 
	 * @param inputStream
	 * @param prefixo : prefixo que será gravado no log antes de cada linha lida do Stream.
	 * @param level : nível de log utilizado para escrever no log
	 */
	public static void consomeStream(final InputStream inputStream, final String prefixo, final Level level) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				    String line;
				    while ((line = reader.readLine()) != null) {
				    	if (prefixo != null && level != null) {
				    		LOGGER.log(level, prefixo + " " + line);
				    	}
				    }
				} catch (IOException ex) {
					if (level != null) {
						LOGGER.log(level, "Erro ignorado ao ler Stream: " + ex.getLocalizedMessage(), ex);
					}
				}
			}
		}).start();
	}
	
	public static void consomeStream(final InputStream inputStream, final String prefixo) {
		consomeStream(inputStream, prefixo, Level.DEBUG);
	}
}
