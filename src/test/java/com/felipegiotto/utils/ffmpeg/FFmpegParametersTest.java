package com.felipegiotto.utils.ffmpeg;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class FFmpegParametersTest {

	@Test
	public void redimensionandoVideo() throws Exception {
		FFmpegFileInfo fileInfo = FFmpegFileInfoTest.getFileInfoComCache("video_camera_nikon.txt");
		FFmpegParameters parameters = new FFmpegParameters();
		parameters.setVideoCopiarMetadados(false);
		parameters.setAudioMoverMetadadosParaInicio(false);
		
		// Tamanho definido
		parameters.setVideoResolutionFixed(200, 300);
		assertEquals("-vf scale=w=200:h=300", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Somente largura
		parameters.setVideoResolutionFixed(200, null);
		assertEquals("-vf scale=200:-1", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Somente altura
		parameters.setVideoResolutionFixed(null, 300);
		assertEquals("-vf scale=-1:300", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Altura e largura, mas mantendo proporção de entrada, sem obedecer múltiplo
		parameters.setVideoResolutionConstrained(200, 200, null);
		assertEquals("-vf scale=w=200:h=112", StringUtils.join(parameters.buildParameters(fileInfo), " "));
		
		// Altura e largura, mas mantendo proporção de entrada e obedecendo múltiplo
		parameters.setVideoResolutionConstrained(700, 500, 16);
		assertEquals("-vf scale=w=688:h=384", StringUtils.join(parameters.buildParameters(fileInfo), " "));
	}

}
