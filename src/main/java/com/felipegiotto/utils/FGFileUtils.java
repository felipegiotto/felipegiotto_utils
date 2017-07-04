package com.felipegiotto.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Métodos auxiliares referentes a arquivos ou pastas
 * 
 * @author felipegiotto@gmail.com
 */
public class FGFileUtils {
	
	/**
	 * Carrega um objeto Properties de um arquivo
	 * 
	 * TODO: Criar uma classe (ex: FGFileProperties), que já receba um arquivo na sua inicialização e, a cada chamada ao método "saveToFile", reescreve.
	 * TODO: Criar FGDateUtils, com formatadores "padrão" para diversos formatos conhecidos, sempre utilizando SafeSimpleDateFormat (SQL Date, SQL Timestamp, D/M/Y, D/M/Y/H/M/S, H/M/S)
	 *   
	 * @param arquivo : arquivo de entrada, com os dados que serão populados no objeto
	 * @param obrigatorio : se "true" e se o arquivo de entrada não existir, lança uma exceção
	 * @return
	 * @throws IOException
	 */
	public static Properties carregarArquivoProperties(File arquivo, boolean obrigatorio) throws IOException {
		Properties props = new Properties();
		if (arquivo.exists() && arquivo.isFile()) {
			try (FileInputStream fis = new FileInputStream(arquivo)) {
				props.load(fis);
			}
		} else if (obrigatorio) {
			throw new FileNotFoundException("Arquivo não existe: " + arquivo);
		}
		return props;
	}

	
	/**
	 * Grava um objeto Properties em um arquivo.
	 * @param properties
	 * @param arquivoSaida
	 * @param comentario : texto inserido na primeira linha do arquivo
	 * @throws IOException
	 */
	public static void salvarArquivoProperties(Properties properties, File arquivoSaida, String comentario)
			throws IOException {
		try (FileOutputStream fos = new FileOutputStream(arquivoSaida)) {
			properties.store(fos, comentario);
		}
	}


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
