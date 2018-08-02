package com.felipegiotto.utils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * Classe utilizada para evitar fadiga ocular utilizando a técnica do "20-20-20" 
 * (a cada 20 minutos olhar para algo a 20 pés de distância durante 20 segundos).
 * 
 * OBS: Na verdade, a pausa é a cada 30 minutos.
 * 
 * Instruções: Executar a classe e fazer uma pausa quando notificado!
 * 
 * Fonte: https://abertoatedemadrugada.com/2014/02/evita-fadiga-ocular-em-frente-ao-pc-com.html
 * 
 * @author felipegiotto@gmail.com
 */
public class FGEyeFatigueUtils {

	private static final int TEMPO_EM_MINUTOS_ENTRE_PAUSAS = 30;
	private static final int TEMPO_EM_MINUTOS_PARA_REINICIAR_CONTAGEM_COM_PROTETOR_DE_TELA_ATIVO = 5;
	private static Object monitor = new Object();
	private static JFrame frame;
	private static JButton button;

	public static void main(String[] args) throws InterruptedException {

		prepareWindow();

		int minutosProtetorTelaAtivo = 0;
		while (true) {
			for (int i=TEMPO_EM_MINUTOS_ENTRE_PAUSAS; i>0; i--) {
				System.out.println(LocalDateTime.now() + ": Next break in " + i + "m");
				Thread.sleep(60_000);

				// Conta há quantos minutos o protetor de tela está ativo
				if (protetorDeTelaEstaAtivo()) {
					minutosProtetorTelaAtivo++;
				} else {
					minutosProtetorTelaAtivo = 0;
				}

				// Se protetor de tela estiver ativo há mais de 5 minutos, reinicia a contagem do tempo
				if (minutosProtetorTelaAtivo > TEMPO_EM_MINUTOS_PARA_REINICIAR_CONTAGEM_COM_PROTETOR_DE_TELA_ATIVO) {
					i = TEMPO_EM_MINUTOS_ENTRE_PAUSAS;
					System.out.println(LocalDateTime.now() + ": Protetor de tela ativo há " + minutosProtetorTelaAtivo + "m. Contagem reiniciada.");
				}
			}
			showWindow(frame);
			synchronized (monitor) {
				monitor.wait();
			}
			hideWindow(frame);
		}

	}

	private static boolean protetorDeTelaEstaAtivo() {
		try {
			if (SystemUtils.IS_OS_LINUX) {
				Process process = Runtime.getRuntime().exec(new String[] {"gnome-screensaver-command", "--query"});
				process.waitFor();
				
				// Exemplo de retorno:
				// A proteção de tela está inativa
				// A proteção de tela está ativa
				String retorno = StringUtils.join(IOUtils.readLines(process.getInputStream(), Charset.defaultCharset()), "");
				if (retorno.contains(" ativa") || retorno.contains(" active")) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private static void hideWindow(JFrame frame) {
		frame.setVisible(false);
	}

	private static void showWindow(JFrame frame) {
		System.out.println("Take a break now!");

		// Habilita o botão somente alguns segundos depois, para evitar acionamentos acidentais.
		button.setEnabled(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5_000);
				} catch (InterruptedException e) {
				}
				button.setEnabled(true);
			}
		}).start();

		// Exibe a janela
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible( true );
	}

	private static void prepareWindow() {

		frame = new JFrame("FGEyeFatigueUtils");
		frame.setAlwaysOnTop(true);
		frame.setLocationByPlatform(true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(50, 50, 50, 50));
		panel.setLayout(new GridBagLayout());
		frame.add(panel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel label = new JLabel("Time to take a break!");
		panel.add(label, gbc);

		button = new JButton("OK, break is over");
		panel.add(button, gbc);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (monitor) {
					monitor.notify();
				}
			}
		});
	}
}
