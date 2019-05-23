package com.felipegiotto.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class FGSincronizarConteudoPastasTest {

	@Test
	public void criarPastasSemConteudo() throws IOException {
		
		// Pasta de origem e destino dos backups
		File origem = new File("tmp/criarPastasSemConteudo/origem");
		File destino = new File("tmp/criarPastasSemConteudo/destino");
		FileUtils.deleteQuietly(origem);
		FileUtils.deleteQuietly(destino);
		origem.mkdirs();
		destino.mkdirs();
		
		// Uma pasta terá arquivos, a outra não
		File pastaComConteudoOrigem = new File(origem, "pastaComConteudo");
		File pastaSemConteudoOrigem = new File(origem, "pastaSemConteudo");
		pastaComConteudoOrigem.mkdirs();
		pastaSemConteudoOrigem.mkdirs();
		File pastaComConteudoDestino = new File(destino, "pastaComConteudo");
		File pastaSemConteudoDestino = new File(destino, "pastaSemConteudo");
		
		// Cria arquivo
		File arquivo1 = new File(pastaComConteudoOrigem, "arquivo.txt");
		arquivo1.createNewFile();
		
		// Efetua backup
		FGSincronizarConteudoPastas s = new FGSincronizarConteudoPastas(origem.toPath(), destino.toPath());
		
		// Confere se somente a pasta com conteúdo foi criada
		s.setDeveCriarPastasSomenteSeHouverConteudo(true);
		s.sincronizar();
		assertTrue(pastaComConteudoDestino.isDirectory());
		assertFalse(pastaSemConteudoDestino.isDirectory());
		
		// Confere se ambas as pastas foram criadas
		s.setDeveCriarPastasSomenteSeHouverConteudo(false);
		s.sincronizar();
		assertTrue(pastaComConteudoDestino.isDirectory());
		assertTrue(pastaSemConteudoDestino.isDirectory());
		
	}
	
	@Test
	public void retornarListaDeArquivosSincronizados() throws Exception {
		File origem = new File("tmp/retornarListaDeArquivosSincronizados/origem");
		File destino = new File("tmp/retornarListaDeArquivosSincronizados/destino");
		FileUtils.deleteQuietly(origem);
		FileUtils.deleteQuietly(destino);
		origem.mkdirs();
		destino.mkdirs();
		
		// Cria arquivos (somente TXTs serão migrados)
		File arquivoSomenteOrigem1 = new File(origem, "arquivoSomenteOrigem1.txt");
		File arquivoSomenteOrigem2 = new File(origem, "arquivoSomenteOrigem2.txt");
		File arquivoSomenteOrigemExtensaoErrada = new File(origem, "arquivoSomenteOrigemExtensaoErrada.dat");
		arquivoSomenteOrigem1.createNewFile();
		arquivoSomenteOrigem2.createNewFile();
		arquivoSomenteOrigemExtensaoErrada.createNewFile();
		
		// Arquivos que já existem no destino (e não na origem) não devem ser apagados
		File arquivoSomenteDestinoAntigo = new File(destino, "arquivoSomenteDestinoAntigo.txt");
		arquivoSomenteDestinoAntigo.createNewFile();

		// Arquivos que já existem na origem e no destino devem ser mantidos
		File arquivoJaSincronizadoOrigem  = new File(origem,  "arquivoJaSincronizado.txt");
		File arquivoJaSincronizadoDestino = new File(destino, "arquivoJaSincronizado.txt");
		arquivoJaSincronizadoOrigem.createNewFile();
		arquivoJaSincronizadoDestino.createNewFile();

		// Efetua backup
		FGSincronizarConteudoPastas s = new FGSincronizarConteudoPastas(origem.toPath(), destino.toPath());
		DirectoryStream.Filter<Path> customFileFilter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path path) {
				return path.toString().endsWith(".txt");
			}
		};
		s.setCustomFileFilter(customFileFilter);
		s.setDeveArmazenarERetornarListaDeArquivosSincronizadosOrigem(true);
		s.sincronizar();
		
		// Confere se arquivos existem corretamente
		File arquivoSomenteOrigem1Destino = new File(destino, arquivoSomenteOrigem1.getName());
		File arquivoSomenteOrigem2Destino = new File(destino, arquivoSomenteOrigem2.getName());
		File arquivoSomenteOrigemExtensaoErradaDestino = new File(destino, arquivoSomenteOrigemExtensaoErrada.getName());
		assertTrue(arquivoSomenteOrigem1Destino.isFile());
		assertTrue(arquivoSomenteOrigem2Destino.isFile());
		assertFalse(arquivoSomenteOrigemExtensaoErradaDestino.isFile());
		
		// Confere se o arquivo antigo (que existe no destino mas não na origem) foi mantido
		assertTrue(arquivoSomenteDestinoAntigo.isFile());
		
		// Confere se o arquivo antigo (que já estava sincronizado) foi mantido
		assertTrue(arquivoJaSincronizadoDestino.isFile());
		
		// Confere se os arquivos foram retornados corretamente
		assertEquals(3, s.getListaDeArquivosSincronizadosOrigem().size());
		Object[] lista = s.getListaDeArquivosSincronizadosOrigem()
				.stream()
				.map((path) -> path.getFileName()) // Pega somente o nome do arquivo
				.sorted()
				.toArray();
		String arquivosSincronizados = StringUtils.join(lista, ", ");
		assertEquals("arquivoJaSincronizado.txt, arquivoSomenteOrigem1.txt, arquivoSomenteOrigem2.txt", arquivosSincronizados);
	}
	
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
