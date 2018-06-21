package com.felipegiotto.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class FGXMLUtilsTest {

	@Test
	public void testSerializar() throws IOException {
		
		// Arquivo onde dados serão gravados e lidos
		File arquivo = new File("tmp/FGXMLUtilsTest.xml");
		arquivo.delete();
		
		// Objeto que será serializado
		Map<Integer, String> hash = new HashMap<>();
		hash.put(1, "Um");
		hash.put(2, "Dois");
		
		// Executa operação
		FGXMLUtils.saveToFile(hash, arquivo);
		
		// Confere se arquivo foi gerado
		assertTrue(arquivo.isFile());
		
		// Confere se arquivo é carregado
		Map<Integer, String> hash2 = (Map<Integer, String>) FGXMLUtils.loadFromFile(arquivo);
		assertEquals("Um", hash2.get(1));
		assertEquals("Dois", hash2.get(2));
	}
}
