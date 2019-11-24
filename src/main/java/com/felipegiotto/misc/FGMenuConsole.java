package com.felipegiotto.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

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
	private ColetorSubmenus coletor;
	
	public FGMenuConsole(String titulo, ColetorSubmenus coletor) {
		this.titulo = titulo;
		this.coletor = coletor;
	}
	
	public FGMenuConsole opcaoVoltar(String opcaoVoltar) {
		this.opcaoVoltar = opcaoVoltar;
		return this;
	}

	public boolean exibir() throws Exception {
		
		while(true) {
			
			// Coleta a próxima "rodada" de opções
			CriadorDeItens criador = new CriadorDeItens(this);
			this.coletor.coletar(criador);
			List<Item> itens = criador.itens;
			
			// Preenche e ajusta os atalhos
			Set<String> atalhosUtilizados = new TreeSet<>();
			int tamanhoMaiorAtalho = 0;
			for (Item item: itens) {
				
				// Se item tem atalho mas não tem código, tira o atalho
				if (item.codigo == null && item.atalho != null) {
					item.atalho = null;
				}
				
				// Se atalho já está sendo utilizado, remove.
				if (item.atalho != null && !atalhosUtilizados.add(item.atalho)) {
					item.atalho = null;
				}
				
				// Se algum item ficou sem atalho, grava um atalho numérico padrão
				if (item.codigo != null && item.atalho == null) {
					int atalho = 1;
					while (!atalhosUtilizados.add(Integer.toString(atalho))) {
						atalho++;
					}
					
					item.atalho = Integer.toString(atalho);
				}
				
				// Calcula a largura do maior atalho, para que todos os itens sejam exibidos alinhados verticalmente
				if (item.atalho != null) {
					tamanhoMaiorAtalho = Math.max(tamanhoMaiorAtalho, item.atalho.length() + 2);
				}
				
			}
			
			// Mostra o título do "menu"
			System.out.println("\n" + titulo);
			
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
			String resposta = FGInterativoUtils.aguardarRespostaUsuario("").toUpperCase();
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
				System.out.println("Opção inválida! Pressione ENTER");
				FGInterativoUtils.aguardarRespostaUsuario();
			}
		}
	}
	
	public boolean exibirSafe() {
		try {
			return exibir();
		} catch (Exception e) {
			e.printStackTrace();
			return false; // Retorna "true" para que continue executando, até que usuário realmente aborte
		}
	}
	
	public void exibirPersistente() throws Exception {
		while (exibirSafe()) {
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
	
	public class Item {
		String nome;
		String atalho;
		RunnableComException codigo;
		
		public Item nome(String nome) {
			this.nome = nome;
			return this;
		}
		
		public Item atalho(String atalho) {
			this.atalho = atalho != null ? atalho.toUpperCase() : null;
			return this;
		}
		
		public Item codigo(RunnableComException codigo) {
			this.codigo = codigo;
			return this;
		}
		
	}

	public interface RunnableComException {
		public void run() throws Exception;
	}
	
	public class CriadorDeItens {
		
		List<Item> itens = new ArrayList<>();
		
		FGMenuConsole menu;
		
		public CriadorDeItens(FGMenuConsole menu) {
			this.menu = menu;
		}
		
		public Item item() {
			Item item = new Item();
			this.itens.add(item);
			return item;
		}
		
		public Item item(String nome) {
			Item item = item();
			item.nome(nome);
			return item;
		}
		
		public Item item(String nome, RunnableComException codigo) {
			Item item = item(nome);
			item.codigo(codigo);
			return item;
		}

		public Item item(String atalho, String nome, RunnableComException codigo) {
			Item item = item(nome, codigo);
			item.atalho(atalho);
			return item;
		}
		
		public void opcaoVoltar(String opcaoVoltar) {
			menu.opcaoVoltar(opcaoVoltar);
		}
	}
	
	public interface ColetorSubmenus {
		public void coletar(CriadorDeItens criadorDeItens) throws Exception;
	}
}
