package com.felipegiotto.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Métodos auxiliares referentes a arquivos ou pastas
 * 
 * @author felipegiotto@gmail.com
 */
public class FGFileUtils {
	
	/**
	 * Não instanciar - utilizar somente métodos estáticos
	 */
	private FGFileUtils() { }
	
	
	public static void garantirQuePastaExista(Path in) throws IOException {
		if (!Files.isDirectory(in)) {
			throw new IOException("Pasta não existe: " + in);
		}
	}
	
	
	public static void garantirQuePastaExista(File pasta) throws IOException {
		if (!pasta.isDirectory()) {
			throw new IOException("Pasta não existe: " + pasta);
		}
	}

	
	public static void garantirQueArquivoOuPastaExista(File arquivo) throws IOException {
		if (!arquivo.exists()) {
			throw new IOException("Arquivo/Pasta não existe: " + arquivo);
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
		f = new File(caminho.replaceAll("\\\\", ""));
		if (f.exists()) {
			return f;
		}

		// Tenta encontrar removendo aspas simples do início e do final, que acontece ao "Colar nomes de arquivos" no Ubuntu
		if (caminho.startsWith("'") && caminho.endsWith("'")) {
			f = new File(caminho.substring(1, caminho.length()-1));
			if (f.exists()) {
				return f;
			}
		}
		
		if (obrigatorio) {
			throw new FileNotFoundException("Arquivo não existe: " + caminho);
		} else {
			return null;
		}
	}
}
