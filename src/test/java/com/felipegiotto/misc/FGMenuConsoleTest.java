package com.felipegiotto.misc;

public class FGMenuConsoleTest {

	public static void main(String[] args) throws Exception {
		new FGMenuConsole("Teste título", (menu) -> {
			menu.item("Foo", () -> { 
				System.out.println("Executado FOO");
				throw new RuntimeException("Erro proposital");
			});
			menu.item("----------------");
			menu.item("Bar", () -> System.out.println("Executado BAR")).atalho("BAR");
			menu.opcaoVoltar("Cancelar");
		}).exibirPersistente();
	}
}
