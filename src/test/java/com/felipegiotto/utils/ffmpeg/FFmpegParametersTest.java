package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class FFmpegParametersTest {

	@Test
	public void inicioFimEmSegundos() {
		FFmpegParameters parameters = new FFmpegParameters();
		parameters.setTempoInicial(3600 + 60 + 1);
		assertEquals("01:01:01", parameters.getTempoInicial());
		parameters.setTempoFinal(2 * (3600 + 60 + 1));
		assertEquals("02:02:02", parameters.getTempoFinal());
		parameters.setTempoFinal(60);
		assertEquals("00:01:00", parameters.getTempoFinal());
	}
	
	@Test
	public void redimensionandoVideo() throws Exception {
		FFmpegFileInfo fileInfo = FFmpegFileInfoTest.getFileInfoComCache("video_camera_nikon.txt");
		FFmpegParameters parameters = new FFmpegParameters();
		parameters.setVideoCopiarMetadados(false);
		parameters.setAudioMoverMetadadosParaInicio(false);
		
		// Tamanho definido
		parameters.setVideoResolutionFixed(200, 300);
		assertEquals("-vsync 0 -vf scale=w=200:h=300", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Somente largura
		parameters.setVideoResolutionFixed(200, null);
		assertEquals("-vsync 0 -vf scale=200:-1", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Somente altura
		parameters.setVideoResolutionFixed(null, 300);
		assertEquals("-vsync 0 -vf scale=-1:300", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Altura e largura, mas mantendo proporção de entrada, sem obedecer múltiplo
		parameters.setVideoResolutionConstrained(200, 200, null, false);
		assertEquals("-vsync 0 -vf scale=w=200:h=112", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Altura e largura, mas mantendo proporção de entrada e obedecendo múltiplo
		parameters.setVideoResolutionConstrained(700, 500, 16, false);
		assertEquals("-vsync 0 -vf scale=w=688:h=384", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Altura e largura proporcional mas com vídeo tombado 
		parameters.setVideoResolutionConstrained(500, 700, 16, true);
		assertEquals("-vsync 0 -vf scale=w=688:h=384", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		parameters.setVideoResolutionConstrained(500, 700, 16, false);
		assertEquals("-vsync 0 -vf scale=w=496:h=272", StringUtils.join(parameters.buildParameters(fileInfo), " "));
	}

	@Test
	public void limitandoFPS() throws Exception {
		FFmpegFileInfo fileInfo = FFmpegFileInfoTest.getFileInfoComCache("video_camera_nikon.txt");
		FFmpegParameters parameters = new FFmpegParameters();
		parameters.setVideoCopiarMetadados(false);
		parameters.setAudioMoverMetadadosParaInicio(false);
		
		// Tamanho definido
		parameters.setVideoFPS(60);
		assertEquals("-vf fps=fps=60", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
	}
	
	@Test
	public void cropandoVideo() throws Exception {
		FFmpegFileInfo fileInfo = FFmpegFileInfoTest.getFileInfoComCache("video_camera_nikon.txt");
		FFmpegParameters parameters = new FFmpegParameters();
		parameters.setVideoCopiarMetadados(false);
		parameters.setAudioMoverMetadadosParaInicio(false);
		
		// Tamanho definido
		parameters.setVideoCropRectangle(new Rectangle(new Point(10, 20), new Dimension(100, 200)));
		assertEquals("-vsync 0 -vf crop=100:200:10:20", StringUtils.join(parameters.buildParameters(fileInfo), " "));
	}
}
