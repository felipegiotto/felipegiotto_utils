package com.felipegiotto.utils;

public class FGHtmlUtils {

	public static String sanitizeToId(String id) {
		return id.replaceAll("]", "").replaceAll("[^a-zA-Z0-9\\-\\_]", "_");
	}
}
