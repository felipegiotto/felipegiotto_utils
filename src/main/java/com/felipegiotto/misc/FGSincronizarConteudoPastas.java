package com.felipegiotto.misc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.felipegiotto.utils.config.FGProperties;

/**
 * Sincroniza pastas (para criação de backups), de forma parecida com o que o rsync faz.
 * 
 * TODO: Permitir excluir backups antigos (sufixo .bkYYYYMMDDHHMM), informando uma data 
 *       limite (nesse caso, excluindo os mais antigos do que a data informada), permitindo
 *       manter somente os X últimos (independente da data de modificação) ou então limpar TODOS.
 *
 * TODO: efetuar backups de arquivos antigos TODOS com o mesmo timestamp (para isolar os backups 
 *       gerados em uma determinada execução)
 * 
 * TODO: excluir backups de arquivos antigos progressivamente (localizar todos, organizar por 
 *       data e apagar em "fatias", ex: 5% a cada confirmação do usuário)
 * 
 * TODO: permitir escolher arquivos que não deve fazer backup de versões antigas
 * 
 * @author felipegiotto@gmail.com
 */
public class FGSincronizarConteudoPastas {

	private static final Logger LOGGER = LogManager.getLogger(FGSincronizarConteudoPastas.class);
	private static final short SEGUNDOS_PARA_MOSTRAR_PROGRESSO = 1;
	private static final File arquivoEstatisticas = new File("tmp/estatisticas_copias.properties");
	private static DecimalFormat dfBytes = new DecimalFormat("###,###");
	private String nome;
	private Path pastaOrigem;
	private Path pastaDestino;
	private boolean simulacao = false;
	private boolean excluirArquivosDoDestinoQueNaoExistemNaOrigem = false;
	private boolean preservarVersoesAntigasDeArquivos = false;
	private boolean executando = false;
	private DirectoryStream.Filter<Path> globalFileFilter;
	private DirectoryStream.Filter<Path> customFileFilter;
	private int qtdWarnings = 0;
	private List<String> warnings = new ArrayList<>();
	private int qtdErros = 0;
	private List<String> erros = new ArrayList<>();
	private String sufixoBackupArquivosAntigos = ".bk" + DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(LocalDateTime.now());

	private boolean deveCopiarArquivoSeTamanhosForemDiferentes = true;
	private boolean deveCopiarArquivoSeDatasForemDiferentes = true;
	private long toleranciaMaximaDataModificacaoMillis = 0;
	
	// Variáveis para mostrar progresso:
	private Path pastaSendoCopiadaAgora;
	private Path arquivoSendoCopiadoAgora;
	private long totalPastasConferidas = 0;
	private long totalBytesArquivosCopiados = 0;
	private long qtdArquivosOrigem = 0;
	private long totalBytesArquivosOrigem = 0;
	private String nomeEstatisticaTotalPastasCopiadas;
	private String nomeEstatisticaTempoUltimaExecucao;
	private long estatisticaTotalPastasCopiadasNaUltimaExecucao = 0;
	private long estatisticaTempoUltimaExecucao = 0;
	private long qtdArquivosCopiados = 0;
	private long qtdArquivosExcluidos = 0;
	private long qtdArquivosRenomeados = 0;
	private long totalBytesArquivosJaEstavamSincronizados = 0;
	private long qtdArquivosJaEstavamSincronizados = 0;
	private StopWatch tempoExecucao;
	private StopWatch tempoManipulandoArquivos;

	public FGSincronizarConteudoPastas(String nome, Path pastaOrigem, Path pastaDestino) throws IOException {
		init(nome + " - ", pastaOrigem, pastaDestino);
	}

	public FGSincronizarConteudoPastas(Path pastaOrigem, Path pastaDestino) throws IOException {
		init("", pastaOrigem, pastaDestino);
	}

	private static final Pattern pArquivoBackup = Pattern.compile("\\.bk\\d{12}$");
	
	private void init(String nome, Path pastaOrigem, Path pastaDestino) throws IOException {
		this.pastaOrigem = pastaOrigem;
		this.pastaDestino = pastaDestino;
		this.nome = nome;

		// Carrega estatisticas da ultima execucao
		FGProperties estatisticasUltimaExecucao = new FGProperties(arquivoEstatisticas.toPath(), false);
		nomeEstatisticaTotalPastasCopiadas = "total_pastas_copiadas_" + pastaOrigem;
		estatisticaTotalPastasCopiadasNaUltimaExecucao = estatisticasUltimaExecucao.getLong(nomeEstatisticaTotalPastasCopiadas, 0L);
		nomeEstatisticaTempoUltimaExecucao = "tempo_ultima_execucao_" + pastaOrigem + "___" + pastaDestino;
		estatisticaTempoUltimaExecucao = estatisticasUltimaExecucao.getLong(nomeEstatisticaTempoUltimaExecucao, 0L);

		this.globalFileFilter = new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException {
				String name = entry.getFileName().toString();
				String nameLower = name.toLowerCase();
				if (name.equals(".DS_Store") || name.equals("iPod Photo Cache") || nameLower.contains("icon?")) {
					return false;
				}

				// Ignora arquivos de backup que foram gerados pela ferramenta (terminados em ".bkYYYYMMDDHHMM")
				Matcher mArquivoBackup = pArquivoBackup.matcher(name);
				if (mArquivoBackup.find()) {
					return false;
				}
				
				return true;
			}
		};

		if (!Files.isDirectory(pastaOrigem)) {
			throw new IOException(nome + "Pasta de origem não existe: " + pastaOrigem);
		}
		try (DirectoryStream<Path> filhosOrigem = Files.newDirectoryStream(pastaOrigem, globalFileFilter)) {
			boolean conseguiuLerAlgumFilho = false;
			for (@SuppressWarnings("unused") Path entry : filhosOrigem) {
				conseguiuLerAlgumFilho = true;
				break;
			}
			if (!conseguiuLerAlgumFilho) {
				throw new IOException(nome + "Pasta de origem não pode ser lida ou está vazia: " + pastaOrigem);
			}
		}

		if (!Files.isDirectory(pastaDestino)) {
			throw new IOException(nome + "Pasta de destino não existe: " + pastaDestino);
		}
		if (!Files.isWritable(pastaDestino)) {
			throw new IOException(nome + "Pasta de destino não pode ser gravada: " + pastaDestino);
		}
	}

	public void sincronizar() throws IOException {

		if (executando) {
			throw new IOException("Rotina já está em execução");
		}
		executando = true;
		try {
			tempoExecucao = new StopWatch();
			tempoExecucao.start();
			tempoManipulandoArquivos = new StopWatch();
			tempoManipulandoArquivos.start();
			tempoManipulandoArquivos.suspend();

			// Thread que mostra o progresso da cópia
			Thread threadProgresso = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {

						mostrarPastaAtual();
						try {
							Thread.sleep(5_000);
						} catch (InterruptedException ex) {
							return;
						}
					}
				}
			});
			threadProgresso.start();
			try {
				processarRecursivamente(pastaOrigem, pastaDestino);
			} catch (Exception ex) {
				LOGGER.error(nome + "Erro inesperado: " + ex.getLocalizedMessage(), ex);
				qtdErros++;
				erros.add("Erro inesperado: " + ex.getLocalizedMessage());
				throw ex;
			} finally {
				threadProgresso.interrupt();
			}

		} finally {
			executando = false;
		}

		mostrarPastaAtual();

		if (!simulacao) {

			// Grava estatísticas da última execucao
			FGProperties estatisticasUltimaExecucao = new FGProperties(arquivoEstatisticas.toPath(), false);
			estatisticasUltimaExecucao.setLong(nomeEstatisticaTotalPastasCopiadas, totalPastasConferidas);
			estatisticasUltimaExecucao.setLong(nomeEstatisticaTempoUltimaExecucao, getTempoExecucaoMillis() - getTempoExecucaoCopiandoArquivosMillis());
			arquivoEstatisticas.getParentFile().mkdirs();
			estatisticasUltimaExecucao.save("Estatísticas de execução de backups");
		}
	}

	public boolean sincronizarSafe() {
		try {
			sincronizar();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private long getTempoExecucaoMillis() {
		if (tempoExecucao != null) {
			return tempoExecucao.getTime();
		} else {
			return 0;
		}
	}

	private long getTempoExecucaoCopiandoArquivosMillis() {
		if (tempoManipulandoArquivos != null) {
			return tempoManipulandoArquivos.getTime();
		} else {
			return 0;
		}
	}
	
	private void processarRecursivamente(Path origem, Path destino) throws IOException {

		Path pastaSendoCopiadaAntes = pastaSendoCopiadaAgora;
		pastaSendoCopiadaAgora = origem;
		// try { Thread.sleep(5); } catch (InterruptedException ex ) {}

		// Exclui arquivos que não existem mais
		if (excluirArquivosDoDestinoQueNaoExistemNaOrigem) {
			excluirArquivosInexistentes(origem, destino);
		}

		try (DirectoryStream<Path> filhosOrigem = Files.newDirectoryStream(origem, this.globalFileFilter)) {

			for (Path filhoOrigem : filhosOrigem) {

				if (customFileFilter != null && !customFileFilter.accept(filhoOrigem)) {
					LOGGER.debug(nome + "Ignorando " + filhoOrigem);
					continue;
				}

				// Às vezes, HD ejeta incorretamente mas esse FOR continua sendo executado,
				// "entendendo" que os filhos não existem mais na origem e apagando-os.
				// Por isso, a cada verificação de arquivo filho, verifica se o "pai" continua existindo,
				// para evitar este tipo de problema.
				pastaDeveExistir(origem);

				Path filhoDestino = Paths.get(destino.toString(), filhoOrigem.getFileName().toString());

				if (Files.isSymbolicLink(filhoOrigem)) {
					LOGGER.debug(nome + "Ignorando link simbolico: " + filhoOrigem);

				} else if (Files.isDirectory(filhoOrigem)) {

					if (!simulacao) {
						
						// Pode ocorrer de um arquivo passar a ser, posteriormente, uma pasta.
						// Nesse caso, é preciso excluir (ou renomear) o backup antigo (que era um arquivo)
						if (Files.isRegularFile(filhoDestino)) {
							if (preservarVersoesAntigasDeArquivos) {
								renomearArquivoParaBackup(filhoDestino, true);
							} else {
								excluirRecursivamente(filhoDestino);
							}
						}
						try {
							Files.createDirectories(filhoDestino);
						} catch (IOException ex) {
							qtdErros++;
							String erro = "Erro criando pasta '" + filhoDestino + "': " + ex.getLocalizedMessage();
							if (erros.size() < 100) {
								erros.add(erro);
							}
							LOGGER.error(nome + erro, ex);
							continue;
						}
					}
					processarRecursivamente(filhoOrigem, filhoDestino);

				} else if (Files.isRegularFile(filhoOrigem)) {
					if (Files.isReadable(filhoOrigem)) {
						long tamanhoFilhoOrigem = Files.size(filhoOrigem);
						totalBytesArquivosOrigem += tamanhoFilhoOrigem;
						qtdArquivosOrigem++;

						if (deveSicronizarArquivo(filhoOrigem, filhoDestino)) {
							try {
								arquivoSendoCopiadoAgora = filhoDestino;
								
								if (preservarVersoesAntigasDeArquivos) {
									renomearArquivoParaBackup(filhoDestino, true);
								} else {
									excluirRecursivamente(filhoDestino);
								}
								
								copiarArquivoSetarAtributos(filhoOrigem, filhoDestino);
							} catch (IOException ex) {
								qtdErros++;
								if (erros.size() < 100) {
									String erro = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
									if (!erro.contains(filhoOrigem.toString())) {
										erro += " - " + filhoOrigem;
									}
									erros.add(erro);
								}
								LOGGER.error(nome + ex.getLocalizedMessage() + ": " + filhoOrigem, ex);
							} finally {
								arquivoSendoCopiadoAgora = null;
							}

						} else {
							qtdArquivosJaEstavamSincronizados++;
							totalBytesArquivosJaEstavamSincronizados += tamanhoFilhoOrigem;
						}

					} else {
						qtdWarnings++;
						if (warnings.size() < 100) {
							warnings.add("Não consigo ler: " + filhoOrigem);
						}
						LOGGER.warn(nome + "Não consigo ler: " + filhoOrigem);
					}

				} else {
					qtdWarnings++;
					if (warnings.size() < 100) {
						warnings.add("Não sei o que é este arquivo: " + filhoOrigem);
					}
					LOGGER.warn(nome + "Não sei o que é este arquivo: " + filhoOrigem);
				}
			}
		}

		totalPastasConferidas++;
		pastaSendoCopiadaAgora = pastaSendoCopiadaAntes;
	}

	/**
	 * Renomeia um arquivo antigo do backup, inserindo um sufixo ".bkYYYYMMDDHHMM"
	 * 
	 * @param arquivoPasta
	 * @throws IOException 
	 */
	private void renomearArquivoParaBackup(Path arquivoPasta, boolean excluirSeNaoConseguirRenomear) throws IOException {
		if (!simulacao && preservarVersoesAntigasDeArquivos) {
			if (Files.exists(arquivoPasta)) {

				Path arquivoBackup = Paths.get(arquivoPasta.toString() + sufixoBackupArquivosAntigos);

				// Se já existe um backup antigo com o mesmo nome, exclui esse
				// backup antes de renomear o original
				if (Files.exists(arquivoBackup)) {
					excluirRecursivamente(arquivoBackup);
				}

				// Renomeia o arquivo ou pasta atual, inserindo o novo sufixo
				LOGGER.info(nome + "Mantendo versao antiga em: " + arquivoBackup);
				try {
					tempoManipulandoArquivos.resume();
					try {
						Files.move(arquivoPasta, arquivoBackup);
					} finally {
						tempoManipulandoArquivos.suspend();
					}
				} catch (IOException ex) {

					// Pode ocorrer erro, por exemplo, se o caminho for muito
					// longo por causa do sufixo
					// Ex: java.nio.file.FileSystemException:
					// /Users/taeta/encfs_mount_bkfelipe3/Users/taeta/Documents/Concursos/2017-04
					// - Auditor Receita Estrategia/Curso/Direito Tributario
					// 2017-2018/Aula 11/05_Extincao do credito tributario -
					// Pagamento
					// indevido__fabiodutra-direitotributarioextincaodocreditotributario-pagamento-indevido-640x360.mp4
					// -> /Users/taeta/...../xxx640x360.mp4.bk201805210630: File
					// name too long
					if (excluirSeNaoConseguirRenomear) {

						String msg = "Nao consegui renomear versao antiga de arquivo, sera excluido: " + arquivoPasta;
						LOGGER.warn(msg);
						qtdWarnings++;
						if (warnings.size() < 100) {
							warnings.add(msg);
						}

						excluirRecursivamente(arquivoPasta);
					} else {
						throw ex;
					}
				}
				qtdArquivosRenomeados++;

			}
		}
	}

	private void pastaDeveExistir(Path origem) throws IOException {
		
		if (!Files.isDirectory(origem)) {
			throw new IOException("Pasta não existe mais: " + origem);
		}
	}

	private void copiarArquivoSetarAtributos(Path filhoOrigem, Path filhoDestino) throws IOException {

		LOGGER.debug(nome + "Copiando arquivo " + filhoOrigem);
		if (!simulacao) {
			copiarArquivo(filhoOrigem, filhoDestino);
			
			Files.setLastModifiedTime(filhoDestino, Files.getLastModifiedTime(filhoOrigem));
			
			try {
				Files.setPosixFilePermissions(filhoDestino, Files.getPosixFilePermissions(filhoOrigem));
			} catch (UnsupportedOperationException ex) {
				// Não fazer nada, pois FS de destino não suporta permissões Posix
			}
			
		}
		qtdArquivosCopiados++;
	}

	public void copiarArquivo(Path filhoOrigem, Path filhoDestino) throws IOException {
		
		if (!simulacao) {
			
			tempoManipulandoArquivos.resume();
			try {
			
				long ultimoProgresso = System.currentTimeMillis();
				long tamanhoOrigem = Files.size(filhoOrigem);
				try (InputStream fis = Files.newInputStream(filhoOrigem)) {
					try (OutputStream os = Files.newOutputStream(filhoDestino)) {
						byte[] buffer = new byte[10_000];
						int bytesRead;
						long totalRead = 0;
						long bytesReadDesdeUltimoProgresso = 0;
						while ((bytesRead = fis.read(buffer)) > 0) {
							totalRead += bytesRead;
							bytesReadDesdeUltimoProgresso += bytesRead;
							totalBytesArquivosCopiados += bytesRead;
							os.write(buffer, 0, bytesRead);
							long tempoDesdeUltimoProgresso = System.currentTimeMillis() - ultimoProgresso;
							if (tempoDesdeUltimoProgresso > SEGUNDOS_PARA_MOSTRAR_PROGRESSO * 1000) {
								LOGGER.info(nome + "* " + (totalRead * 1000 / tamanhoOrigem / 10.0) + "% ("
										+ FileUtils.byteCountToDisplaySize(totalRead) + "/"
										+ FileUtils.byteCountToDisplaySize(tamanhoOrigem) + " - "
										+ FileUtils.byteCountToDisplaySize(bytesReadDesdeUltimoProgresso) + "/s)");
								ultimoProgresso = System.currentTimeMillis();
								bytesReadDesdeUltimoProgresso = 0;
							}
						}
					}
				}
				
			} finally {
				tempoManipulandoArquivos.suspend();
			}
		}
	}

	private void mostrarPastaAtual() {
		StringBuilder sb = new StringBuilder();
		sb.append(nome + "(" + totalPastasConferidas);
		if (estatisticaTotalPastasCopiadasNaUltimaExecucao > totalPastasConferidas) {
			sb.append("/" + estatisticaTotalPastasCopiadasNaUltimaExecucao);
		}
		sb.append(" pastas");
		if (executando && estatisticaTempoUltimaExecucao > 0) {
			long tempo = getTempoExecucaoMillis() - getTempoExecucaoCopiandoArquivosMillis();
			if (tempo < estatisticaTempoUltimaExecucao) {
				sb.append(" - ETA > " + DurationFormatUtils.formatDurationHMS(estatisticaTempoUltimaExecucao - tempo));
			}
		}
		sb.append(")");
		if (pastaSendoCopiadaAgora != null) {
			sb.append(" " + pastaSendoCopiadaAgora);
		}
		if (arquivoSendoCopiadoAgora != null) {
			sb.append("/" + arquivoSendoCopiadoAgora.getFileName());
		}
		LOGGER.info(sb.toString());
	}

	public void mostrarResultados() throws IOException {

		if (!StringUtils.isBlank(nome)) {
			LOGGER.info("========== " + nome.split(" - ")[0] + " ==========");
		}

		// Primeiro mostra erros
		if (qtdErros > 0) {
			LOGGER.error("Arquivos com erro (detalhes nos logs): " + qtdErros + ". Amostra:");
			for (String erro : erros) {
				LOGGER.error("* " + erro);
			}
		} else {
			LOGGER.info("Nenhum erro!");
		}

		// Depois mostra warnings
		if (qtdWarnings > 0) {
			LOGGER.warn("Warnings (detalhes nos logs): " + qtdWarnings + ". Amostra:");
			for (String warning: warnings) {
				LOGGER.warn("* " + warning);
			}
		} else {
			LOGGER.info("Nenhum warning!");
		}

		// Depois mostra INFO
		LOGGER.info("Pasta origem:                          " + pastaOrigem);
		LOGGER.info("Pasta destino:                         " + pastaDestino);
		LOGGER.info("Tempo total de execução:               " + tempoExecucao);
		LOGGER.info("Tempo manipulando arquivos:            " + tempoManipulandoArquivos);
		LOGGER.info("Qtd pastas conferidas na origem:       " + totalPastasConferidas);
		LOGGER.info("Arquivos conferidos na origem:         " + qtdArquivosOrigem + " - "
				+ byteCountToDisplaySize(totalBytesArquivosOrigem));
		LOGGER.info("Arquivos copiados para destino:        " + qtdArquivosCopiados + " - "
				+ byteCountToDisplaySize(totalBytesArquivosCopiados));
		LOGGER.info("Arquivos/pastas excluídos do destino:  " + qtdArquivosExcluidos);
		LOGGER.info("Arquivos/pastas renomeados no destino: " + qtdArquivosRenomeados);
		LOGGER.info("Arquivos previamente sincronizados:    " + qtdArquivosJaEstavamSincronizados + " - "
				+ byteCountToDisplaySize(totalBytesArquivosJaEstavamSincronizados));

		try {
			FileStore destinoStore = Files.getFileStore(pastaDestino);
			long totalSpace = destinoStore.getTotalSpace();
			long usableSpace = destinoStore.getUsableSpace();
			StringBuilder descricaoEspacoLivre = new StringBuilder();
			descricaoEspacoLivre.append(byteCountToDisplaySize(usableSpace) + " de " + byteCountToDisplaySize(totalSpace));
			if (totalSpace > 0) {
				descricaoEspacoLivre.append(" (" + (usableSpace * 1000 / totalSpace / 10.0) + "%)");
			}
			LOGGER.info("Espaço livre no destino:              " + descricaoEspacoLivre);
		} catch (IOException ex) {
		}

		LOGGER.info("\n\n");
	}

	private static DecimalFormat df = new DecimalFormat("###,###");

	private static String byteCountToDisplaySize(long bytes) {
		StringBuilder sb = new StringBuilder();
		sb.append(FileUtils.byteCountToDisplaySize(bytes));
		if (bytes > 0) {
			sb.append("/" + df.format(bytes) + "B");
		}

		return sb.toString();
	}
	
	private boolean deveSicronizarArquivo(Path origem, Path destino) throws IOException {

		if (!Files.exists(destino)) {
			LOGGER.info(nome + "Destino não existe: " + destino);
			return true;
		}

		if (deveCopiarArquivoSeTamanhosForemDiferentes) {
			long tamanhoOrigem = Files.size(origem);
			long tamanhoDestino = Files.size(destino);
			if (tamanhoOrigem != tamanhoDestino) {
				LOGGER.info(nome + "Tamanho diferente (" + dfBytes.format(tamanhoOrigem) + " - " + dfBytes.format(tamanhoDestino) + "): " + origem);
				return true;
			}
		}

		// Por causa de diferentes configurações de timezone, arquivos podem
		// estar sincronizados e possuir várias horas de diferença (já encontrei até 6
		// horas).
		// Por isso, vou usar a seguinte regra:
		// 1. Se a diferença for maior que 10 horas, sincroniza.
		// 2. Se a diferença for menor que 10 horas mas todos os outros campos
		// forem iguais (até o milisegundo), considera que arquivo está OK.
		if (deveCopiarArquivoSeDatasForemDiferentes) {
			FileTime origemTime = Files.getLastModifiedTime(origem);
			FileTime destinoTime = Files.getLastModifiedTime(destino);
			long diferencaTotal = Math.abs(origemTime.toMillis() - destinoTime.toMillis());
			long diferencaEmHoras = diferencaTotal / (60 * 60 * 1000);
			long restoDiferencaEmHoras = diferencaTotal % (60 * 60 * 1000);
			if ((diferencaTotal > toleranciaMaximaDataModificacaoMillis) && (diferencaEmHoras > 10 || restoDiferencaEmHoras > 0)) {
				LOGGER.info(nome + "Data de modificacao diferente (" + origemTime + " - " + destinoTime + " - Diferenca de "
						+ DurationFormatUtils.formatDurationHMS(diferencaTotal) + "): " + origem);
				return true;
			}
		}

		return false;
	}

	private long ultimaExibicaoListagemArquivosInexistentes = 0;
	private void excluirArquivosInexistentes(Path origem, Path destino) throws IOException {

		if (Files.exists(destino) && Files.isDirectory(destino) && Files.isReadable(destino)) {
			
			// Mostra que está listando arquivos, mas não a toda hora.
			long agora = System.currentTimeMillis();
			if (agora - ultimaExibicaoListagemArquivosInexistentes > 10_000) {
				LOGGER.debug(nome + "Listando arquivos do destino, para exclusao: " + destino);
				ultimaExibicaoListagemArquivosInexistentes = agora;
			}
			
			// Itera sobre todos os filhos
			try (DirectoryStream<Path> filhosDestino = Files.newDirectoryStream(destino, this.globalFileFilter)) {
				for (Path filhoDestino : filhosDestino) {
					
					// Às vezes, HD ejeta incorretamente mas esse FOR continua sendo executado,
					// "entendendo" que os filhos não existem mais na origem e apagando-os.
					// Por isso, a cada verificação de arquivo filho, verifica se o "pai" continua existindo,
					// para evitar este tipo de problema.
					pastaDeveExistir(origem);
					
					Path filhoOrigem = Paths.get(origem.toString(), filhoDestino.getFileName().toString());
					if (!Files.exists(filhoOrigem)) {
						
						if (preservarVersoesAntigasDeArquivos) {
							renomearArquivoParaBackup(filhoDestino, true);
							
						} else {
							LOGGER.info(nome + "Excluindo pois não existe mais na origem: " + filhoDestino);
							excluirRecursivamente(filhoDestino);
						}
					}
				}
			}
		}
	}

	private void excluirRecursivamente(Path destino) throws IOException {
		if (Files.isDirectory(destino)) {
			final List<Object> pathsToDelete = Files.walk(destino).sorted(Comparator.reverseOrder())
					.collect(Collectors.toList());
			for (Object path : pathsToDelete) {
				LOGGER.info("Excluindo " + path);
				if (!simulacao) {
					
					tempoManipulandoArquivos.resume();
					try {
						Files.deleteIfExists((Path) path);
					} finally {
						tempoManipulandoArquivos.suspend();
					}
				}
				qtdArquivosExcluidos++;
			}
			
		} else {
			
			tempoManipulandoArquivos.resume();
			try {
				Files.deleteIfExists(destino);
				qtdArquivosExcluidos++;
				
			} catch (Exception ex) {
				LOGGER.warn("Não consegui excluir " + destino);
				qtdWarnings++;
				if (warnings.size() < 100) {
					warnings.add("Não consegui excluir " + destino);
				}
				
			} finally {
				tempoManipulandoArquivos.suspend();
			}
		}
	}

	public void setSimulacao(boolean simulacao) {
		this.simulacao = simulacao;
	}

	public boolean isSimulacao() {
		return simulacao;
	}
	
	public void setExcluirArquivosDoDestinoQueNaoExistemNaOrigem(
			boolean excluirArquivosDoDestinoQueNaoExistemNaOrigem) {
		this.excluirArquivosDoDestinoQueNaoExistemNaOrigem = excluirArquivosDoDestinoQueNaoExistemNaOrigem;
	}

	public void setPreservarVersoesAntigasDeArquivos(boolean preservarVersoesAntigasDeArquivos) {
		this.preservarVersoesAntigasDeArquivos = preservarVersoesAntigasDeArquivos;
	}
	
	public Path getPastaOrigem() {
		return pastaOrigem;
	}

	public Path getPastaDestino() {
		return pastaDestino;
	}

	public String getNome() {
		return nome;
	}
	
	public long getTotalBytesArquivosOrigem() {
		return totalBytesArquivosOrigem;
	}

	public void setCustomFileFilter(DirectoryStream.Filter<Path> customFilenameFilter) {
		this.customFileFilter = customFilenameFilter;
	}
	
	public void setDeveCopiarArquivoSeDatasForemDiferentes(boolean deveCopiarArquivoSeDatasForemDiferentes) {
		this.deveCopiarArquivoSeDatasForemDiferentes = deveCopiarArquivoSeDatasForemDiferentes;
	}
	
	public void setToleranciaMaximaDataModificacaoMillis(long millis) {
		this.toleranciaMaximaDataModificacaoMillis = millis;
	}
	
	public void setDeveCopiarArquivoSeTamanhosForemDiferentes(boolean deveCopiarArquivoSeTamanhosForemDiferentes) {
		this.deveCopiarArquivoSeTamanhosForemDiferentes = deveCopiarArquivoSeTamanhosForemDiferentes;
	}

	public void mostrarEstatisticaUltimaExecucao() {
		if (estatisticaTempoUltimaExecucao > 0) {
			LOGGER.info(
					nome + "Última execução: " + DurationFormatUtils.formatDurationHMS(estatisticaTempoUltimaExecucao));
		} else {
			LOGGER.info(nome + "Última execução: (tempo desconhecido)");
		}
	}

	public static void main(String[] args) throws Exception {
		
		FGSincronizarConteudoPastas s = new FGSincronizarConteudoPastas(new File("/Users/taeta/Desktop/Lixo").toPath(), new File("/Users/taeta/Desktop/Lixo2").toPath());
		s.simulacao = false;
		s.excluirArquivosDoDestinoQueNaoExistemNaOrigem = true;
		s.preservarVersoesAntigasDeArquivos = true;
		s.sincronizar();
		s.mostrarResultados();
		LOGGER.info("FIM!");
	}
}
