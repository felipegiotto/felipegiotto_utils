package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.io.File;

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
		
		// Se, agora, mudar o áudio para "copy", também deve zerar as informações extras de áudio
		ffmpeg.setAudio(TipoAudioVideo.COPY);
		assertEquals("ffmpeg -i entrada.avi -c:a copy saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
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
		ffmpeg.configurarPadraoCamerasFelipe(true);
		assertEquals("nice -n 15 ffmpeg -i teste.avi -c:v libx264 -preset slow -crf 24 -map_metadata 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));

		ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.configurarPadraoCamerasFelipe(false);
		assertEquals("nice -n 15 ffmpeg -i teste.avi -c:v libx264 -crf 24 -map_metadata 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));}

	@Test
	public void configurarPadraoCentralMultimidia() {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("src/test/resources/video_1280x544.mov");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.configurarPadraoCentralMultimidia(true);
		assertEquals("nice -n 15 ffmpeg -i src/test/resources/video_1280x544.mov -c:v libxvid -qscale:v 10 -vf scale=w=720:h=304 -c:a libmp3lame -qscale:a 5 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}

	@Test
	public void analisarResolucaoArquivoVideo() throws Exception {
		File input = new File("src/test/resources/video_1280x544.mov");
		Point resolucao = FFmpegCommand.getResolucaoVideo(input);
		assertNotNull(resolucao);
		assertEquals(1280, resolucao.getX(), 0.001);
		assertEquals(544, resolucao.getY(), 0.001);
	}
	
	@Test
	public void analisarDuracaoArquivoVideo() throws Exception {
		File input = new File("src/test/resources/video_1280x544.mov");
		assertEquals(2, FFmpegCommand.getDuracaoSegundosVideo(input));
	}

	@Test
	public void isArquivoVideoTest() {
		assertTrue(FFmpegCommand.isArquivoVideo("video.avi"));
		assertFalse(FFmpegCommand.isArquivoVideo("imagem.jpg"));
	}
}
