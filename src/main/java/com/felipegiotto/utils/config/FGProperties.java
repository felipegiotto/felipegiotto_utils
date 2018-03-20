package com.felipegiotto.utils.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.felipegiotto.utils.FGFileUtils;
import com.felipegiotto.utils.exception.NotFoundException;

/**
 * Classe para ler e gravar dados em arquivos ".properties"
 * 
 * @author felipegiotto@gmail.com
 */
public class FGProperties {

	private Properties properties;
	private Path pathArquivo;
	private static final String NULL_VALUE = "___NULL__VALUE___";
	
	public FGProperties(Path pathArquivo, boolean obrigatorio) throws IOException {
		this.pathArquivo = pathArquivo;
		this.properties = FGFileUtils.carregarArquivoProperties(pathArquivo, obrigatorio);
	}
	
	public void save(String comentario) throws IOException {
		FGFileUtils.salvarArquivoProperties(properties, pathArquivo, comentario);
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
		return isEmptyOrNullValue(value) ? null : value;
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
}
