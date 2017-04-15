package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class FFmpegCommandTest {

	@BeforeClass
	public static void prepararCaminhoFFmpeg() {
		FFmpegCommand.setFFmpegPath("ffmpeg");
	}
	
	private FFmpegCommand criarObjetoMinimo() {
		FFmpegCommand ffmpeg = new FFmpegCommand();
		ffmpeg.setAudioMoverMetadadosParaInicio(false);
		ffmpeg.setVideoCopiarMetadados(false);
		return ffmpeg;
	}
	
	@Test
	public void linhaDeComandoMinima() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		assertEquals("ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}

	@Test
	public void definindoPrioridade() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.setProcessNicePriority(15);
		assertEquals("nice -n 15 ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoInicioOuFim() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setTempoInicial("00:00:10");
		ffmpeg.setTempoFinal("00:00:20");
		assertEquals("ffmpeg -i entrada.avi -ss 00:00:10 -to 00:00:20 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeVideoComParametrosExtras() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideo(TipoAudioVideo.ENCODE);
		ffmpeg.setVideoEncoderCodec("libx264");
		ffmpeg.setVideoAddExtraParameters("-preset", "slow");
		assertEquals("ffmpeg -i entrada.avi -c:v libx264 -preset slow saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void rotacionandoVideo() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideo(TipoAudioVideo.ENCODE);
		ffmpeg.setVideoEncoderCodec("libx264");
		ffmpeg.setVideoRotation(90);
		assertEquals("ffmpeg -i entrada.avi -c:v libx264 -metadata:s:v:0 rotate=90 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void copiandoStreamsDeAudioEVideo() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideo(TipoAudioVideo.COPY);
		ffmpeg.setAudio(TipoAudioVideo.COPY);
		assertEquals("ffmpeg -i entrada.avi -c:v copy -c:a copy saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void copiandoMetadadosParaArquivoDeSaida() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.setVideoCopiarMetadados(true);
		assertEquals("ffmpeg -i teste.avi -map_metadata 0 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.setVideoCopiarMetadados(false);
		assertEquals("ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeAudioComParametrosExtras() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setAudio(TipoAudioVideo.ENCODE);
		ffmpeg.setAudioEncoderCodec("aac");
		ffmpeg.setAudioAddExtraParameters("-b:a", "128k");
		assertEquals("ffmpeg -i entrada.avi -c:a aac -b:a 128k saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void movendoMetadadosDeAudioParaInicioDoVideo() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.setAudioMoverMetadadosParaInicio(true);
		assertEquals("ffmpeg -i teste.avi -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.setAudioMoverMetadadosParaInicio(false);
		assertEquals("ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void configurarPadraoCamerasFelipe() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.configurarPadraoCamerasFelipe();
		assertEquals("nice -n 15 ffmpeg -i teste.avi -c:v libx264 -preset slow -crf 24 -map_metadata 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}

	@Test
	public void configurarPadraoCentralMultimidia() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.configurarPadraoCentralMultimidia(true);
		assertEquals("nice -n 15 ffmpeg -i teste.avi -c:v libxvid -preset slow -qscale:v 10 -vf scale=w=700:h=480:force_original_aspect_ratio=decrease -c:a libmp3lame -qscale:a 5 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}

}
