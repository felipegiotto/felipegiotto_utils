package com.felipegiotto.misc;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FGSincronizarConteudoPastasTest {

	@Test
	public void testArquivoQueViraPasta() throws Exception {
		File origem = new File("tmp/testArquivoQueViraPasta/origem");
		File destino = new File("tmp/testArquivoQueViraPasta/destino");
		FileUtils.deleteQuietly(origem);
		FileUtils.deleteQuietly(destino);
		origem.mkdirs();
		destino.mkdirs();
		
		// Cria um arquivo e uma pasta
		File arquivoOrigem = new File(origem, "arquivo1");
		File pastaOrigem = new File(origem, "pasta1");
		arquivoOrigem.createNewFile();
		pastaOrigem.mkdirs();
		
		// Efetua backup
		FGSincronizarConteudoPastas s = new FGSincronizarConteudoPastas(origem.toPath(), destino.toPath());
		s.setPreservarVersoesAntigasDeArquivos(true);
		s.sincronizar();
		
		// Confere se ambos existem no destino
		File arquivoDestino = new File(destino, "arquivo1");
		File pastaDestino = new File(destino, "pasta1");
		assertTrue(arquivoDestino.isFile());
		assertTrue(pastaDestino.isDirectory());
		
		// Troca de arquivo para pasta e vice versa
		FileUtils.deleteQuietly(arquivoOrigem);
		FileUtils.deleteQuietly(pastaOrigem);
		arquivoOrigem.mkdirs();
		pastaOrigem.createNewFile();
		
		// Efetua backup novamente
		s.sincronizar();
		
		// Confere se o destino mudou
		assertTrue(arquivoDestino.isDirectory());
		assertTrue(pastaDestino.isFile());
		
		// Troca novamente de pasta para arquivo e vice versa
		FileUtils.deleteQuietly(arquivoOrigem);
		FileUtils.deleteQuietly(pastaOrigem);
		arquivoOrigem.createNewFile();
		pastaOrigem.mkdirs();
		
		// Efetua backup novamente
		s.sincronizar();
		
		// Confere se o destino voltou a ser como era
		assertTrue(arquivoDestino.isFile());
		assertTrue(pastaDestino.isDirectory());
	}
}
