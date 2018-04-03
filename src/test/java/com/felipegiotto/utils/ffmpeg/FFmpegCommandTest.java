package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class FFmpegCommandTest {

	@BeforeClass
	public static void prepararCaminhoFFmpeg() {
		FFmpegCommand.setFFmpegPath("src/test/resources/ffmpeg");
	}
	
	private FFmpegCommand criarObjetoMinimo() {
		FFmpegCommand ffmpeg = new FFmpegCommand();
		ffmpeg.setAudioMoverMetadadosParaInicio(false);
		ffmpeg.setVideoCopiarMetadados(false);
		return ffmpeg;
	}
	
	@Test
	public void linhaDeComandoMinima() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		assertEquals("src/test/resources/ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}

	@Test
	public void definindoPrioridade() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.setProcessNicePriority(15);
		assertEquals("nice -n 15 src/test/resources/ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoInicioOuFim() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setTempoInicial("00:00:10");
		ffmpeg.setTempoFinal("00:00:20");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -ss 00:00:10 -to 00:00:20 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeVideoComParametrosExtras() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideoEncoderCodec("libx264");
		ffmpeg.setVideoAddExtraParameters("-preset", "slow");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -preset slow saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void redimensionandoVideo() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideoEncoderCodec("libx264");
		
		// Tamanho definido
		ffmpeg.setVideoResolution(200, 300);
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf scale=w=200:h=300 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Somente largura
		ffmpeg.setVideoResolution(200, null);
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf scale=200:-1 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Somente altura
		ffmpeg.setVideoResolution(null, 300);
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf scale=-1:300 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoLuminosidade() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideoEncoderCodec("libx264");
		ffmpeg.setGanhoLuminosidade(2.0); // Deixa vídeo mais claro
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf lutyuv=y=val*2.0 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.setGanhoLuminosidade(null); // Retorna ao padrao
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void rotacionandoVideo() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideoRotation(90);
		
		ffmpeg.setVideoEncoderCopy();
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v copy -metadata:s:v:0 rotate=90 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.setVideoEncoderCodec("libx264");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf transpose=2 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void rotacionandoHflip() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideoRotationHorizontalFlip(true);
		
		ffmpeg.setVideoEncoderCodec("libx264");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf hflip saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Se encontrar uma forma de fazer "flip" sem reprocessar o vídeo, implementar e habilitar o teste
//		ffmpeg.setVideoEncoderCopy();
//		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v copy -metadata:s:v:0 rotate=90 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void copiandoStreamsDeAudioEVideo() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setVideoEncoderCopy();
		ffmpeg.setAudioEncoderCopy();
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v copy -c:a copy saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void copiandoMetadadosParaArquivoDeSaida() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.setVideoCopiarMetadados(true);
		assertEquals("src/test/resources/ffmpeg -i teste.avi -map_metadata 0 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.setVideoCopiarMetadados(false);
		assertEquals("src/test/resources/ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeAudioComParametrosExtras() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setAudioEncoderCodec("aac");
		ffmpeg.setAudioAddExtraParameters("-b:a", "128k");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:a aac -b:a 128k saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Se, agora, mudar o áudio para "copy", também deve zerar as informações extras de áudio
		ffmpeg.setAudioEncoderCopy();
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:a copy saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void movendoMetadadosDeAudioParaInicioDoVideo() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.setAudioMoverMetadadosParaInicio(true);
		assertEquals("src/test/resources/ffmpeg -i teste.avi -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.setAudioMoverMetadadosParaInicio(false);
		assertEquals("src/test/resources/ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void configurarPadraoCamerasFelipe() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.configurarPadraoCamerasFelipe(true);
		assertEquals("nice -n 15 src/test/resources/ffmpeg -i teste.avi -c:v libx264 -preset slow -crf 24 -map_metadata 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));

		ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.configurarPadraoCamerasFelipe(false);
		assertEquals("nice -n 15 src/test/resources/ffmpeg -i teste.avi -c:v libx264 -crf 24 -map_metadata 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));}

	@Test
	public void configurarPadraoCentralMultimidia() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.setInputFile("src/test/resources/video_1280x544.mov");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.configurarPadraoCentralMultimidia(true);
		assertEquals("nice -n 15 src/test/resources/ffmpeg -i src/test/resources/video_1280x544.mov -c:v libxvid -qscale:v 10 -vf scale=w=720:h=304 -c:a libmp3lame -qscale:a 5 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
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
	
	public static void main(String[] args) throws Exception {
		//TODO: sobrescrever arquivo original, porque se ja existir, trava
		prepararCaminhoFFmpeg();
		FFmpegCommand f = new FFmpegCommand();
		f.setVideoEncoderCopy();
		f.setInputFile(new File("/Users/taeta/Desktop/Lixo/lixo.mov"));
		f.setVideoRotationVerticalFlip(true);
		f.setOutputFile(new File("/Users/taeta/Desktop/Lixo/lixo_out.mov"));
		f.setTempoFinal("00:00:03");
		f.runAndWait(true);
	}

}
