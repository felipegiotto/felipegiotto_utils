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

/**
 * Classe que monta uma linha de comando completa para chamar o "ffmpeg"
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
	 */
	public void runAndWait(boolean escreverRetornoLogs) throws IOException, InterruptedException {
		
		// Executa o FFmpeg
		Process p = run();
		
		// Mostra o resultado do comando
		FGStreamUtils.consomeStream(p.getInputStream(), escreverRetornoLogs ? "STDOUT" : null, Level.INFO);
		FGStreamUtils.consomeStream(p.getErrorStream(), escreverRetornoLogs ? "STDERR" : null, Level.INFO);

		// Aguarda o termino e verifica se ocorreu erro
		p.waitFor();
		FGProcessUtils.conferirRetornoProcesso(p);
	}
	
	public Process run() throws IOException {
		List<String> commands = buildParameters();
		
		LOGGER.info("Executando comando: " + commands);
		
		ProcessBuilder pb = new ProcessBuilder(commands);
		return pb.start();
	}

	public ArrayList<String> buildParameters() throws IOException {
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
		commands.add(FFmpegPath);
		
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
		
		commands.addAll(parameters.buildParameters());
		
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

		return filename.endsWith(".AVI")
				|| filename.endsWith(".MKV")
				|| filename.endsWith(".MP4")
				|| filename.endsWith(".M4V") 
				|| filename.endsWith(".WMV") 
				|| filename.endsWith(".FLV") 
				|| filename.endsWith(".RMVB") 
				|| filename.endsWith(".MOV");
	}
}
