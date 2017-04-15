package com.felipegiotto.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FGFileUtils {

	/**
	 * Indica se uma pasta (child) é subdiretório de outra (base)
	 * 
	 * Fonte: http://www.java2s.com/Tutorial/Java/0180__File/Checkswhetherthechilddirectoryisasubdirectoryofthebasedirectory.htm
	 * 
	 * @param base
	 * @param child
	 * @return
	 * @throws IOException
	 */
	public static boolean isSubDirectory(File base, File child) throws IOException {
		base = base.getCanonicalFile();
		child = child.getCanonicalFile();

		File parentFile = child;
		while (parentFile != null) {
			if (base.equals(parentFile)) {
				return true;
			}
			parentFile = parentFile.getParentFile();
		}
		return false;
	}

	public static File tentarIdentificarArquivoDaString(String caminho, boolean obrigatorio) throws FileNotFoundException {
		
		// Verifica se o caminho informado existe, depois de retirar espaços em
		// branco do início e do fim.
		File f = new File(caminho.trim());
		if (f.exists()) {
			return f;
		}

		// Tenta encontrar arquivo apagando "\ ", que pode aparecer ao copiar arquivos
		// no Finder e colar no Terminal.
		f = new File(caminho.replaceAll("\\\\ ", " "));
		if (f.exists()) {
			return f;
		}

		if (obrigatorio) {
			throw new FileNotFoundException("Arquivo não existe: " + caminho);
		} else {
			return null;
		}
	}

}
