package com.felipegiotto.utils;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

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
public class FGEyeFatigueUtils {

	private static Object monitor = new Object();
	
	public static void main(String[] args) throws InterruptedException {
		
        JFrame frame = prepareWindow();
		
        while (true) {
        	for (int i=20; i>0; i--) {
        		System.out.println("Next break in " + i + "m");
        		Thread.sleep(60_000);
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

	private static void showWindow(JFrame frame) {
		System.out.println("Take a break now!");
		frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible( true );
	}

	private static JFrame prepareWindow() {
		
		JFrame frame = new JFrame("FGEyeFatigueUtils");
        frame.setAlwaysOnTop( true );
        frame.setLocationByPlatform( true );
        frame.setLayout(new FlowLayout(FlowLayout.LEADING));
        
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));
        panel.setLayout(new GridBagLayout());
        frame.add(panel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel label = new JLabel("Time to take a break!");
        panel.add(label, gbc);
        
		JButton button = new JButton("OK, break is over");
		panel.add(button, gbc);
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (monitor) {
					monitor.notify();
				}
			}
		});
		return frame;
	}
}
