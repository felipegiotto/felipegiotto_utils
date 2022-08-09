package com.felipegiotto.utils.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.felipegiotto.utils.FGProcessUtils;
import com.felipegiotto.utils.FGStreamUtils;
import com.felipegiotto.utils.ffmpeg.util.FFmpegException;

/**
 * Classe que monta uma linha de comando completa para chamar o "ffmpeg"
 * 
 * TODO: monitorar travamento do ffmpeg (começa a processar o vídeo e, depois de alguns frames, fica travado). Acontecia na versão ffmpeg-94112-gbb11584924. Monitorar, agora que autalizei para ffmpeg-95025-g4ba45a95df.
 * 
 * @author felipegiotto@gmail.com
 */
public class FFmpegCommand {

	private static String FFmpegPath;
	private static final Logger LOGGER = LogManager.getLogger(FFmpegCommand.class);
	private FFmpegParameters parameters = new FFmpegParameters();
	
	public static void setFFmpegPath(String ffmpegPath) {
		FFmpegPath = ffmpegPath;
	}
	
	public static String getFFmpegPath() {
		
		// Verifica se o executável do ffmpeg foi configurado
		if (FFmpegPath == null || !new File(FFmpegPath).exists()) {
			throw new RuntimeException("Parâmetro 'FFmpegPath' não foi configurado corretamente em FFmpegCommand, pois arquivo não existe: " + FFmpegPath);
	
		}
		return FFmpegPath;
	}

	public FFmpegParameters getParameters() {
		return parameters;
	}
	
	public void setParameters(FFmpegParameters parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Executa o ffmpeg, espera o término e confere o retorno do processo.
	 * 
	 * Escreve o progresso do ffmpeg no logger.
	 * 
	 * @throws IOException
	 * @throws InterruptedException 
	 * @throws FFmpegException 
	 */
	public void runAndWait(boolean escreverRetornoLogs) throws IOException, InterruptedException, FFmpegException {
		
		// Executa o FFmpeg
		Process p = run();
		
		// Evento para capturar Ctrl+C, se usuário abortar
		Thread shutdownHook = new Thread() {
	        public void run() {
	        	System.out.println("Finalizando ffmpeg...");
	        	p.destroy();
	        }
	    };
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		try {
			// Mostra o resultado do comando
			FGStreamUtils.consomeStream(p.getInputStream(), escreverRetornoLogs ? "STDOUT" : null, Level.INFO);
			final StringBuilder durationString = new StringBuilder();
			FGStreamUtils.consomeStream(p.getErrorStream(), (line) -> {
				if (escreverRetornoLogs) {
					
					// Analisa a duração do vídeo nas primeiras linhas do FFMPEG, para mostrar progresso
					// OBS: só funciona corretamente quando está processando somente UM vídeo.
					// TODO: melhorar, fazendo funcionar para quando houver mais
					if (durationString.toString().isEmpty() && inputFiles.size() == 1) {
						Float duracaoLinha = FFmpegFileInfo.getVideoDurationFromLine(line);
						if (duracaoLinha != null) {
							durationString.append("total=" + FFmpegParameters.secondsToHMS(duracaoLinha.intValue()) + " ");
						}
					}
					
					// Descarta linhas inúteis
					if (line.contains("x265 [info]")) {
						return;
					}
					
					// Linha de progresso é exibida de forma diferente, sobrescrevendo no mesmo lugar
					// Outras linhas são exibidas como vieram do ffmpeg
					StringBuilder exibir = new StringBuilder();
					exibir.append(line);
					if (line.contains("frame=") && line.contains("bitrate=")) {
						
						// Mostra a duração total do vídeo (se ela foi identificada)
						exibir.append(durationString);
						
						// Volta linha ao início para que seja sobrescrita
						exibir.append('\r');
						System.out.print(exibir);
					} else {
						LOGGER.info("STDERR " + exibir);
					}
				}
			});
	
			// Aguarda o termino e verifica se ocorreu erro
			p.waitFor();
		
		} finally {
			
			// Remove a captura de Ctrl+C
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
		
		FGProcessUtils.conferirRetornoProcesso(p);
	}
	
	public Process run() throws IOException, FFmpegException {
		List<String> commands = buildParameters();
		
		LOGGER.info("Executando comando: " + commands);
		
		ProcessBuilder pb = new ProcessBuilder(commands);
		return pb.start();
	}

	public ArrayList<String> buildParameters() throws IOException, FFmpegException {
		
		ArrayList<String> commands = new ArrayList<>();
		
		// Prioridade (valor 'nice')
		if (processNicePriority != null) {
			if (SystemUtils.IS_OS_UNIX) {
				commands.add("nice");
				commands.add("-n");
				commands.add(processNicePriority.toString());
			} else {
				LOGGER.warn("Argumento 'setProcessNicePriority' só funciona em ambientes Unix-like e será ignorado.");
			}
		}
		
		// Comando ffmpeg
		commands.add(getFFmpegPath());
		
		// Arquivos de entrada
		if (inputFiles.size() == 0) {
			throw new InvalidParameterException("Faltou definir arquivo de entrada com setInputFile!");
		}
		for (String file: inputFiles) {
			commands.add("-i");
			commands.add(file);
		}
		
		// TODO: Tirar daqui e colocar em FFmpegParameters??
		// Se houver mais de um arquivo, precisa especificar a concatenação
		if (inputFiles.size() > 1) {
			commands.add("-filter_complex");
			commands.add("concat=n=" + inputFiles.size() + ":v=1:a=1");
		}
		
		FFmpegFileInfo fileInfoPrimeiroArquivo = new FFmpegFileInfo(new File(inputFiles.get(0)));
		commands.addAll(parameters.buildParameters(fileInfoPrimeiroArquivo));
		
		// Arquivo de saída
		if (outputFile == null) {
			throw new InvalidParameterException("Faltou definir arquivo de saída com setOutputFile!");
		}
		commands.add(outputFile);
		
		return commands;
	}

	private List<String> inputFiles = new ArrayList<>();
	
	/**
	 * Configura o arquivo de entrada, que será processado pelo ffmpeg.
	 * 
	 * Este parâmetro é obrigatório antes de executar o ffmpeg!
	 * 
	 * @param inputFile
	 */
	public void addInputFile(String inputFile) {
		this.inputFiles.add(inputFile);
	}
	
	public void addInputFile(File inputFile) {
		this.inputFiles.add(inputFile.getAbsolutePath());
	}
	
	private String outputFile;
	
	/**
	 * Configura o arquivo de saída, que será gerado pelo ffmpeg.
	 * 
	 * Este parâmetro é obrigatório antes de executar o ffmpeg!
	 * 
	 * @param outputFile
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile.getAbsolutePath();
	}
	
	private Integer processNicePriority;
	
	/**
	 * Configura a prioridade que será executado o ffmpeg (valor 'nice').
	 * 
	 * Por enquanto, só funciona em ambientes Unix-like.
	 * 
	 * Maior prioridade = -20
	 * Menor prioridade = 19
	 * Prioridade padrão = null
	 * Fonte: https://en.wikipedia.org/wiki/Nice_(Unix)
	 * 
	 * @param priority
	 */
	public void setProcessNicePriority(Integer processNicePriority) {
		this.processNicePriority = processNicePriority;
	}
	
	public static boolean isArquivoVideo(String filename) {

		filename = filename.toUpperCase();

		return false
				|| filename.endsWith(".AVI")
				|| filename.endsWith(".FLV") 
				|| filename.endsWith(".MKV")
				|| filename.endsWith(".MOV")
				|| filename.endsWith(".MP4")
				|| filename.endsWith(".MTS")
				|| filename.endsWith(".M4V") 
				|| filename.endsWith(".RMVB") 
				|| filename.endsWith(".WMV") 
				;
	}
}
