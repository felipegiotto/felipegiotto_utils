package com.felipegiotto.misc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.felipegiotto.utils.FGInterativoUtils;
import com.felipegiotto.utils.FGOperatingSystemUtils;

/**
 * Classe utilizada para evitar fadiga ocular utilizando a técnica do "20-20-20" 
 * (a cada 20 minutos olhar para algo a 20 pés de distância durante 20 segundos).
 * 
 * Instruções: Executar a classe e fazer uma pausa quando notificado!
 * 
 * Fonte: https://abertoatedemadrugada.com/2014/02/evita-fadiga-ocular-em-frente-ao-pc-com.html
 * 
 * @author felipegiotto@gmail.com
 */
public class FGEyeFatigueMonitor {

	private static final int TEMPO_EM_MINUTOS_PARA_REINICIAR_CONTAGEM_COM_PROTETOR_DE_TELA_ATIVO = 2;
	
	private int minutesBetweenPauses;
	private Object monitor = new Object();
	private JFrame frame;
	private JButton button;

	public static void main(String[] args) throws InterruptedException {
		int minutos = FGInterativoUtils.perguntarNumeroInteiroParaUsuario("Qual o intervalo (em minutos) entre cada pausa?", false, (numero) -> numero > 0);
		FGEyeFatigueMonitor m = new FGEyeFatigueMonitor(minutos);
		m.start();
	}

	public FGEyeFatigueMonitor(int minutesBetweenPauses) {
		this.minutesBetweenPauses = minutesBetweenPauses;
	}
	
	private void start() throws InterruptedException {
		prepareWindow();

		int minutosProtetorTelaAtivo = 0;
		while (true) {
			for (int i=minutesBetweenPauses; i>0; i--) {
				System.out.println(LocalDateTime.now() + ": Next break in " + i + "m");
				
				long antes = System.currentTimeMillis();
				Thread.sleep(60_000);
				long depois = System.currentTimeMillis();
				
				// Pode ser que o computador tenha sido suspenso neste intervalo.
				// Aplica a mesma regra do protetor de tela: se ficou suspenso por muito tempo, reinicia
				if ((depois - antes) / 60_000 > TEMPO_EM_MINUTOS_PARA_REINICIAR_CONTAGEM_COM_PROTETOR_DE_TELA_ATIVO) {
					i = minutesBetweenPauses;
					System.out.println(LocalDateTime.now() + ": Computador foi suspenso. Contagem reiniciada.");
				} else {

					// Conta há quantos minutos o protetor de tela está ativo
					try {
						if (FGOperatingSystemUtils.isScreensaverActive()) {
							minutosProtetorTelaAtivo++;
						} else {
							minutosProtetorTelaAtivo = 0;
						}
					} catch (RuntimeException ex) {
						// Sistema operacional desconhecido
					}
	
					// Se protetor de tela estiver ativo há mais de 5 minutos, reinicia a contagem do tempo
					if (minutosProtetorTelaAtivo > TEMPO_EM_MINUTOS_PARA_REINICIAR_CONTAGEM_COM_PROTETOR_DE_TELA_ATIVO) {
						i = minutesBetweenPauses;
						System.out.println(LocalDateTime.now() + ": Protetor de tela ativo há " + minutosProtetorTelaAtivo + "m. Contagem reiniciada.");
					}
				}
			}
			showWindow(frame);
			synchronized (monitor) {
				monitor.wait();
			}
			hideWindow(frame);
		}
	}

	private static void hideWindow(JFrame frame) {
		frame.setVisible(false);
	}

	private void showWindow(JFrame frame) {
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

	private void prepareWindow() {

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
