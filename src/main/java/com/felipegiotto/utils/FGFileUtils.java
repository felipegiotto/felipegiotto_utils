package com.felipegiotto.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

/**
 * Métodos auxiliares referentes a arquivos ou pastas
 * 
 * @author felipegiotto@gmail.com
 */
public class FGFileUtils {
	
	public static void garantirQuePastaExista(File in) throws IOException {
		if (!in.isDirectory()) {
			throw new IOException("Pasta não existe: " + in);
		}
	}

	
	public static void garantirQueArquivoOuPastaExista(File in) throws IOException {
		if (!in.exists()) {
			throw new IOException("Arquivo/Pasta não existe: " + in);
		}
	}
	

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
		
		// Remove espaços em branco do início e do fim.
		caminho = caminho.trim();
		
		// Verifica se o caminho informado existe
		File f = new File(caminho);
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
