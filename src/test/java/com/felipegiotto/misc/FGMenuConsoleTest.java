package com.felipegiotto.misc;

public class FGMenuConsoleTest {

	public static void main(String[] args) {
		new FGMenuConsole("Teste título")
			.item("Item foo", () -> { System.out.println("Executado Item foo"); })
			.item("Nao faço nada", null)
			.item("Item bar", () -> { System.out.println("Executado Item bar"); })
			.item("Item bar", () -> { System.out.println("Executado Item bar"); })
			.item("Item bar", () -> { System.out.println("Executado Item bar"); })
			.item("Item bar", () -> { System.out.println("Executado Item bar"); })
			.item("Item bar", () -> { System.out.println("Executado Item bar"); })
			.item("Item bar", () -> { System.out.println("Executado Item bar"); })
			.item("a", "Item baraaa", () -> { System.out.println("Executado Item baraaa"); })
			.item("abbb", "Item abbb", () -> { System.out.println("Executado Item abbb"); })
			.item("Item bar", () -> { System.out.println("Executado Item bar"); })
			.opcaoVoltar("Voltaaaaar")
			.exibirSafe();
	}
}
