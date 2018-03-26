package com.felipegiotto.utils.ffmpeg;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
	
	public static void setFFmpegPath(String ffmpegPath) {
		FFmpegPath = ffmpegPath;
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

	public ArrayList<String> buildParameters() {
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
		
		// Arquivo de entrada
		if (inputFile == null) {
			throw new InvalidParameterException("Faltou definir arquivo de entrada com setInputFile!");
		}
		commands.add("-i");
		commands.add(inputFile);
		
		// Tempos inicial e final
		if (tempoInicial != null) {
			commands.add("-ss");
			commands.add(tempoInicial);
		}
		if (tempoFinal != null) {
			commands.add("-to");
			commands.add(tempoFinal);
		}
		
		// Codec de vídeo
		if (TipoAudioVideo.ENCODE.equals(video)) {
			if (videoEncoderCodec == null) {
				throw new InvalidParameterException("Faltou definir codec de vídeo com 'setVideoEncoderCodec', pois foi utilizado setVideo(TipoVideo.ENCODE)");
			}
			commands.add("-c:v");
			commands.add(videoEncoderCodec);
			
		} else if (TipoAudioVideo.COPY.equals(video)) {
			commands.add("-c:v");
			commands.add("copy");
//		} else {
//			throw new NotImplementedException("Ainda não foi implementado: video=" + video);
		}
		
		// Parametros extras para processar o video
		if (videoExtraParameters != null) {
			commands.addAll(videoExtraParameters);
		}
		
		// Redimensionando vídeo
		if (videoWidth != null && videoHeight != null) {
			addVideoFilter("scale=w=" + videoWidth + ":h=" + videoHeight);
		} else if (videoWidth != null && videoHeight == null) {
			addVideoFilter("scale=" + videoWidth + ":-1");
		} else if (videoWidth == null && videoHeight != null) {
			addVideoFilter("scale=-1:" + videoHeight);
		}
		
		// Adiciona os filtros (ex: scale), separados por vírgula
		if (videoFilters != null) {
			commands.add("-vf");
			commands.add(StringUtils.join(videoFilters, ","));
		}
		
		// Rotação do vídeo
		if (videoRotation != null) {
			commands.add("-metadata:s:v:0");
			commands.add("rotate=" + videoRotation);
		}
		
		// Copiar metadados para destino
		if (videoCopiarMetadados) {
			commands.add("-map_metadata");
			commands.add("0");
		}
		
		// Codec de áudio
		if (TipoAudioVideo.ENCODE.equals(audio)) {
			if (audioEncoderCodec == null) {
				throw new InvalidParameterException("Faltou definir codec de áudio com 'setAudioEncoderCodec', pois foi utilizado setAudio(TipoAudioVideo.ENCODE)");
			}
			commands.add("-c:a");
			commands.add(audioEncoderCodec);
			
			// Parametros extras para processar o áudio (somente se estiver utilizando ENCODE)
			if (audioExtraParameters != null) {
				commands.addAll(audioExtraParameters);
			}
			
		} else if (TipoAudioVideo.COPY.equals(audio)) {
			commands.add("-c:a");
			commands.add("copy");
//		} else {
//			throw new NotImplementedException("Ainda não foi implementado: audio=" + audio);
		}
		
		// Mover metadados do áudio para o início
		if (audioMoverMetadadosParaInicio) {
			commands.add("-movflags");
			commands.add("+faststart");
		}
		
		// Arquivo de saída
		if (outputFile == null) {
			throw new InvalidParameterException("Faltou definir arquivo de saída com setOutputFile!");
		}
		commands.add(outputFile);
		
		return commands;
	}

	private String inputFile;
	
	/**
	 * Configura o arquivo de entrada, que será processado pelo ffmpeg.
	 * 
	 * Este parâmetro é obrigatório antes de executar o ffmpeg!
	 * 
	 * @param inputFile
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile.getAbsolutePath();
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
	
	private String tempoInicial;
	private String tempoFinal;
	
	/**
	 * Configura o tempo inicial do vídeo de entrada que será processado.
	 * 
	 * Deve ser informado no formato HH:MM:SS. 
	 * 
	 * Por exemplo, ao chamar setTempoInicial("00:00:10"), o arquivo de saída conterá 
	 * a partir dos 10 segundos do arquivo de entrada.
	 * 
	 * Este método pode ser combinado com setTempoFinal. Por exemplo, para processar
	 * somente o intervalo entre 10s e 40s do arquivo de entrada, utilizar:
	 * setTempoInicial("00:00:10");
	 * setTempoFinal("00:00:40");
	 * 
	 * @param tempoInicial
	 */
	public void setTempoInicial(String tempoInicial) {
		this.tempoInicial = tempoInicial;
	}
	
	/**
	 * Configura o tempo final do vídeo de entrada que será processado.
	 * 
	 * Deve ser informado no formato HH:MM:SS. 
	 * 
	 * Por exemplo, ao chamar setTempoFinal("00:01:00"), o arquivo de saída conterá 
	 * somente o primeiro minuto do arquivo de entrada.
	 * 
	 * Este método pode ser combinado com setTempoInicial. Por exemplo, para processar
	 * somente o intervalo entre 10s e 40s do arquivo de entrada, utilizar:
	 * setTempoInicial("00:00:10");
	 * setTempoFinal("00:00:40");
	 * 
	 * @param tempoInicial
	 */
	public void setTempoFinal(String tempoFinal) {
		this.tempoFinal = tempoFinal;
	}

	private TipoAudioVideo video;
	private TipoAudioVideo audio;
	
	/**
	 * Define a forma de processamento do vídeo, que pode ser:
	 * ENCODE: Reprocessa o vídeo conforme codec definido em "setVideoEncoderCodec"
	 * COPY: Copia o vídeo do arquivo original, sem reprocessá-lo
	 * NONE: Não grava vídeo no arquivo de saída.
	 * 
	 * @param video
	 */
	public void setVideo(TipoAudioVideo video) {
		this.video = video;
	}

	/**
	 * Define a forma de processamento do vídeo, que pode ser:
	 * ENCODE: Reprocessa o vídeo conforme codec definido em "setVideoEncoderCodec"
	 * COPY: Copia o vídeo do arquivo original, sem reprocessá-lo
	 * NONE: Não grava vídeo no arquivo de saída.
	 * 
	 * @param video
	 */
	public void setAudio(TipoAudioVideo audio) {
		this.audio = audio;
	}

	private String videoEncoderCodec;
	private String audioEncoderCodec;
	
	/**
	 * Define o codec de processamento do vídeo, quando for 
	 * utilizado setVideo(TipoAudioVideo.ENCODE)
	 * 
	 * Para consultar todos os encoders disponíveis, utilizar o comando
	 * "ffmpeg -encoders" e analisar os que possuem "V" (video) no início da linha
	 * 
	 * @param videoEncoderCodec
	 */
	public void setVideoEncoderCodec(String videoEncoderCodec) {
		this.videoEncoderCodec = videoEncoderCodec;
	}
	
	/**
	 * Define o ganho de luminosidade ao compactar o vídeo.
	 * A luminosidade padrão é "1" (ou manter como NULL para que essa
	 * informação nem seja passada ao FFMPEG). 
	 * 
	 * Valores menores (ex: 0.8) deixam o vídeo mais escuro. 
	 * Valores maiores (ex: 1.5) deixam o vídeo mais claro. 
	 * 
	 * Fonte: https://forum.videohelp.com/threads/367595-%5BSOLVED%5D-%5Bffmpeg%5D-Brightening-a-dark-video
	 * 
	 * @param ganhoLuminosidade
	 */
	public void setGanhoLuminosidade(Double ganhoLuminosidade) {
		if (ganhoLuminosidade != null) {
			addVideoFilter("lutyuv=y=val*" + ganhoLuminosidade);
		}
	}
	
	List<String> videoFilters;
	
	/**
	 * Adiciona um filtro de vídeo a ser passado ao ffmpeg.
	 * 
	 * Filtros (-vf) são úteis para ajustar o tamanho (scale), a luminosidade (lutyuv), etc.
	 * 
	 * Vários filtros podem ser passados ao ffmpeg, separados por vírgula
	 * (fonte: https://stackoverflow.com/questions/6195872/applying-multiple-filters-at-once-with-ffmpeg)
	 *
	 * @param videoFilter
	 */
	public void addVideoFilter(String videoFilter) {
		if (videoFilters == null) {
			videoFilters = new ArrayList<>();
		}
		videoFilters.add(videoFilter);
	}

	/**
	 * Define o codec de processamento do áudio, quando for 
	 * utilizado setAudio(TipoAudioVideo.ENCODE)
	 * 
	 * Para consultar todos os encoders disponíveis, utilizar o comando
	 * "ffmpeg -encoders" e analisar os que possuem "A" (audio) no início da linha
	 * 
	 * @param audioEncoderCodec
	 */
	public void setAudioEncoderCodec(String audioEncoderCodec) {
		this.audioEncoderCodec = audioEncoderCodec;
	}
	
	List<String> videoExtraParameters;
	List<String> audioExtraParameters;
	
	/**
	 * Adiciona parâmetros extras a serem passados para o codec de vídeo, ex:
	 * setVideoAddExtraParameters("-preset", "slow");
	 * 
	 * Este método pode ser chamado diversas vezes para adicionar diversos parâmetros
	 * 
	 * @param parameters
	 */
	public void setVideoAddExtraParameters(String... parameters) {
		if (videoExtraParameters == null) {
			videoExtraParameters = new ArrayList<>();
		}
		for (String parameter: parameters) {
			videoExtraParameters.add(parameter);
		}
	}
	
	/**
	 * Adiciona parâmetros extras a serem passados para o codec de áudio, ex:
	 * setAudioAddExtraParameters("-b:a", "128k");
	 * 
	 * Este método pode ser chamado diversas vezes para adicionar diversos parâmetros
	 * 
	 * @param parameters
	 */
	public void setAudioAddExtraParameters(String... parameters) {
		if (audioExtraParameters == null) {
			audioExtraParameters = new ArrayList<>();
		}
		for (String parameter: parameters) {
			audioExtraParameters.add(parameter);
		}
	}
	
	private boolean videoCopiarMetadados = true;
	
	/**
	 * Indica se os metadados do vídeo de entrada (incluindo data de gravação, 
	 * coordenadas GPS, etc) deverão ser migrados para o arquivo de saída.
	 * 
	 * O recomendado é utilizar valor "true".
	 * 
	 * @param videoCopiarMetadados
	 */
	public void setVideoCopiarMetadados(boolean videoCopiarMetadados) {
		this.videoCopiarMetadados = videoCopiarMetadados;
	}
	
	private boolean audioMoverMetadadosParaInicio = true;
	
	/**
	 * Indica se os metadados do áudio devem ser gravados no início do arquivo.
	 * 
	 * Isso acelera a reprodução
	 * 
	 * @param moverMetadadosParaInicio
	 */
	public void setAudioMoverMetadadosParaInicio(boolean moverMetadadosParaInicio) {
		this.audioMoverMetadadosParaInicio = moverMetadadosParaInicio;
	}


	/**
	 * Configura todos os parâmetros para o padrão de vídeo e áudio utilizado por Felipe
	 * para compactar os vídeos dos celulares e da câmera fotográfica.
	 * 
	 * Este padrão foi estudado conforme testes descritos no arquivo "pesquisa_codec_video.xml",
	 * de modo que funcione em diversos dispositivos (mac, celular, whatsapp, google drive, etc).
	 */
	public void configurarPadraoCamerasFelipe(boolean presetSlow) {
		
		// 15 = Prioridade baixa, para não prejudicar outras atividades do PC
		setProcessNicePriority(15);
		
		// Fonte: https://trac.ffmpeg.org/wiki/Encode/H.265
		// Fonte (OLD): https://trac.ffmpeg.org/wiki/Encode/H.264
		setVideo(TipoAudioVideo.ENCODE);
		setVideoEncoderCodec("libx264");
		
		// Preset (qualidade x velocidade)
		if (presetSlow) {
			setVideoAddExtraParameters("-preset", "slow");     // Velocidade (em video de exemplo): 0.130x (padrao)
//			setVideoAddExtraParameters("-preset", "slower");   // Velocidade (em video de exemplo): 0.080x
//			setVideoAddExtraParameters("-preset", "veryslow"); // Velocidade (em video de exemplo): 0.051x
		}

		// Qualidade:
		// The range of the quantizer scale is 0-51: where 0 is lossless, 23 is default, 
		// and 51 is worst possible. A lower value is a higher quality and a subjectively 
		// sane range is 18-28. Consider 18 to be visually lossless or nearly so: it should 
		// look the same or nearly the same as the input but it isn't technically lossless.
		setVideoAddExtraParameters("-crf", "24"); // (padrao)
//		setVideoAddExtraParameters("-crf", "28"); // Qualidade um pouco menor, arquivo um pouco menor

		// Perfil do H.264.
		// Os vídeos do Youtube utilizam o profile "main", que é compatível com mais dispositivos. 
		// Se não especificar o profile, o ffmpeg utiliza "high".
		// Profile constrains H.264 to a subset of features - higher profiles require more 
		// CPU power to decode and are able to generate better looking videos at same bitrate. 
		// You should always choose the best profile your target devices support.
		// High: Desktop browsers, iPhone 4S+, iPad 2+, Android 4.x+ tablets, Xbox 360, Playstation 3
		// Main: iPhone 3GS, iPhone 4, iPad, low-end Android phones
		// Baseline: iPhone, iPhone 3G, old low-end Android devices, other embedded players
		// Fonte: https://www.virag.si/2012/01/web-video-encoding-tutorial-with-ffmpeg-0-9/
//		setVideoAddExtraParameters("-profile:v", "main");

		// Adiciona metadados do vídeo original.
		// Se esse parâmetro não existir, alguns metadados não são migrados para o vídeo 
		// compactado, ex: "creation_time". Esses metadados, são importantes, por exemplo,
		// para colocar o prefixo no nome do arquivo com o instante em que o vídeo foi gravado.
		// A lista dos metadados de um arquivo pode ser consultada com "ffmpeg -i <arquivo>"
		// Fonte: http://superuser.com/questions/510578/when-spliting-mp4s-with-ffmpeg-how-do-i-include-metadata
		setVideoCopiarMetadados(true);

		// Codec de audio
		// Fonte: https://trac.ffmpeg.org/wiki/Encode/AAC
		setAudio(TipoAudioVideo.ENCODE);
		setAudioEncoderCodec("aac");
		
		// Qualidade do audio
		setAudioAddExtraParameters("-b:a", "128k");
		
		// Move metadados do áudio para o início do arquivo, para acelerar o início da reprodução
		setAudioMoverMetadadosParaInicio(true);
	}

	/**
	 * Configura todos os parâmetros para o padrão de vídeo e áudio utilizado pela 
	 * Central Multimidia Pioneer AVH-288BT.
	 */
	public void configurarPadraoCentralMultimidia(boolean reduzirResolucaoParaCaberNaCentral) {
		
		// 15 = Prioridade baixa, para não prejudicar outras atividades do PC
		setProcessNicePriority(15);
		
		setVideo(TipoAudioVideo.ENCODE);
		setVideoEncoderCodec("libxvid");

		// Preset (qualidade x velocidade)
		//setVideoAddExtraParameters("-preset", "slow");     // Velocidade (em video de exemplo): 0.130x (padrao)

		// Qualidade
		setVideoAddExtraParameters("-qscale:v", "10");

		// Redimensionar para caber na tela da Central AVH-288BT
		// Fonte: https://trac.ffmpeg.org/wiki/Scaling%20(resizing)%20with%20ffmpeg
		// Manual: http://pioneer.com.br/media/57028ce4c6c7f_0f800c39e4ad83be08fa4de9e5832bc5.pdf
		// * Resolucao Maxima: 720px x 480/576px
		// * Taxa de quadros maxima: 30fps
		if (reduzirResolucaoParaCaberNaCentral) {
			
			try {
				setVideoMaximumResolution(720, 480, 16);				
			} catch (IOException | NullPointerException | InterruptedException | FFmpegException e) {
				throw new RuntimeException("Não foi possível analisar resolução do vídeo: " + e.getLocalizedMessage(), e);
			}
		}

		// Codec de audio
		setAudio(TipoAudioVideo.ENCODE);
		setAudioEncoderCodec("libmp3lame");

		// Qualidade do audio
		setAudioAddExtraParameters("-qscale:a", "5");
	}

	Integer videoRotation;
	
	/**
	 * Define a rotação do vídeo, em graus. Ex:
	 * 90=Tombar para direita
	 * 180=Cabeca para baixo
	 * 270=Tombar para esquerda
	 * null=Não rotacionar
	 * 
	 * OBS: A rotação será definida nos metadados, e não no encode do arquivo.
	 *      Por isso, a rotação pode ser utilizada com COPY do stream de vídeo.
	 * @param videoRotation
	 */
	public void setVideoRotation(Integer videoRotation) {
		this.videoRotation = videoRotation;
	}
	
	public void setVideoRotationTombarParaDireita() {
		setVideoRotation(90);
	}
	
	public void setVideoRotationCabecaParaBaixo() {
		setVideoRotation(180);
	}
	
	public void setVideoRotationTombarParaEsquerda() {
		setVideoRotation(270);
	}
	
	private Integer videoWidth;
	private Integer videoHeight;
	
	/**
	 * Define resolução horizontal e vertical do vídeo.
	 * 
	 * Se algum dos parâmetros for NULL, será utilizado um valor que mantenha a proporção do vídeo de saída.
	 * 
	 * @param width
	 * @param height
	 */
	public void setVideoResolution(Integer width, Integer height) {
		this.videoWidth = width;
		this.videoHeight = height;
	}

	/**
	 * Define a resolução MÁXIMA do vídeo, de forma que ele caiba dentro da faixa
	 * informada, mantendo a sua proporção original.
	 * 
	 * O parâmetro "multiplos" permite, ainda, diminuir cada uma das resoluções (width
	 * e height) para múltiplos de determinado número (ex: 16), para evitar problemas de 
	 * números quebrados que o ffmpeg não consiga compactar.
	 * 
	 * IMPORTANTE: o arquivo de entrada deve ter sido definido, pois a resolução do
	 * arquivo original precisará ser lida para calcular a resolução de saída.
	 * 
	 * @param maxWidth
	 * @param maxHeight
	 * @param multiplo
	 * @throws FFmpegException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void setVideoMaximumResolution(Integer maxWidth, Integer maxHeight, Integer multiplo) throws IOException, InterruptedException, FFmpegException {
		
		// Pega a resolução original do vídeo
		Point resolucao = getResolucaoVideo(new File(inputFile));
		
		// Calcula a relação ideal para restringir a resolução ao limite da tela da central
		double relacaoWidth = resolucao.getX() / maxWidth;
		double relacaoHeight = resolucao.getY() / maxHeight;
		double relacaoMaior = Math.max(relacaoWidth, relacaoHeight);
		
		// Verifica se será necessário redimensionar o vídeo (pode ser que ele já seja menor do que o limite da tela - 720x480)
		if (relacaoMaior > 1) {
			
			// Calcula a nova largura e altura, conforme a relação ideal calculada.
			int width = (int) (resolucao.getX() / relacaoMaior);
			int height = (int) (resolucao.getY() / relacaoMaior);
			
			// Limita a resolução de saída a múltiplos de 16 (parece que dá menos problemas. Seria o tamanho de cada "quadrado" utilizado na compactação do vídeo?)
			if (multiplo != null) {
				width = (width / multiplo) * multiplo;
				height = (height / multiplo) * multiplo;
			}
			LOGGER.debug("Resolucoes: entrada=" + resolucao.getX() + "x" + resolucao.getY() + ", saída=" + width + "x" + height);
			setVideoResolution(width, height);
		}
	}
	
	public static List<String> getFileInfo(String path) throws IOException {
		List<String> comandos = new ArrayList<>();
		
		comandos.add(FFmpegPath);
		comandos.add("-i");
		comandos.add(path);
		
		ProcessBuilder pb = new ProcessBuilder(comandos);
		Process p = pb.start();
		
		List<String> saida = new ArrayList<>();
		
		try (Scanner scanner = new Scanner(p.getErrorStream())) {
			while (scanner.hasNextLine()) {
				saida.add(scanner.nextLine());
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
		
		return saida;
	}

	public static Point getResolucaoVideo(File input) throws IOException, InterruptedException, FFmpegException {
		ArrayList<String> commands = new ArrayList<>();
		
		commands.add(FFmpegPath);
		commands.add("-i");
		commands.add(input.getAbsolutePath());
		
		Process proc = FGProcessUtils.executarComando(commands);
		try {
			FGStreamUtils.consomeStream(proc.getInputStream(), "");
			String out = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
			Pattern pResolucao = Pattern.compile("(\\d+)x(\\d+)");
			Matcher m = pResolucao.matcher(out);
			while (m.find()) {
				int width = Integer.parseInt(m.group(1));
				int height = Integer.parseInt(m.group(2));
				if (width > 0 && height > 0) {
					Point p = new Point(width, height);
					return p;
				}
			}
			
			String erro = "Não foi possível identificar a resolução do vídeo no arquivo " + input + "!";
			throw new FFmpegException(erro, out);
			
		} finally {
			proc.waitFor();
		}
	}
	
	public static int getDuracaoSegundosVideo(File input) throws IOException, InterruptedException, FFmpegException {
		ArrayList<String> commands = new ArrayList<>();
		
		commands.add(FFmpegPath);
		commands.add("-i");
		commands.add(input.getAbsolutePath());
		
		Process proc = FGProcessUtils.executarComando(commands);
		try {
			FGStreamUtils.consomeStream(proc.getInputStream(), "");
			String out = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
			Pattern pResolucao = Pattern.compile("Duration: (\\d+):(\\d+):(\\d+)\\.");
			Matcher m = pResolucao.matcher(out);
			while (m.find()) {
				int horas = Integer.parseInt(m.group(1));
				int minutos = Integer.parseInt(m.group(2));
				int segundos = Integer.parseInt(m.group(3));
				return (3600 * horas) + (60 * minutos) + segundos; 
			}
			
			String erro = "Não foi possível identificar a duração do vídeo no arquivo " + input + "!";
			throw new FFmpegException(erro, out);
		} finally {
			proc.waitFor();
		}
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
	
	public static void main(String[] args) throws Exception {
		setFFmpegPath("/Users/taeta/workspace/backup_fotos_e_macbook_ultimate/ffmpeg/ffmpeg-90148-g0419623cdc");
		FFmpegCommand f = new FFmpegCommand();
		f.setInputFile(new File("/Users/taeta/Desktop/Lixo/IMG_5706_compact_.mov"));
		f.setVideo(TipoAudioVideo.COPY);
		f.setOutputFile(new File("/Users/taeta/Desktop/Lixo/IMG_5706_compact_teste.MOV"));
		f.runAndWait(true);
	}
}
