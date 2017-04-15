package com.felipegiotto.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FGStreamUtils {

	private static final Logger LOGGER = LogManager.getLogger(FGStreamUtils.class);
	
	/**
	 * Consome um stream de forma assíncrona, logando (ou não) cada linha que é lida
	 * 
	 * @param inputStream
	 * @param prefixo : prefixo que será gravado no log antes de cada linha lida do Stream.
	 * Se prefixo == null, não escreve nada no log
	 */
	public static void consomeStream(final InputStream inputStream, final String prefixo) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				    String line;
				    while ((line = reader.readLine()) != null) {
				    	if (prefixo != null) {
				    		LOGGER.debug(prefixo + " " + line);
				    	}
				    }
				} catch (IOException ex) {
					LOGGER.info("Erro ignorado ao ler Stream: " + ex.getLocalizedMessage(), ex);
				}
			}
		}).start();
	}
}
