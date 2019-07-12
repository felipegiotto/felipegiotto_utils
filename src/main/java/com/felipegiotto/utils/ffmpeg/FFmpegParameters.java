package com.felipegiotto.utils.ffmpeg;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.felipegiotto.utils.ffmpeg.util.FFmpegException;

public class FFmpegParameters {
	
	private static final Logger LOGGER = LogManager.getLogger(FFmpegParameters.class);
	
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
	
	public String getTempoInicial() {
		return tempoInicial;
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

	public String getTempoFinal() {
		return tempoFinal;
	}
	
	public static final String ENCODER_COPY = "copy";
	private String videoEncoderCodec = null;
	private String audioEncoderCodec = null;
	
	/**
	 * Define o codec de processamento do vídeo.
	 * 
	 * Para somente copiar o "stream" de vídeo, sem reprocessar, utilizar "setVideoEncoderCopy".
	 * 
	 * Para consultar todos os encoders disponíveis, utilizar o comando
	 * "ffmpeg -encoders" e analisar os que possuem "V" (video) no início da linha
	 * 
	 * @param videoEncoderCodec
	 */
	public void setVideoEncoderCodec(String videoEncoderCodec) {
		this.videoEncoderCodec = videoEncoderCodec;
	}
	
	public String getVideoEncoderCodec() {
		return videoEncoderCodec;
	}
	
	/**
	 * Define que o vídeo será simplesmente copiado do arquivo de entrada para o de saída.
	 * 
	 * Para reprocessar o vídeo utilizando algum codec, utilizar "setVideoEncoderCodec"
	 */
	public void setVideoEncoderCopy() {
		setVideoEncoderCodec(ENCODER_COPY);
	}
	
	private String videoPreset;
	
	/**
	 * Define o "preset" da codificação do vídeo (relação qualidade/tamanho/velocidade).
	 * 
	 * Exemplo: "medium", "slow", "slower".
	 * 
	 * Consulte a documentação do codec de vídeo (ex: H265) para informações sobre os valores possíveis:
	 * https://trac.ffmpeg.org/wiki/Encode/H.265
	 * 
	 * @param videoPreset
	 */
	public void setVideoPreset(String videoPreset) {
		this.videoPreset = videoPreset;
	};
	
	public String getVideoPreset() {
		return videoPreset;
	}
	
	/**
	 * Define o codec de processamento do áudio.
	 * 
	 * Para somente copiar o "stream" de áudio, sem reprocessar, utilizar "setAudioEncoderCodecCopy".
	 * 
	 * Para consultar todos os encoders disponíveis, utilizar o comando
	 * "ffmpeg -encoders" e analisar os que possuem "A" (audio) no início da linha
	 * 
	 * @param audioEncoderCodec
	 */
	public void setAudioEncoderCodec(String audioEncoderCodec) {
		this.audioEncoderCodec = audioEncoderCodec;
	}
	
	public String getAudioEncoderCodec() {
		return audioEncoderCodec;
	}
	
	/**
	 * Define que o áudio será simplesmente copiado do arquivo de entrada para o de saída.
	 * 
	 * Para reprocessar o áudio utilizando algum codec, utilizar "setAudioEncoderCodec"
	 */
	public void setAudioEncoderCopy() {
		setAudioEncoderCodec(ENCODER_COPY);
	}
	
	private boolean luminosidadeMaisClara;
	//private Double ganhoLuminosidade;
	
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
//	public void setGanhoLuminosidade(Double ganhoLuminosidade) {
//		this.ganhoLuminosidade = ganhoLuminosidade;
//	}
	public void setLuminosidadeMaisClara(boolean luminosidadeMaisClara) {
		this.luminosidadeMaisClara = luminosidadeMaisClara;
	}
	
	public boolean isLuminosidadeMaisClara() {
		return luminosidadeMaisClara;
	}
	
	ArrayList<String> videoFilters = new ArrayList<>();
	
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
		videoFilters.add(videoFilter);
	}
	
	public ArrayList<String> getVideoFilters() {
		return videoFilters;
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
	
	public boolean isVideoCopiarMetadados() {
		return videoCopiarMetadados;
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
	public void configurarPadraoCamerasFelipe(boolean utilizarH265, boolean presetSlow) {
		
		// 15 = Prioridade baixa, para não prejudicar outras atividades do PC
		// TODO: Descomentar
//		setProcessNicePriority(15);
		
		if (utilizarH265) {
			configurarVideoH265(presetSlow);
		} else {
			configurarVideoH264(presetSlow);
		}

		// Adiciona metadados do vídeo original.
		// Se esse parâmetro não existir, alguns metadados não são migrados para o vídeo 
		// compactado, ex: "creation_time". Esses metadados, são importantes, por exemplo,
		// para colocar o prefixo no nome do arquivo com o instante em que o vídeo foi gravado.
		// A lista dos metadados de um arquivo pode ser consultada com "ffmpeg -i <arquivo>"
		// Fonte: http://superuser.com/questions/510578/when-spliting-mp4s-with-ffmpeg-how-do-i-include-metadata
		setVideoCopiarMetadados(true);

		// Codec de audio
		// Fonte: https://trac.ffmpeg.org/wiki/Encode/AAC
		setAudioEncoderCodec("aac");
		
		// Qualidade do audio
		setAudioQualidade("128k");
		
		// Move metadados do áudio para o início do arquivo, para acelerar o início da reprodução
		setAudioMoverMetadadosParaInicio(true);
	}

	private Integer videoQualidadeCrfPadrao;
	private Integer videoQualidadeCrf;
	
	public void setVideoQualidadeCrfPadrao(Integer qualidadeCrfPadrao) {
		this.videoQualidadeCrfPadrao = qualidadeCrfPadrao;
	}
	
	public void setVideoQualidadeCrf(Integer qualidadeCrf) {
		this.videoQualidadeCrf = qualidadeCrf;
	}
	
	/**
	 * Se usuário especificou uma qualidade CRF, retorna ela. Senão:
	 * Se existe uma qualidade parão, retorna ela. Senão:
	 * Retorna null
	 */
	public Integer getVideoQualidadeCrfOuPadrao() {
		if (videoQualidadeCrf != null) {
			return videoQualidadeCrf;
		}
		return videoQualidadeCrfPadrao;
	}

	private String audioQualidade;
	public void setAudioQualidade(String qualidade) {
		this.audioQualidade = qualidade;
	}
	
	public String getAudioQualidade() {
		return audioQualidade;
	}
	
	/**
	 * Configurações para utilizar codec H264
	 * 
	 * @param presetSlow indica se o preset "slow" deve ser utilizado
	 * @deprecated utilizar preferencialmente {@link #configurarVideoH265}
	 */
	private void configurarVideoH264(boolean presetSlow) {
		
		// Fonte: https://trac.ffmpeg.org/wiki/Encode/H.264
		setVideoEncoderCodec("libx264");
		
		// Preset (qualidade x velocidade)
		if (presetSlow) {
			setVideoPreset("slow");
//			setVideoAddExtraParameters("-preset", "slow");     // Velocidade (em video de exemplo): 0.130x (padrao)
//			setVideoAddExtraParameters("-preset", "slower");   // Velocidade (em video de exemplo): 0.080x
//			setVideoAddExtraParameters("-preset", "veryslow"); // Velocidade (em video de exemplo): 0.051x
		} else {
			setVideoPreset(null);
		}

		// Qualidade:
		// The range of the quantizer scale is 0-51: where 0 is lossless, 23 is default, 
		// and 51 is worst possible. A lower value is a higher quality and a subjectively 
		// sane range is 18-28. Consider 18 to be visually lossless or nearly so: it should 
		// look the same or nearly the same as the input but it isn't technically lossless.
		setVideoQualidadeCrfPadrao(24);
//		setVideoAddExtraParameters("-crf", "24"); // (padrao)
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
	}

	/**
	 * Configurações para utilizar codec H265
	 * 
	 * @param presetSlow
	 */
	private void configurarVideoH265(boolean presetSlow) {
		
		// Fonte: https://trac.ffmpeg.org/wiki/Encode/H.265
		setVideoEncoderCodec("libx265");

		// Preset (qualidade x velocidade)
		if (presetSlow) {
			setVideoPreset("slow");
//			setVideoAddExtraParameters("-preset", "slow");
//			setVideoAddExtraParameters("-preset", "slower");
//			setVideoAddExtraParameters("-preset", "veryslow");
		} else {
			setVideoPreset(null);
		}

		// Qualidade:
		// Choose a CRF. The default is 28, and it should visually correspond to libx264
		// video at CRF 23, but result in about half the file size. Other than that, 
		// CRF works just like in x264.
		// setVideoAddExtraParameters("-crf", "28"); // (padrao)
		setVideoQualidadeCrfPadrao(28); // Padrao

		// Workaround para que miniaturas e preview do Finder funcionem corretamente no MacOS X
		// Fonte: https://discussions.apple.com/thread/8091782
		setVideoAddExtraParameters("-tag:v", "hvc1");
	}
	
	/**
	 * Configura todos os parâmetros para o padrão de vídeo e áudio utilizado pela 
	 * Central Multimidia Pioneer AVH-288BT.
	 * 
	 * @param arquivoVideoParaReduzirResolucao : se informado, será a base para definir
	 * a resolução do vídeo de saída para que caiba corretamente na resolução da central.
	 */
	public void configurarPadraoCentralMultimidia(File arquivoVideoParaReduzirResolucao) {
		
		// 15 = Prioridade baixa, para não prejudicar outras atividades do PC
		// TODO: Descomentar
//		setProcessNicePriority(15);
		
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
		if (arquivoVideoParaReduzirResolucao != null) {
			
			try {
				setVideoMaxResolutionFromFile(720, 480, 16, arquivoVideoParaReduzirResolucao);				
			} catch (IOException | NullPointerException | FFmpegException e) {
				throw new RuntimeException("Não foi possível analisar resolução do vídeo: " + e.getLocalizedMessage(), e);
			}
		}

		// Codec de audio
		setAudioEncoderCodec("libmp3lame");

		// Qualidade do audio
		setAudioAddExtraParameters("-qscale:a", "5");
	}

	Integer videoRotation;
	boolean videoRotationHorizontalFlip = false;
	boolean videoRotationVerticalFlip = false;
	
	/**
	 * Define a rotação do vídeo, em graus. Ex:
	 * 90=Tombar para esquerda
	 * 180=Cabeca para baixo
	 * 270=Tombar para direita
	 * null=Não rotacionar
	 * 
	 * OBS: A rotação será definida nos metadados, e não no encode do arquivo.
	 *      Por isso, a rotação pode ser utilizada com COPY do stream de vídeo.
	 * @param videoRotation
	 */
	public void setVideoRotation(Integer videoRotation) {
		this.videoRotation = videoRotation;
		videoRotationHorizontalFlip = false;
		videoRotationVerticalFlip = false;
	}
	
	public Integer getVideoRotation() {
		return videoRotation;
	}
	
	public void setVideoRotationTombarParaDireita() {
		setVideoRotation(270);
	}
	
	public void setVideoRotationCabecaParaBaixo() {
		setVideoRotation(180);
	}
	
	public void setVideoRotationTombarParaEsquerda() {
		setVideoRotation(90);
	}
	
	/**
	 * Indica se o vídeo deve ser espelhado horizontalmente
	 * 
	 * @param videoRotationHorizontalFlip
	 */
	public void setVideoRotationHorizontalFlip(boolean videoRotationHorizontalFlip) {
		this.videoRotationHorizontalFlip = videoRotationHorizontalFlip;
	}
	
	public boolean isVideoRotationHorizontalFlip() {
		return videoRotationHorizontalFlip;
	}
	
	/**
	 * Indica se o vídeo deve ser espelhado verticalmente
	 * 
	 * @param videoRotationHorizontalFlip
	 */
	public void setVideoRotationVerticalFlip(boolean videoRotationVerticalFlip) {
		this.videoRotationVerticalFlip = videoRotationVerticalFlip;
	}
	
	public boolean isVideoRotationVerticalFlip() {
		return videoRotationVerticalFlip;
	}
	
	private Integer videoResolutionWidth;
	private Integer videoResolutionHeight;
	
	/**
	 * Define resolução horizontal e vertical do vídeo.
	 * 
	 * Se algum dos parâmetros for NULL, será utilizado um valor que mantenha a proporção do vídeo de saída.
	 * 
	 * @param width
	 * @param height
	 */
	public void setVideoResolution(Integer width, Integer height) {
		this.videoResolutionWidth = width;
		this.videoResolutionHeight = height;
	}
	
	public Integer getVideoResolutionWidth() {
		return videoResolutionWidth;
	}
	
	public Integer getVideoResolutionHeight() {
		return videoResolutionHeight;
	}
	
	private boolean ocultarInformacoesVersoesBibliotecas;
	public void setOcultarInformacoesVersoesBibliotecas(boolean ocultar) {
		this.ocultarInformacoesVersoesBibliotecas = ocultar;
	}
	
	public boolean isOcultarInformacoesVersoesBibliotecas() {
		return ocultarInformacoesVersoesBibliotecas;
	}
	
	/**
	 * Define a resolução MÁXIMA do vídeo, de forma que ele caiba dentro da faixa
	 * informada, mantendo a sua proporção original.
	 * 
	 * O parâmetro "multiple" permite, ainda, diminuir cada uma das resoluções (width
	 * e height) para múltiplos de determinado número (ex: 16), para evitar problemas de 
	 * números quebrados que o ffmpeg não consiga compactar.
	 * 
	 * Lê a resolução de um vídeo de entrada (para considerar como referência) e, a partir
	 * das regras descritas, calcula a resolução do vídeo de saída.
	 * 
	 * @param maxWidth : largura máxima que o vídeo pode ter
	 * @param maxHeight : altura máxima que o vídeo pode ter
	 * @param multiple : OPCIONAL: define que a largura e a altura do vídeo deverão ser múltiplos deste parâmetro (ex: 16)
	 * @param fileInfo : local de onde a resolução original do vídeo será lida
	 * @throws FFmpegException
	 * @throws IOException
	 */
	public void setVideoMaxResolutionFromFile(int maxWidth, int maxHeight, Integer multiple, FFmpegFileInfo fileInfo) throws FFmpegException, IOException {
		
		// Pega a resolução original do vídeo
		Point resolucao = fileInfo.getVideoResolution();
		
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
			if (multiple != null) {
				width = (width / multiple) * multiple;
				height = (height / multiple) * multiple;
			}
			LOGGER.debug("Resolucoes: entrada=" + resolucao.getX() + "x" + resolucao.getY() + ", saída=" + width + "x" + height);
			setVideoResolution(width, height);
		}
	}
	
	public void setVideoMaxResolutionFromFile(int width, int height, Integer multiple, File file) throws FFmpegException, IOException {
		FFmpegFileInfo fileInfo = new FFmpegFileInfo(file);
		setVideoMaxResolutionFromFile(width, height, multiple, fileInfo);
	}
	
	public ArrayList<String> buildParameters() throws IOException {
		ArrayList<String> commands = new ArrayList<>();
		
		if (ocultarInformacoesVersoesBibliotecas) {
			commands.add("-hide_banner");
		}
		
		// Tempos inicial e final
		// TODO: implementar múltiplas janelas de tempo, aqui e na rotina de compactação de vídeos
		if (tempoInicial != null) {
			commands.add("-ss");
			commands.add(tempoInicial);
		}
		if (tempoFinal != null) {
			commands.add("-to");
			commands.add(tempoFinal);
		}
		
		// Codec de vídeo, que poderá ser "copy" ou o nome do encoder.
		if (videoEncoderCodec != null) {
			commands.add("-c:v");
			commands.add(videoEncoderCodec);
		}
		
		// Qualidade CRF
		if (getVideoQualidadeCrfOuPadrao() != null) {
			commands.add("-crf");
			commands.add(getVideoQualidadeCrfOuPadrao().toString());
		}
		
		// Parametros extras para processar o video
		if (videoPreset != null) {
			commands.add("-preset");
			commands.add(videoPreset);
		}
		if (videoExtraParameters != null) {
			commands.addAll(videoExtraParameters);
		}
		
		// Cria uma nova lista para armazenar os filtros, que conterá tanto os que 
		// foram adicionados manualmente pelo usuário quanto os que foram definidos por
		// certos parâmetros (como luminosidade, rotação, etc).
		@SuppressWarnings("unchecked")
		List<String> allVideoFilters = (List<String>) videoFilters.clone();
		
		// Ganho de luminosidade (>1 deixa mais claro, <1 deixa mais escuro)
//		if (ganhoLuminosidade != null) {
//			allVideoFilters.add("lutyuv=y=val*" + ganhoLuminosidade);
//		}
		// TODO: Melhorar, voltando para um esquema parecido com o anterior, com "ganhoLuminosidade", mas utilizando as curvas em vez de "lutyuv"
		if (luminosidadeMaisClara) {
			allVideoFilters.add("curves=r='0/0.1 0.3/0.5 1/1':g='0/0.1 0.3/0.5 1/1':b='0/0.1 0.3/0.5 1/1'");
		}
		
		// Redimensionando vídeo
		if (videoResolutionWidth != null && videoResolutionHeight != null) {
			allVideoFilters.add("scale=w=" + videoResolutionWidth + ":h=" + videoResolutionHeight);
		} else if (videoResolutionWidth != null && videoResolutionHeight == null) {
			allVideoFilters.add("scale=" + videoResolutionWidth + ":-1");
		} else if (videoResolutionWidth == null && videoResolutionHeight != null) {
			allVideoFilters.add("scale=-1:" + videoResolutionHeight);
		}
		
		// Copiar metadados para destino
		if (videoCopiarMetadados) {
			commands.add("-map_metadata");
			commands.add("0");
		}
		
		// Rotação do vídeo / flip
		if (videoRotationHorizontalFlip) {
			
			// Se encontrar uma forma de fazer "flip" sem reprocessar o vídeo, retirar essa restrição.
			if (ENCODER_COPY.equals(videoEncoderCodec)) {
				throw new IOException("Não é possível selecionar 'videoRotationHorizontalFlip' com encoder 'copy', pois o vídeo precisa ser reprocessado.", null);
			}
			
			// Fonte: https://duxyng.wordpress.com/2013/04/07/rotateflip-video-with-ffmpeg/
			allVideoFilters.add("hflip");
			
		} else if (videoRotationVerticalFlip) {
			
			// Se encontrar uma forma de fazer "flip" sem reprocessar o vídeo, retirar essa restrição.
			if (ENCODER_COPY.equals(videoEncoderCodec)) {
				throw new IOException("Não é possível selecionar 'videoRotationVerficalFlip' com encoder 'copy', pois o vídeo precisa ser reprocessado", null);
			}
			
			// Fonte: https://duxyng.wordpress.com/2013/04/07/rotateflip-video-with-ffmpeg/
			allVideoFilters.add("vflip");
			
		} else if (videoRotation != null) {
			
			// Se vídeo será somente copiado, adiciona um metadado de rotação.
			// Se vídeo será recodificado, precisa fazer um "transpose"
			if (ENCODER_COPY.equals(videoEncoderCodec)) {
				commands.add("-metadata:s:v:0");
				commands.add("rotate=" + videoRotation);
				
			} else {
				
				/*
				 * For the transpose parameter you can pass:
				 * 0 = 90CounterCLockwise and Vertical Flip (default)
				 * 1 = 90Clockwise
				 * 2 = 90CounterClockwise
				 * 3 = 90Clockwise and Vertical Flip
				 * Use -vf "transpose=2,transpose=2" for 180 degrees.
				 * 
				 * Fonte: https://stackoverflow.com/questions/3937387/rotating-videos-with-ffmpeg
				 */
				if (videoRotation.equals(90)) {
					allVideoFilters.add("transpose=2");
					
				} else if (videoRotation.equals(180)) {
					allVideoFilters.add("transpose=2");
					allVideoFilters.add("transpose=2");
					
				} else if (videoRotation.equals(270)) {
					allVideoFilters.add("transpose=1");
					
				} else {
					throw new IOException("Não sei rotacionar " + videoRotation + "º");
				}
			}
		}
		
		// Adiciona os filtros (ex: scale), separados por vírgula
		if (!allVideoFilters.isEmpty()) {
			commands.add("-vf");
			commands.add(StringUtils.join(allVideoFilters, ","));
		}
		
		// Codec de áudio
		if (audioEncoderCodec != null) {
			commands.add("-c:a");
			commands.add(audioEncoderCodec);
		}
		
		// Parametros extras para processar o áudio (somente se estiver utilizando ENCODE)
		boolean recompactarAudio = audioEncoderCodec != null && !ENCODER_COPY.equals(audioEncoderCodec);
		if (recompactarAudio) {
			if (audioQualidade != null) {
				commands.add("-b:a");
				commands.add(audioQualidade);
			}
			if (audioExtraParameters != null) {
				commands.addAll(audioExtraParameters);
			}
		} else {
			if (audioExtraParameters != null) {
				LOGGER.warn("Foram definidos parâmetros de processamento de áudio, mas o áudio não está sendo reprocessado. Os parâmetros serão ignorados. audioEncoderCodec='" + audioEncoderCodec + "', audioExtraParameters='" + audioExtraParameters + "'");
			}
		}
		
		// Mover metadados do áudio para o início
		if (audioMoverMetadadosParaInicio) {
			commands.add("-movflags");
			commands.add("+faststart");
		}
		
		return commands;
	}	
}
