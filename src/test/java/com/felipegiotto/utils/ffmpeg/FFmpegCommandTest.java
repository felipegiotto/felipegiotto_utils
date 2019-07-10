package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		ffmpeg.getParameters().setAudioMoverMetadadosParaInicio(false);
		ffmpeg.getParameters().setVideoCopiarMetadados(false);
		return ffmpeg;
	}
	
	@Test
	public void linhaDeComandoMinima() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		assertEquals("src/test/resources/ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}

	@Test
	public void definindoPrioridade() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.setProcessNicePriority(15);
		assertEquals("nice -n 15 src/test/resources/ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoInicioOuFim() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setTempoInicial("00:00:10");
		ffmpeg.getParameters().setTempoFinal("00:00:20");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -ss 00:00:10 -to 00:00:20 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeVideoComParametrosExtras() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		ffmpeg.getParameters().setVideoAddExtraParameters("-preset", "slow");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -preset slow saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeVideoParaVariosVideos() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada1.avi");
		ffmpeg.addInputFile("entrada2.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		assertEquals("src/test/resources/ffmpeg -i entrada1.avi -i entrada2.avi -filter_complex concat=n=2:v=1:a=1 -c:v libx264 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void redimensionandoVideo() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		
		// Tamanho definido
		ffmpeg.getParameters().setVideoResolution(200, 300);
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf scale=w=200:h=300 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Somente largura
		ffmpeg.getParameters().setVideoResolution(200, null);
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf scale=200:-1 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Somente altura
		ffmpeg.getParameters().setVideoResolution(null, 300);
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf scale=-1:300 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoLuminosidade() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
//		ffmpeg.getParameters().setGanhoLuminosidade(2.0); // Deixa vídeo mais claro
		ffmpeg.getParameters().setLuminosidadeMaisClara(true);
//		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf lutyuv=y=val*2.0 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf curves=r='0/0.1 0.3/0.5 1/1':g='0/0.1 0.3/0.5 1/1':b='0/0.1 0.3/0.5 1/1' saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
//		ffmpeg.getParameters().setGanhoLuminosidade(null); // Retorna ao padrao
		ffmpeg.getParameters().setLuminosidadeMaisClara(false); // Retorna ao padrao
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void rotacionandoVideo() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoRotation(90);
		
		ffmpeg.getParameters().setVideoEncoderCopy();
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v copy -metadata:s:v:0 rotate=90 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf transpose=2 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void rotacionandoHflip() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoRotationHorizontalFlip(true);
		
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v libx264 -vf hflip saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Se encontrar uma forma de fazer "flip" sem reprocessar o vídeo, implementar e habilitar o teste
//		ffmpeg.setVideoEncoderCopy();
//		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v copy -metadata:s:v:0 rotate=90 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void copiandoStreamsDeAudioEVideo() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCopy();
		ffmpeg.getParameters().setAudioEncoderCopy();
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:v copy -c:a copy saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void copiandoMetadadosParaArquivoDeSaida() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().setVideoCopiarMetadados(true);
		assertEquals("src/test/resources/ffmpeg -i teste.avi -map_metadata 0 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.getParameters().setVideoCopiarMetadados(false);
		assertEquals("src/test/resources/ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeAudioComParametrosExtras() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setAudioEncoderCodec("aac");
		ffmpeg.getParameters().setAudioAddExtraParameters("-b:a", "128k");
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:a aac -b:a 128k saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Se, agora, mudar o áudio para "copy", também deve zerar as informações extras de áudio
		ffmpeg.getParameters().setAudioEncoderCopy();
		assertEquals("src/test/resources/ffmpeg -i entrada.avi -c:a copy saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void movendoMetadadosDeAudioParaInicioDoVideo() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().setAudioMoverMetadadosParaInicio(true);
		assertEquals("src/test/resources/ffmpeg -i teste.avi -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.getParameters().setAudioMoverMetadadosParaInicio(false);
		assertEquals("src/test/resources/ffmpeg -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void configurarPadraoCamerasFelipe() throws IOException {
		
		// H264
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().configurarPadraoCamerasFelipe(false, true);
		assertEquals("src/test/resources/ffmpeg -i teste.avi -c:v libx264 -crf 24 -preset slow -map_metadata 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));

		// H265
		ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().configurarPadraoCamerasFelipe(true, false);
		assertEquals("src/test/resources/ffmpeg -i teste.avi -c:v libx265 -crf 28 -tag:v hvc1 -map_metadata 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// H265 com qualidade CRF=30
		ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().configurarPadraoCamerasFelipe(true, false);
		ffmpeg.getParameters().setQualidadeCrf(30);
		assertEquals("src/test/resources/ffmpeg -i teste.avi -c:v libx265 -crf 30 -tag:v hvc1 -map_metadata 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
	}

	@Test
	public void configurarPadraoCentralMultimidia() throws IOException {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("src/test/resources/video_1280x544.mov");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().configurarPadraoCentralMultimidia(new File("src/test/resources/video_1280x544.mov"));
		assertEquals("src/test/resources/ffmpeg -i src/test/resources/video_1280x544.mov -c:v libxvid -qscale:v 10 -vf scale=w=720:h=304 -c:a libmp3lame -qscale:a 5 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
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
		f.getParameters().setVideoEncoderCopy();
		f.addInputFile(new File("/Users/taeta/Desktop/Lixo/lixo.mov"));
		f.getParameters().setVideoRotationVerticalFlip(true);
		f.setOutputFile(new File("/Users/taeta/Desktop/Lixo/lixo_out.mov"));
		f.getParameters().setTempoFinal("00:00:03");
		f.runAndWait(true);
	}

}
