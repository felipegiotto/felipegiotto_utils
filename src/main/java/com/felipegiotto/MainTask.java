package com.felipegiotto;

import com.felipegiotto.misc.FGEyeFatigueMonitor;
import com.felipegiotto.utils.FGInterativoUtils;

public class MainTask {

	public static void main(String[] args) throws Exception {
		System.out.println("felipegiotto_utils");
		switch (FGInterativoUtils.perguntarOpcoesParaUsuario("Qual classe deve ser executada?", 
				"FGEyeFatigueUtils - Avisa a cada X minutos para que seja realizada uma pausa")) {
		case 1: 
			FGEyeFatigueMonitor.main(null);
			break;
		}
	}
}
