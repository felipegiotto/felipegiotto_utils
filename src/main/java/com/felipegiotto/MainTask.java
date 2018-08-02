package com.felipegiotto;

import com.felipegiotto.utils.FGEyeFatigueUtils;
import com.felipegiotto.utils.FGInterativoUtils;

public class MainTask {

	public static void main(String[] args) throws Exception {
		System.out.println("felipegiotto_utils");
		switch (FGInterativoUtils.perguntarOpcoesParaUsuario("Qual classe deve ser executada?", 
				"FGEyeFatigueUtils - Avisa a cada X minutos para que seja realizada uma pausa no trabalho")) {
		case 1: 
			FGEyeFatigueUtils.main(null);
			break;
		}
	}
}
