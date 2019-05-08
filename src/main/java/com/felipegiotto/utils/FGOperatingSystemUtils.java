package com.felipegiotto.utils;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class FGOperatingSystemUtils {

	public static boolean isScreensaverActive() {
		try {
			if (SystemUtils.IS_OS_LINUX) {
				Process process = Runtime.getRuntime().exec(new String[] {"gnome-screensaver-command", "--query"});
				process.waitFor();
				
				// Exemplo de retorno:
				// A proteção de tela está inativa
				// A proteção de tela está ativa
				String retorno = StringUtils.join(IOUtils.readLines(process.getInputStream(), Charset.defaultCharset()), "");
				return retorno.contains(" ativa") || retorno.contains(" active");
				
			} else if (SystemUtils.IS_OS_MAC_OSX) {
				Process process = Runtime.getRuntime().exec(new String[] {"ioreg", "-n", "IODisplayWrangler"});
				
				try {
					// Exemplo de retorno:
					// Tela ATIVA (protetor desligado)
					// (...) ,"DevicePowerState"=4, (...)
					// Tela DESLIGADA:
					// (...) ,"DevicePowerState"=1, (...)
					String retorno = StringUtils.join(IOUtils.readLines(process.getInputStream(), Charset.defaultCharset()), "");
					Pattern p = Pattern.compile("\"DevicePowerState\"=(\\d+)");
					Matcher m = p.matcher(retorno);
					if (m.find()) {
						if (!m.group(1).equals("4")) {
							return true;
						}
					}
					return false;
					
				} finally {
					process.waitFor();
				}
				
			} else {
				throw new RuntimeException("Unrecognized Operating System");
			}
			
		} catch (Exception e) {
			return false;
		}
	}
}
