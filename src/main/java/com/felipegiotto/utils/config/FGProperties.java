package com.felipegiotto.utils.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.felipegiotto.utils.exception.NotFoundException;

/**
 * Classe para ler e gravar dados em arquivos ".properties"
 * 
 * TODO: Testes unitarios
 * 
 * TODO: Implementar keySet, propertyNames (ver qual funciona de acordo com o esperado)
 * 
 * @author felipegiotto@gmail.com
 */
public class FGProperties {

	private Properties properties;
	private Path pathArquivo;
	private static final String NULL_VALUE = "___NULL__VALUE___";
	private boolean changed = false;
	
	public FGProperties(Path pathArquivo, boolean obrigatorio) throws IOException {
		this.pathArquivo = pathArquivo;
		this.properties = carregarArquivoProperties(pathArquivo, obrigatorio);
	}
	
	/**
	 * Carrega um objeto Properties de um arquivo
	 * 
	 * TODO: Criar FGDateUtils, com formatadores "padrão" para diversos formatos conhecidos, sempre utilizando SafeSimpleDateFormat (SQL Date, SQL Timestamp, D/M/Y, D/M/Y/H/M/S, H/M/S)
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
	 * Carrega um objeto Properties de um arquivo
	 * 
	 * @param arquivo : arquivo de entrada, com os dados que serão populados no objeto
	 * @param obrigatorio : se "true" e se o arquivo de entrada não existir, lança uma exceção
	 * @return
	 * @throws IOException
	 */
	public static Properties carregarArquivoProperties(Path arquivo, boolean obrigatorio) throws IOException {
		Properties props = new Properties();
		if (Files.isRegularFile(arquivo)) {
			try (InputStream fis = Files.newInputStream(arquivo)) {
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

	/**
	 * Grava um objeto Properties em um arquivo.
	 * @param properties
	 * @param arquivoSaida
	 * @param comentario : texto inserido na primeira linha do arquivo
	 * @throws IOException
	 */
	public static void salvarArquivoProperties(Properties properties, Path arquivoSaida, String comentario)
			throws IOException {
		try (OutputStream fos = Files.newOutputStream(arquivoSaida)) {
			properties.store(fos, comentario);
		}
	}
	
	public void save() throws IOException {
		salvarArquivoProperties(properties, pathArquivo, null);
		changed = false;
	}
	
	public void save(String comment) throws IOException {
		salvarArquivoProperties(properties, pathArquivo, comment);
		changed = false;
	}
	
	/**
	 * Salva em arquivo, mas somente se houve alguma modificação em relação ao objeto original.
	 * 
	 * OBS: para que a detecção de alterações funcione corretamente, devem ser utilizados os "setters"
	 * da classe FGProperties. Se o objeto properties (retornado com "getProperties()") for manipulado
	 * diretamente, a detecção não funcionará corretamente.
	 * 
	 * @throws IOException
	 */
	public void saveIfModified() throws IOException {
		if (changed) {
			save();
		}
	}
	
	/**
	 * Salva em arquivo, mas somente se houve alguma modificação em relação ao objeto original.
	 * 
	 * OBS: para que a detecção de alterações funcione corretamente, devem ser utilizados os "setters"
	 * da classe FGProperties. Se o objeto properties (retornado com "getProperties()") for manipulado
	 * diretamente, a detecção não funcionará corretamente.
	 * 
	 * @param comment : comentário que será inserido automaticamente no cabeçalho do arquivo salvo.
	 * @throws IOException
	 */
	public void saveIfModified(String comment) throws IOException {
		if (changed) {
			save(comment);
		}
	}
	
	/**
	 * Indica se uma propriedade existe
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return properties.containsKey(key);
	}
	
	private boolean isEmptyOrNullValue(String value) {
		return NULL_VALUE.equals(value) || StringUtils.isEmpty(value);
	}
	
	public void remove(String key) {
		properties.remove(key);
		changed = true;
	}
	
	/*********************** String ***********************/
	
	/**
	 * Grava uma propriedade no formato String.
	 * 
	 * O valor pode, inclusive, ser NULL.
	 * 
	 * @param key
	 * @param value
	 */
	public void setString(String key, String value) {
		properties.setProperty(key, value != null ? value : NULL_VALUE);
		changed = true;
	}
	
	/**
	 * Lê uma propriedade no formato String. 
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setString(key, null); 
	 * 
	 * Se a propriedade não existir, returna NULL.
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		String value = properties.getProperty(key);
		return NULL_VALUE.equals(value) ? null : value;
	}
	
	/**
	 * Lê uma propriedade no formato String.
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setString(key, null);
	 * 
	 * Se a propriedade não existir, lança {@link NotFoundException}
	 * 
	 * @param key
	 * @return
	 */
	public String getStringMandatory(String key) {
		if (properties.containsKey(key)) {
			return getString(key);			
		} else {
			throw new NotFoundException("Property not found: " + key);
		}
	}
	
	/**
	 * Lê uma propriedade no formato String.
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setString(key, null);
	 * 
	 * Se ela não existir, retorna o valor default
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key, String defaultValue) {
		if (properties.containsKey(key)) {
			return getString(key);			
		} else {
			return defaultValue;
		}
	}
	
	/*********************** Integer ***********************/
	
	/**
	 * Grava uma propriedade no formato Integer.
	 * 
	 * O valor pode, inclusive, ser NULL.
	 * 
	 * @param key
	 * @param value
	 */
	public void setInt(String key, Integer value) {
		properties.setProperty(key, value != null ? Integer.toString(value) : NULL_VALUE);
		changed = true;
	}
	
	/**
	 * Lê uma propriedade no formato Integer. 
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setInt(key, null); 
	 * 
	 * Se a propriedade não existir, returna NULL.
	 * 
	 * @param key
	 * @return
	 */
	public Integer getInt(String key) {
		String value = properties.getProperty(key);
		return isEmptyOrNullValue(value) ? null : Integer.parseInt(value);
	}
	
	/**
	 * Lê uma propriedade no formato Integer.
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setInteger(key, null);
	 * 
	 * Se a propriedade não existir, lança {@link NotFoundException}
	 * 
	 * @param key
	 * @return
	 */
	public Integer getIntegerMandatory(String key) {
		if (properties.containsKey(key)) {
			return getInt(key);			
		} else {
			throw new NotFoundException("Property not found: " + key);
		}
	}
	
	/**
	 * Lê uma propriedade no formato Integer.
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setInteger(key, null);
	 * 
	 * Se ela não existir, retorna o valor default
	 * 
	 * @param key
	 * @return
	 */
	public Integer getInt(String key, Integer defaultValue) {
		if (properties.containsKey(key)) {
			return getInt(key);			
		} else {
			return defaultValue;
		}
	}

	/*********************** Long ***********************/
	
	/**
	 * Grava uma propriedade no formato Long.
	 * 
	 * O valor pode, inclusive, ser NULL.
	 * 
	 * @param key
	 * @param value
	 */
	public void setLong(String key, Long value) {
		properties.setProperty(key, value != null ? Long.toString(value) : NULL_VALUE);
		changed = true;
	}
	
	/**
	 * Lê uma propriedade no formato Long. 
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setLong(key, null); 
	 * 
	 * Se a propriedade não existir, returna NULL.
	 * 
	 * @param key
	 * @return
	 */
	public Long getLong(String key) {
		String value = properties.getProperty(key);
		return isEmptyOrNullValue(value) ? null : Long.parseLong(value);
	}

	/**
	 * Lê uma propriedade no formato Long.
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setLong(key, null);
	 * 
	 * Se a propriedade não existir, lança {@link NotFoundException}
	 * 
	 * @param key
	 * @return
	 */
	public Long getLongMandatory(String key) {
		if (properties.containsKey(key)) {
			return getLong(key);			
		} else {
			throw new NotFoundException("Property not found: " + key);
		}
	}
	
	/**
	 * Lê uma propriedade no formato Long.
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setLong(key, null);
	 * 
	 * Se ela não existir, retorna o valor default
	 * 
	 * @param key
	 * @return
	 */
	public Long getLong(String key, Long defaultValue) {
		if (properties.containsKey(key)) {
			return getLong(key);			
		} else {
			return defaultValue;
		}
	}

	/*********************** Double ***********************/
	
	/**
	 * Grava uma propriedade no formato Double.
	 * 
	 * O valor pode, inclusive, ser NULL.
	 * 
	 * @param key
	 * @param value
	 */
	public void setDouble(String key, Double value) {
		properties.setProperty(key, value != null ? Double.toString(value) : NULL_VALUE);
		changed = true;
	}
	
	/**
	 * Lê uma propriedade no formato Double. 
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setDouble(key, null); 
	 * 
	 * Se a propriedade não existir, returna NULL.
	 * 
	 * @param key
	 * @return
	 */
	public Double getDouble(String key) {
		String value = properties.getProperty(key);
		return isEmptyOrNullValue(value) ? null : Double.parseDouble(value);
	}
	
	/**
	 * Lê uma propriedade no formato Double.
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setDouble(key, null);
	 * 
	 * Se a propriedade não existir, lança {@link NotFoundException}
	 * 
	 * @param key
	 * @return
	 */
	public Double getDoubleMandatory(String key) {
		if (properties.containsKey(key)) {
			return getDouble(key);			
		} else {
			throw new NotFoundException("Property not found: " + key);
		}
	}
	
	/**
	 * Lê uma propriedade no formato Double.
	 * 
	 * O valor pode, inclusive, ser NULL, se foi utilizado setDouble(key, null);
	 * 
	 * Se ela não existir, retorna o valor default
	 * 
	 * @param key
	 * @return
	 */
	public Double getDouble(String key, Double defaultValue) {
		if (properties.containsKey(key)) {
			return getDouble(key);			
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Indica se houve alguma modificação neste objeto depois que ele foi criado (ou instanciado de um arquivo)
	 * 
	 * @return
	 */
	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * Retorna o objeto Properties original.
	 * 
	 * @return
	 */
	public Properties getProperties() {
		return properties;
	}
}
