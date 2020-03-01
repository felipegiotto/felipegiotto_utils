package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.felipegiotto.utils.ffmpeg.util.FFmpegException;

public class FFmpegFileInfoTest {

	@Test
	public void getDateYMD() throws Exception {
		FFmpegFileInfo fileInfo = getFileInfoComCache("date_ymd.txt");
		
		// date            : 20171221
		assertEquals(LocalDateTime.of(2017, 12, 21, 0, 0, 0), fileInfo.getCreationDateTime(false));
		
		try {
			fileInfo.getVideoResolution();
			fail("Não deveria ter conseguido ler resolução de vídeo (ver arquivo date_ymd.txt)");
		} catch (FFmpegException ex) {
		}
		
		assertEquals(null, fileInfo.getVideoFPS());
	}
	
	@Test
	public void getDateYMDHMS() throws Exception {
		FFmpegFileInfo fileInfo = getFileInfoComCache("date_ymdhms.txt");
		
		// date            : 2011-10-15 14:08:06
		assertEquals(LocalDateTime.of(2011, 10, 15, 14, 8, 6), fileInfo.getCreationDateTime(false));
		assertEquals(LocalDateTime.of(2011, 10, 15, 11, 8, 6), fileInfo.getCreationDateTime(true));
		
		assertEquals(null, fileInfo.getVideoFPS());
	}
	
	@Test
	public void getCreationDateCameraNikon() throws Exception {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_camera_nikon.txt");
		
		// Timestamp da câmera Nikon já está em "-3", então não pode alterar
		// creation_time   : 2019-05-18T18:24:34.000000Z
		assertEquals(LocalDateTime.of(2019, 5, 18, 18, 24, 34), fileInfo.getCreationDateTime(false));
		
		// Resolução do vídeo Full HD
		assertEquals(new Dimension(1920, 1080), fileInfo.getVideoResolution());
		
		// Define a resolução máxima do vídeo (700 x 500) com múltiplo de 16, e mantendo 
		// a mesma proporção do vídeo de entrada, que deverá resultar em 688x384
		FFmpegParameters parameters = new FFmpegParameters();
		parameters.setVideoMaxResolutionFromFile(700, 500, 16, fileInfo);
		assertEquals(688, parameters.getVideoResolutionFixedWidth().intValue());
		assertEquals(384, parameters.getVideoResolutionFixedHeight().intValue());
		
		// Duração (em segundos) do vídeo
		assertEquals(40.24, fileInfo.getVideoDurationSeconds(), 0.01);
		
		// Frames por segundo do vídeo
		assertEquals(29.97, fileInfo.getVideoFPS(), 0.01);
	}
	
	@Test
	public void getCreationDateVideoSemMetadados() throws Exception {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_foto_morph.txt");
		
		assertEquals(null, fileInfo.getCreationDateTime(false));
		
		// Resolução do vídeo Full HD
		assertEquals(new Dimension(1620, 1080), fileInfo.getVideoResolution());
		
		// Define a resolução máxima do vídeo (200 x 200) mantendo 
		// a mesma proporção do vídeo de entrada, que deverá resultar em 200x133
		FFmpegParameters parameters = new FFmpegParameters();
		parameters.setVideoMaxResolutionFromFile(200, 200, null, fileInfo);
		assertEquals(200, parameters.getVideoResolutionFixedWidth().intValue());
		assertEquals(133, parameters.getVideoResolutionFixedHeight().intValue());
		
		// Duração (em segundos) do vídeo
		assertEquals(26, fileInfo.getVideoDurationSeconds(), 0.01);
		
		// Frames por segundo do vídeo
		assertEquals(25f, fileInfo.getVideoFPS(), 0.01);
	}

	@Test
	public void getCreationDateVideoComDataZero() throws Exception {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_data_zero.txt");
		
		assertEquals(null, fileInfo.getCreationDateTime(false));
	}
	
	@Test
	public void getCreationTimeIphoneX() throws Exception {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_iphone_com_creation_time.txt");
		
		// Timestamp está em GMT, precisa converter para GMT-3
		// creation_time   : 2019-05-04T16:22:49.000000Z
		assertEquals(LocalDateTime.of(2019, 5, 4, 13, 22, 49), fileInfo.getCreationDateTime(true));
		assertEquals(LocalDateTime.of(2019, 5, 4, 16, 22, 49), fileInfo.getCreationDateTime(false)); // Se não converter, fica errado
		
		// Resolução do vídeo Full HD
		assertEquals(new Dimension(1920, 1080), fileInfo.getVideoResolution());

		// Duração (em segundos) do vídeo
		assertEquals(55.99, fileInfo.getVideoDurationSeconds(), 0.01);
		
		// Frames por segundo do vídeo
		assertEquals(60, fileInfo.getVideoFPS(), 0.01);
	}
	
	@Test
	public void getCreationDateIphoneSEComHorarioVerao() throws Exception {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_iphone_se.txt");
		
		// Como o horário já está no timezone correto, o parâmetro "ajustarTimeZone" não influencia
		// creation_time   : 2019-01-04T21:30:43.000000Z
		// com.apple.quicktime.creationdate: 2019-01-04T19:30:43-0200
		assertEquals(LocalDateTime.of(2019, 1, 4, 19, 30, 43), fileInfo.getCreationDateTime(true));
		assertEquals(LocalDateTime.of(2019, 1, 4, 19, 30, 43), fileInfo.getCreationDateTime(false));
		
		// Resolução do vídeo Full HD
		assertEquals(new Dimension(1920, 1080), fileInfo.getVideoResolution());

		// Duração (em segundos) do vídeo
		assertEquals(38.46, fileInfo.getVideoDurationSeconds(), 0.01);
		
		// Frames por segundo do vídeo
		assertEquals(29.98, fileInfo.getVideoFPS(), 0.01);
	}
	
	@Test
	public void getCreationDateIphoneX() throws Exception {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_iphone_x.txt");
		
		// Timestamp está em GMT, precisa converter para GMT-3
		// creation_time   : 2019-05-04T19:11:20.000000Z
		assertEquals(LocalDateTime.of(2019, 5, 4, 16, 11, 20), fileInfo.getCreationDateTime(true));
		assertEquals(LocalDateTime.of(2019, 5, 4, 19, 11, 20), fileInfo.getCreationDateTime(false)); // Se não converter, fica errado
		
		// Resolução do vídeo
		assertEquals(new Dimension(568, 320), fileInfo.getVideoResolution());

		// Duração (em segundos) do vídeo
		assertEquals(3670.98, fileInfo.getVideoDurationSeconds(), 0.01);
		
		// Frames por segundo do vídeo
		assertEquals(30, fileInfo.getVideoFPS(), 0.01);
	}

	public static FFmpegFileInfo getFileInfoComCache(String arquivoCache) throws Exception {
		File file = new File("src/test/resources/FFmpegFileInfoTest/" + arquivoCache);
		FFmpegFileInfo fileInfo = new FFmpegFileInfo(file);
		fileInfo.cacheFileInfo = FileUtils.readLines(file, StandardCharsets.UTF_8.toString());
		return fileInfo;
	}
	
	/**
	 * Método que pode ser utilizado na elaboração de um caso de testes, para extrair os metadados
	 * de um vídeo para um arquivo de texto. Este arquivo, então, pode ser utilizado para os testes.
	 * 
	 * @param caminhoVideo : onde está o vídeo cujos dados serão extraídos
	 * @param caminhoSalvarDados : onde os dados devem ser salvos
	 * @throws IOException
	 */
	private static void extrairMetadadosParaArquivoParaElaborarTeste(String caminhoVideo, String caminhoSalvarDados) throws IOException {
		File arquivoSalvarDados = new File(caminhoSalvarDados);
		
		FFmpegFileInfo fileInfo = new FFmpegFileInfo(new File(caminhoVideo));
		FileUtils.writeLines(arquivoSalvarDados, StandardCharsets.UTF_8.toString(), fileInfo.getFullFileInfo());
		System.out.println("Arquivo gravado: " + arquivoSalvarDados);
	}
	
	public static void main(String[] args) throws Exception {
		FFmpegCommand.setFFmpegPath("ffmpeg/ffmpeg-94112-gbb11584924");
		extrairMetadadosParaArquivoParaElaborarTeste(
				"DSC_0138_compact_.mov",
				"src/test/resources/FFmpegFileInfoTest/.txt");
	}	
}
