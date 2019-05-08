package com.felipegiotto.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Grava um objeto em um arquivo XML e permite recuperá-lo posteriormente.
 * 
 * OBS: 
 * 1. O objeto (e todos os seus "filhos") devem possuir construtores públicos sem parâmetros,
 *    para que a rotina de deserialização possa criá-los posteriormente.
 * 2. Todos os atributos deverão OU ser públicos OU possuir getters e setters.
 * 
 * @author felipegiotto@gmail.com
 */
public class FGXMLUtils {

	/**
	 * Não instanciar - utilizar somente métodos estáticos
	 */
	private FGXMLUtils() { }
	
	
	/**
	 * Serializa um objeto em um arquivo XML
	 * 
	 * @param object
	 * @param outputFile
	 * @throws IOException 
	 * @throws  
	 */
	public static void saveToFile(Object object, File outputFile) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			try (XMLEncoder xmlEncoder = new XMLEncoder(fos)) {
				xmlEncoder.writeObject(object);
			}
		}
	}
	
	/**
	 * Carrega um objeto serializado de um arquivo XML
	 * 
	 * @param inputFile
	 * @return
	 * @throws IOException
	 */
	public static Object loadFromFile(File inputFile) throws IOException {
		try (FileInputStream fis = new FileInputStream(inputFile)) {
			try (XMLDecoder xmlDecoder = new XMLDecoder(fis)) {
				return xmlDecoder.readObject();
			}
		}
	}
}
