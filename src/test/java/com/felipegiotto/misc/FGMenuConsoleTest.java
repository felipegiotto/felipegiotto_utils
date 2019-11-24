package com.felipegiotto.misc;

public class FGMenuConsoleTest {

	public static void main(String[] args) {
		new FGMenuConsole("Teste título", (menu) -> {
			menu.item("Item foo", () -> System.out.println("FOO"));
			menu.item("Item bar", () -> System.out.println("BAR")).atalho("BAR");
			menu.item("Separador bla bla2");
			menu.item().nome("Separador bla bla");
			menu.opcaoVoltar("Cancelar");
		}).exibirPersistenteSafe();
	}
}
