package com.felipegiotto.utils.ffmpeg;

public class FFmpegException extends Exception {

	private static final long serialVersionUID = 1L;
	private String saidaComando;
	
	public FFmpegException(String msg, String saidaComando) {
		super(msg);
	}
	
	public FFmpegException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public String getSaidaComando() {
		return saidaComando;
	}
}
