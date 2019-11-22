package com.felipegiotto.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.felipegiotto.utils.FGInterativoUtils;

/**
 * Classe que auxilia na exibição de menus interativos em um console, apresentando opções ao usuário e permitindo realizar ações.
 *
 * @author felipegiotto@gmail.com
 */
public class FGMenuConsole {

	private String titulo;
	private String opcaoVoltar = "(Voltar)";
	private List<Item> itens = new ArrayList<>();
	
	public FGMenuConsole(String titulo) {
		this.titulo = titulo;
	}
	
	public FGMenuConsole item(String atalho, String nome, RunnableComException codigo) {
		Item item = new Item();
		item.nome = nome;
		
		if (codigo != null && atalho != null) {
			item.codigo = codigo;
			item.atalho = atalho.toUpperCase();
		}
		
		this.itens.add(item);
		return this;
	}
	
	public FGMenuConsole item(String nome, RunnableComException codigo) {
		
		String atalho = null;
		if (codigo != null) {
			atalho = retornarProximoNumeroLivre();
		}
		return item(atalho, nome, codigo);
	}
	
	public FGMenuConsole opcaoVoltar(String opcaoVoltar) {
		this.opcaoVoltar = opcaoVoltar;
		return this;
	}
	
	private String retornarProximoNumeroLivre() {
		final AtomicInteger i = new AtomicInteger(1);
		while (this.itens.stream().anyMatch(item -> i.toString().equals(item.atalho))) {
			i.incrementAndGet();
		}
		return i.toString();
	}

	public boolean exibir() throws Exception {
		
		while(true) {
			
			// Mostra o título do "menu"
			System.out.println("\n" + titulo);
			
			// Calcula a largura do maior atalho, para que todos os itens sejam exibidos alinhados verticalmente
			int tamanhoMaiorAtalho = 0;
			for (Item item: itens) {
				if (item.atalho != null) {
					tamanhoMaiorAtalho = Math.max(tamanhoMaiorAtalho, item.atalho.length() + 2);
				}
			}
			
			// Exibe todos os itens
			for (int i=0; i<itens.size(); i++) {
				Item item = itens.get(i);
				String atalho = item.atalho != null ? item.atalho + ": " : "";
				System.out.println(StringUtils.rightPad(atalho, tamanhoMaiorAtalho) + item.nome);
			}
			
			// Exibe o último item (opção para voltar)
			System.out.println(StringUtils.rightPad("0: ", tamanhoMaiorAtalho) + opcaoVoltar);
			
			// Aguarda usuário selecionar uma opção
			System.out.print("Selecione uma opção: ");
			String resposta = FGInterativoUtils.aguardarRespostaUsuario().toUpperCase();
			if ("0".equals(resposta)) {
				return false;
			}
			
			try {
				
				// Busca, dentre os itens, o que o usuário selecionou (pelo seu atalho).
				// Se usuário digitou valor inválido, será lançado um "NoSuchElementException" e retornará ao início.
				Item itemSelecionado = itens.stream().filter((item) -> resposta.equals(item.atalho)).findFirst().get();
				RunnableComException runnable = itemSelecionado.codigo;
				if (runnable != null) {
					runnable.run();
				}
				return true;
			} catch (NoSuchElementException ex) {
				System.out.println("Opção inválida!");
			}
		}
	}
	
	public void exibirSafe() {
		try {
			exibir();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void exibirPersistente() throws Exception {
		while (exibir()) {
			// Retornará automaticamente quando usuário selecionar "Voltar"
		}
	}
	
	public void exibirPersistenteSafe() {
		try {
			exibirPersistente();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class Item {
		String atalho;
		String nome;
		RunnableComException codigo;
	}

	public interface RunnableComException {
		public void run() throws Exception;
	}	
}
