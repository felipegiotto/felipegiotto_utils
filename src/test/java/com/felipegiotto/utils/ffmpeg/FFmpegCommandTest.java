package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class FFmpegCommandTest {

	private static final String FFMPEG_TEST_PATH = "C:\\Users\\felip\\Programas\\ffmpeg.exe";

	@BeforeClass
	public static void prepararCaminhoFFmpeg() {
		//FFmpegCommand.setFFmpegPath("src/test/resources/ffmpeg");
		FFmpegCommand.setFFmpegPath(FFMPEG_TEST_PATH);
	}
	
	private FFmpegCommand criarObjetoMinimo() {
		FFmpegCommand ffmpeg = new FFmpegCommand();
		ffmpeg.getParameters().setAudioMoverMetadadosParaInicio(false);
		ffmpeg.getParameters().setVideoCopiarMetadados(false);
		return ffmpeg;
	}
	
	@Test
	public void linhaDeComandoMinima() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		assertEquals(FFMPEG_TEST_PATH + " -i teste.avi -vsync 0 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}

	@Test
	public void definindoPrioridade() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.setProcessNicePriority(15);
		assertEquals("nice -n 15 " + FFMPEG_TEST_PATH + " -i teste.avi teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoInicioOuFim() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setTempoInicial("00:00:10");
		ffmpeg.getParameters().setTempoFinal("00:00:20");
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -ss 00:00:10 -to 00:00:20 -vsync 0 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void embutindoLegendas() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("C:\\entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setSubtitleFile("C:\\legenda.srt", null); // Tamanho padrão
		assertEquals(FFMPEG_TEST_PATH + " -i C:\\entrada.avi -vsync 0 -vf subtitles='C\\:\\\\legenda.srt' saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("C:\\entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.setSubtitleFile("C:\\legenda.srt", 30); // Tamanho 30
		assertEquals(FFMPEG_TEST_PATH + " -i C:\\entrada.avi -vsync 0 -vf subtitles='C\\:\\\\legenda.srt':force_style='Fontsize=30' saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeVideoComParametrosExtras() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		ffmpeg.getParameters().setVideoAddExtraParameters("-preset", "slow");
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -preset slow -vsync 0 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeVideoParaVariosVideos() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada1.avi");
		ffmpeg.addInputFile("entrada2.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		assertEquals(FFMPEG_TEST_PATH + " -i entrada1.avi -i entrada2.avi -filter_complex concat=n=2:v=1:a=1 -c:v libx264 -vsync 0 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void redimensionandoVideo() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		
		// Tamanho definido
		ffmpeg.getParameters().setVideoResolutionFixed(200, 300);
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -vsync 0 -vf scale=w=200:h=300 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Somente largura
		ffmpeg.getParameters().setVideoResolutionFixed(200, null);
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -vsync 0 -vf scale=200:-1 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Somente altura
		ffmpeg.getParameters().setVideoResolutionFixed(null, 300);
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -vsync 0 -vf scale=-1:300 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoLuminosidade() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
//		ffmpeg.getParameters().setGanhoLuminosidade(2.0); // Deixa vídeo mais claro
		ffmpeg.getParameters().setVideoLuminosidadeMaisClara(true);
//		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -vf lutyuv=y=val*2.0 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -vsync 0 -vf curves=all='0/0.1 0.3/0.5 1/1' saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
//		ffmpeg.getParameters().setGanhoLuminosidade(null); // Retorna ao padrao
		ffmpeg.getParameters().setVideoLuminosidadeMaisClara(false); // Retorna ao padrao
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -vsync 0 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void rotacionandoVideo() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoRotation(90);
		
		ffmpeg.getParameters().setVideoEncoderCopy();
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v copy -vsync 0 -metadata:s:v:0 rotate=90 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -vsync 0 -vf transpose=2 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void rotacionandoHflip() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoRotationHorizontalFlip(true);
		
		ffmpeg.getParameters().setVideoEncoderCodec("libx264");
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v libx264 -vsync 0 -vf hflip saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Se encontrar uma forma de fazer "flip" sem reprocessar o vídeo, implementar e habilitar o teste
//		ffmpeg.setVideoEncoderCopy();
//		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v copy -metadata:s:v:0 rotate=90 saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void copiandoStreamsDeAudioEVideo() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setVideoEncoderCopy();
		ffmpeg.getParameters().setAudioEncoderCopy();
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -c:v copy -vsync 0 -c:a copy saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void copiandoMetadadosParaArquivoDeSaida() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().setVideoCopiarMetadados(true);
		assertEquals(FFMPEG_TEST_PATH + " -i teste.avi -map_metadata 0 -vsync 0 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.getParameters().setVideoCopiarMetadados(false);
		assertEquals(FFMPEG_TEST_PATH + " -i teste.avi -vsync 0 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void definindoCodecDeAudioComParametrosExtras() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("entrada.avi");
		ffmpeg.setOutputFile("saida.avi");
		ffmpeg.getParameters().setAudioEncoderCodec("aac");
		ffmpeg.getParameters().setAudioAddExtraParameters("-b:a", "128k");
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -vsync 0 -c:a aac -b:a 128k saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// Se, agora, mudar o áudio para "copy", também deve zerar as informações extras de áudio
		ffmpeg.getParameters().setAudioEncoderCopy();
		assertEquals(FFMPEG_TEST_PATH + " -i entrada.avi -vsync 0 -c:a copy saida.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void movendoMetadadosDeAudioParaInicioDoVideo() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().setAudioMoverMetadadosParaInicio(true);
		assertEquals(FFMPEG_TEST_PATH + " -i teste.avi -vsync 0 -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		ffmpeg.getParameters().setAudioMoverMetadadosParaInicio(false);
		assertEquals(FFMPEG_TEST_PATH + " -i teste.avi -vsync 0 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
	}
	
	@Test
	public void configurarPadraoCamerasFelipe() throws Exception {
		
		// H264
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().configurarPadraoCamerasFelipe(false, true);
		assertEquals(FFMPEG_TEST_PATH + " -i teste.avi -c:v libx264 -crf 24 -preset slow -map_metadata 0 -vsync 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));

		// H265
		ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().configurarPadraoCamerasFelipe(true, false);
		assertEquals(FFMPEG_TEST_PATH + " -i teste.avi -c:v libx265 -crf 28 -tag:v hvc1 -map_metadata 0 -vsync 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
		// H265 com qualidade CRF=30
		ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("teste.avi");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().configurarPadraoCamerasFelipe(true, false);
		ffmpeg.getParameters().setVideoQualidadeCrf(30);
		assertEquals(FFMPEG_TEST_PATH + " -i teste.avi -c:v libx265 -crf 30 -tag:v hvc1 -map_metadata 0 -vsync 0 -c:a aac -b:a 128k -movflags +faststart teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
		
	}

	@Test
	public void configurarPadraoCentralMultimidia() throws Exception {
		FFmpegCommand ffmpeg = criarObjetoMinimo();
		ffmpeg.addInputFile("src/test/resources/video_1280x544.mov");
		ffmpeg.setOutputFile("teste_output.avi");
		ffmpeg.getParameters().configurarPadraoCentralMultimidia(new File("src/test/resources/video_1280x544.mov"));
		assertEquals(FFMPEG_TEST_PATH + " -i src/test/resources/video_1280x544.mov -c:v libxvid -qscale:v 10 -vsync 0 -vf scale=w=720:h=304 -c:a libmp3lame -qscale:a 5 teste_output.avi", StringUtils.join(ffmpeg.buildParameters(), " "));
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
