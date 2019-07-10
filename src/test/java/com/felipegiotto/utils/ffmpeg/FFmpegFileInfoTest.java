package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FFmpegFileInfoTest {

	@Test
	public void getCreationDateIphoneSEComHorarioVerao() throws IOException {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_iphone_se.txt");
		
		// Como o horário já está no timezone correto, o parâmetro "ajustarTimeZone" não influencia
		// creation_time   : 2019-01-04T21:30:43.000000Z
		// com.apple.quicktime.creationdate: 2019-01-04T19:30:43-0200
		assertEquals(LocalDateTime.of(2019, 1, 4, 19, 30, 43), fileInfo.getCreationDateTime(true));
		assertEquals(LocalDateTime.of(2019, 1, 4, 19, 30, 43), fileInfo.getCreationDateTime(false));
	}
	
	@Test
	public void getCreationDateIphoneX() throws IOException {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_iphone_x.txt");
		
		// Timestamp está em GMT, precisa converter para GMT-3
		// creation_time   : 2019-05-04T19:11:20.000000Z
		assertEquals(LocalDateTime.of(2019, 5, 4, 16, 11, 20), fileInfo.getCreationDateTime(true));
		assertEquals(LocalDateTime.of(2019, 5, 4, 19, 11, 20), fileInfo.getCreationDateTime(false)); // Se não converter, fica errado
	}

	@Test
	public void getCreationTimeIphoneX() throws IOException {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_iphone_com_creation_time.txt");
		
		// Timestamp está em GMT, precisa converter para GMT-3
		// creation_time   : 2019-05-04T16:22:49.000000Z
		assertEquals(LocalDateTime.of(2019, 5, 4, 13, 22, 49), fileInfo.getCreationDateTime(true));
		assertEquals(LocalDateTime.of(2019, 5, 4, 16, 22, 49), fileInfo.getCreationDateTime(false)); // Se não converter, fica errado
	}
	
	@Test
	public void getCreationDateCameraNikon() throws IOException {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_camera_nikon.txt");
		
		// Timestamp da câmera Nikon já está em "-3", então não pode alterar
		// creation_time   : 2019-05-18T18:24:34.000000Z
		assertEquals(LocalDateTime.of(2019, 5, 18, 18, 24, 34), fileInfo.getCreationDateTime(false));
	}
	
	@Test
	public void getDateYMDHMS() throws IOException {
		FFmpegFileInfo fileInfo = getFileInfoComCache("date_ymdhms.txt");
		
		// date            : 2011-10-15 14:08:06
		assertEquals(LocalDateTime.of(2011, 10, 15, 14, 8, 6), fileInfo.getCreationDateTime(false));
		assertEquals(LocalDateTime.of(2011, 10, 15, 11, 8, 6), fileInfo.getCreationDateTime(true));
	}
	
	@Test
	public void getDateYMD() throws IOException {
		FFmpegFileInfo fileInfo = getFileInfoComCache("date_ymd.txt");
		
		// date            : 20171221
		assertEquals(LocalDateTime.of(2017, 12, 21, 0, 0, 0), fileInfo.getCreationDateTime(false));
	}
	
	@Test
	public void getCreationDateVideoSemMetadados() throws IOException {
		FFmpegFileInfo fileInfo = getFileInfoComCache("video_foto_morph.txt");
		
		assertEquals(null, fileInfo.getCreationDateTime(false));
	}

	private FFmpegFileInfo getFileInfoComCache(String arquivoCache) throws IOException {
		FFmpegFileInfo fileInfo = new FFmpegFileInfo(null);
		fileInfo.cacheFileInfo = FileUtils.readLines(new File("src/test/resources/FFmpegFileInfoTest/" + arquivoCache), StandardCharsets.UTF_8.toString());
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
		// TODO: Limpar antes do commit
		FFmpegCommand.setFFmpegPath("/Users/taeta/workspace/backup_fotos_e_macbook_ultimate/ffmpeg/ffmpeg-94112-gbb11584924");
		extrairMetadadosParaArquivoParaElaborarTeste(
				"/Volumes/TimeCapsuleManual/ComBackup/Fotos/2019/2019 - Leonardo da Vinci - 2o Ano/2019-05-18 - Aniver Leo/_EXPORT/2019-05-18-15-24-34 - Luiz e Aristeu brincando - DSC_0138_compact_.mov",
				"src/test/resources/FFmpegFileInfoTest/.txt");
	}	
}
