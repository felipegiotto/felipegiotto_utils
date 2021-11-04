package com.felipegiotto.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
	
	@Test
	public void testSincronizarArquivosConformeData() throws Exception {
		File origem = new File("tmp/testSincronizarArquivosConformeData/origem");
		File destino = new File("tmp/testSincronizarArquivosConformeData/destino");
		FileUtils.deleteQuietly(origem);
		FileUtils.deleteQuietly(destino);
		origem.mkdirs();
		destino.mkdirs();
		
		// Cria um arquivo
		File arquivoOrigem = new File(origem, "arquivo1");
		arquivoOrigem.createNewFile();
				
		FGSincronizarConteudoPastas s = new FGSincronizarConteudoPastas(origem.toPath(), destino.toPath());
		
		verificarSeAcusaDataDiferente(true, LocalDateTime.of(2001, 1, 1, 1, 1), LocalDateTime.of(2002, 2, 2, 2, 2), "Dias diferentes", s);
		verificarSeAcusaDataDiferente(true, LocalDateTime.of(2001, 1, 1, 0, 0, 0), LocalDateTime.of(2001, 1, 1, 0, 0, 5), "5 minutos de diferença", s);
		
		// Diferenças muito pequenas (5ms, com parâmetro para configurar)
		verificarSeAcusaDataDiferente(true,  LocalDateTime.of(2001, 1, 1, 0, 0, 0, 0), LocalDateTime.of(2001, 1, 1, 0, 0, 0, 5000000), "5ms de diferença", s);
		s.setToleranciaMaximaDataModificacaoMillis(6);
		verificarSeAcusaDataDiferente(false, LocalDateTime.of(2001, 1, 1, 0, 0, 0, 0), LocalDateTime.of(2001, 1, 1, 0, 0, 0, 5000000), "5ms de diferença", s);
		s.setToleranciaMaximaDataModificacaoMillis(0);
		
		verificarSeAcusaDataDiferente(false, LocalDateTime.of(2001, 1, 1, 1, 1), LocalDateTime.of(2001, 1, 1, 1, 1), "Exatamente igual", s);
		verificarSeAcusaDataDiferente(false, LocalDateTime.of(2001, 1, 1, 3, 0, 0), LocalDateTime.of(2001, 1, 1, 4, 0, 0), "Exatamente uma hora de diferença (timezone)", s);
		
		// 55ms de diferença, mais a diferença de timezones (6h entre a hora identificada no Mac e no Windows)
		// Data de modificacao diferente (2011-12-09T11:39:40.0556Z - 2011-12-09T17:39:40Z - Diferenca de 05:59:59.945): T:\ComBackup\Digitalizacoes e Comprovantes Antigos\Antigos\2011 - Apartamento 812 - Granada - Porto Alegre - Imobiliaria Guarida\2011-02-16 - Vistoria.pdf
		verificarSeAcusaDataDiferente(true,  LocalDateTime.of(2016, 10, 19, 3, 46, 2, 55600000), LocalDateTime.of(2016, 10, 19, 9, 46, 2, 0), "Diferença em timezone+milisegundos (windows/mac)", s);
		s.setToleranciaMaximaDataModificacaoMillis(55);
		verificarSeAcusaDataDiferente(false, LocalDateTime.of(2016, 10, 19, 3, 46, 2, 55600000), LocalDateTime.of(2016, 10, 19, 9, 46, 2, 0), "Diferença em timezone+milisegundos (windows/mac)", s);
		s.setToleranciaMaximaDataModificacaoMillis(0);
	}

	private void verificarSeAcusaDataDiferente(boolean deveSerDiferente, LocalDateTime data1, LocalDateTime data2, String explicacao, FGSincronizarConteudoPastas sincronizador) {
		long millisData1 = data1.toInstant(ZoneOffset.UTC).toEpochMilli();
		long millisData2 = data2.toInstant(ZoneOffset.UTC).toEpochMilli();
		assertEquals(explicacao, deveSerDiferente, sincronizador.deveSincronizarPorTeremDatasDiferentes(FileTime.fromMillis(millisData1), FileTime.fromMillis(millisData2), explicacao));
	}
}
