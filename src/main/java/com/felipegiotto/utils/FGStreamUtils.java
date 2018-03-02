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

	private static final Logger LOGGER = LogManager.getLogger(FGStreamUtils.class);
	
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
