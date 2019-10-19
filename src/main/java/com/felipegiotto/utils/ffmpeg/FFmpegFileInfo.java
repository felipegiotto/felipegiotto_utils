package com.felipegiotto.utils.ffmpeg;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.felipegiotto.utils.ffmpeg.util.FFmpegException;

/**
 * Classe com funções adicionais utilizando ffmpeg, como por exemplo, leitura de metadados de arquivos.
 * 
 * @author felipegiotto@gmail.com
 */
public class FFmpegFileInfo {

	private static final Logger LOGGER = LogManager.getLogger(FFmpegFileInfo.class);
	private static final Pattern PATTERN_DURATION = Pattern.compile("Duration: (\\d+):(\\d+):(\\d+)\\.(\\d+)");
	private static final Pattern PATTERN_FPS = Pattern.compile("([0-9\\.]+) fps");

	private File file;
	List<String> cacheFileInfo;
	
	public FFmpegFileInfo(File file) {
		this.file = file;
	}
	
	/**
	 * Lê todos os metadados de um arquivo, utilizando ffmpeg, e mantém em cache.
	 * 
	 * @return linhas com os metadados retornados pelo ffmpeg
	 * @throws IOException
	 */
	public List<String> getFullFileInfo() throws IOException {
		
		if (cacheFileInfo == null) {
			List<String> comandos = new ArrayList<>();
			
			comandos.add(FFmpegCommand.getFFmpegPath());
			comandos.add("-i");
			comandos.add(file.getAbsolutePath());
			
			// Oculta informações inúteis, como versões das bibliotecas
			comandos.add("-hide_banner");
	
			ProcessBuilder pb = new ProcessBuilder(comandos);
			Process p = pb.start();
			
			cacheFileInfo = new ArrayList<>();
			
			try (Scanner scanner = new Scanner(p.getErrorStream())) {
				while (scanner.hasNextLine()) {
					cacheFileInfo.add(scanner.nextLine());
				}
			}
	
			// Aguarda o termino e verifica se ocorreu erro
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
			
			// Processo retorna 1
			// Auxiliar.confereRetornoProcesso(p);
		}
		
		return cacheFileInfo;
	}

	// Ex: "date            : 2012-04-05T18:22:59-0300"
	// Ex: "date-por        : 2012-04-05T18:22:59-0300"
	// Ex: "com.apple.quicktime.creationdate: 2019-04-18T19:03:52-0300"
	private static final Pattern pTimestampVideos = Pattern.compile("(date|date-por|creation_time)\\s*\\:\\s(.*)");

	public LocalDateTime getCreationDateTime(boolean ajustarTimeZone) throws IOException {
		LocalDateTime encontrado = null;
		
		for (String line: getFullFileInfo()) {
			Matcher m = pTimestampVideos.matcher(line);
			if (m.find()) {
				String timestampString = m.group(2);
				// LOGGER.debug("Achei timestamp: " + line);
				
				LocalDateTime timestamp = converterTimestampParaLocalDateTime(timestampString, ajustarTimeZone);
				
				// Armazena o menor timestamp encontrado, pois pode haver timestamps em GMT e
				// timestamps com a timezone local (-2 ou -3) nos metadados.
				// Neste caso é melhor pegar a local, que vai ser MENOR do que a GMT.
				// OBS: Isso só vale para fusos horários à esquerda de Greenwich!
				if (timestamp != null && (encontrado == null || timestamp.isBefore(encontrado))) {
					encontrado = timestamp;
				}
			}
		}
		return encontrado;
	}
	
	// Ex: "2012-12-07T17:34:05-0200"
	private static final Pattern pTimestamp1 = Pattern.compile("(\\d+)-(\\d+)-(\\d+)T(\\d+):(\\d+):(\\d+)([\\-0-9]+)00");
	// Ex: "2012-05-01T21:03:46.000000Z"
	private static final Pattern pTimestamp2 = Pattern.compile("(\\d+)-(\\d+)-(\\d+)T(\\d+):(\\d+):(\\d+).000000Z");
	// Ex: "2011-10-15 14:08:06"
	private static final Pattern pTimestamp3 = Pattern.compile("(\\d+)-(\\d+)-(\\d+) (\\d+):(\\d+):(\\d+)");
	// Ex: "date            : 2017"
	private static final Pattern pTimestamp4 = Pattern.compile("^(\\d{4})$");
	// Ex: "date            : 20171221"
	private static final Pattern pTimestamp5 = Pattern.compile("^(\\d{4})(\\d{2})(\\d{2})$");
	
	/**
	 * Converte um timestamp (string) para Date/time.
	 * 
	 * Quando for possível identificar o timezone pela string, faz a conversão automática para a timezone local.
	 * Quando NÃO for possível identificar o timezone pela string, faz a conversão somente se o parâmetro "ajustarTimeZone" estiver habilitado.
	 * 
	 * @param input : string que será lida
	 * @param ajustarTimeZone
	 * @return
	 */
	private static LocalDateTime converterTimestampParaLocalDateTime(String input, boolean ajustarTimeZone) {
		Matcher m;

		m = pTimestamp1.matcher(input);
		if (m.find()) {
			// Este é um timestamp que já possui a timezone correta identificada, então não
			// é preciso alterá-lo.
			return LocalDateTime.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), 
					Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)), Integer.parseInt(m.group(6))); 
		}

		m = pTimestamp2.matcher(input);
		if (m.find()) {
			LocalDateTime ldt = LocalDateTime.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), 
					Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)), Integer.parseInt(m.group(6)));
			
			if (ajustarTimeZone) {
				ldt = ajustarTimeZone(ldt);
			}
			return ldt;
		}

		m = pTimestamp3.matcher(input);
		if (m.find()) {
			LocalDateTime ldt = LocalDateTime.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), 
					Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)), Integer.parseInt(m.group(6)));
			
			if (ajustarTimeZone) {
				ldt = ajustarTimeZone(ldt);
			}
			return ldt;
		}

		m = pTimestamp4.matcher(input);
		if (m.find()) {
			// TODO: Implementar teste!!
			LOGGER.warn("Timestamp contem somente ano: " + input);
			return null;
		}

		m = pTimestamp5.matcher(input);
		if (m.find()) {
			return LocalDateTime.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), 0, 0, 0);
		}
		
		throw new RuntimeException("Timestamp nao identificado: '" + input + "'! Melhore o metodo Auxiliar.converteTimestampParaDate!");
	}

	private static LocalDateTime ajustarTimeZone(LocalDateTime ldt) {
		// Esse pattern está em GMT e precisa ser convertido para Timezone local
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of("GMT"));
		ldt = zdt.withZoneSameInstant(ZoneId.of("America/Sao_Paulo")).toLocalDateTime();
		return ldt;
	}

	public Dimension getVideoResolution() throws FFmpegException, IOException {
		
		for (String linha: getFullFileInfo()) {
			
			// Ex: Stream #0:1(und): Video: h264 (Main) (avc1 / 0x31637661), yuv420p(tv, smpte170m/bt709/bt709), 568x320, 758 kb/s, 30 fps, 30 tbr, 600 tbn, 1200 tbc (default)
			if (linha.contains("Stream") && linha.contains("Video")) {
				Pattern pResolucao = Pattern.compile("(\\d+)x(\\d+)");
				Matcher m = pResolucao.matcher(linha);
				while (m.find()) {
					int width = Integer.parseInt(m.group(1));
					int height = Integer.parseInt(m.group(2));
					if (width > 0 && height > 0) {
						Dimension d = new Dimension(width, height);
						return d;
					}
				}
			}
		}
		
		String erro = "Não foi possível identificar a resolução do vídeo no arquivo " + file + "!";
		throw new FFmpegException(erro, StringUtils.join(getFullFileInfo(), "\n"));
	}

	/**
	 * Retorna a duração de um vídeo, em segundos.
	 * 
	 * @return
	 * @throws FFmpegException
	 * @throws IOException
	 */
	public float getVideoDurationSeconds() throws FFmpegException, IOException {
	
		for (String linha : getFullFileInfo()) {
			Float videoDuration = getVideoDurationFromLine(linha);
			if (videoDuration != null) {
				return videoDuration;
			}
		}
		
		String erro = "Não foi possível identificar a duração do vídeo no arquivo " + file + "!";
		throw new FFmpegException(erro, StringUtils.join(getFullFileInfo(), "\n"));
	}
	
	public Float getVideoFPS() throws NumberFormatException, IOException {
		for (String linha: getFullFileInfo()) {
			
			// Ex: Stream #0:0(eng): Video: h264 (High 4:4:4 Predictive) (avc1 / 0x31637661), yuv444p, 1920x1080, 10399 kb/s, 29.97 fps, 29.97 tbr, 30k tbn, 59.94 tbc (default)
			if (linha.contains("Stream") && linha.contains("fps")) {
				Matcher m = PATTERN_FPS.matcher(linha);
				if (m.find()) {
					return Float.parseFloat(m.group(1));
				}
			}
		}
		return null;
	}
	
	public static Float getVideoDurationFromLine(String linha) {
		Matcher m = PATTERN_DURATION.matcher(linha);
		if (m.find()) {
			int horas = Integer.parseInt(m.group(1));
			int minutos = Integer.parseInt(m.group(2));
			int segundos = Integer.parseInt(m.group(3));
			float centesimos = Float.parseFloat(m.group(4));
			return (3600 * horas) + (60 * minutos) + segundos + (centesimos/100);
		}
		return null;
	}	
}
