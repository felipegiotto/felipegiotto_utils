package com.felipegiotto.utils.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.felipegiotto.utils.datasources.FGConnectionFactory;

/**
 * Classe para gerenciar configurações da aplicação.
 * 
 * Estas configurações ficam gravadas no banco de dados, na tabela "configurações".
 * 
 * Dados podem ser listados com "getConfiguracoes", consultandos individualmente com "get"
 * e gravados com "set"
 * 
 * @author felipegiotto@gmail.com
 */
public class FGConfiguracao {

	private String chave;
	private String valor;
	
	public static List<FGConfiguracao> getConfiguracoes() throws SQLException {
		try (Connection connection = FGConnectionFactory.getDefaultDataSource().getConnection()) {
			List<FGConfiguracao> lista = new ArrayList<>();
			
			try (ResultSet rs = connection.createStatement().executeQuery("SELECT chave, valor FROM configuracoes ORDER BY chave")) {
				while (rs.next()) {
					FGConfiguracao config = new FGConfiguracao();
					config.carregarDoResultSet(rs);
					lista.add(config);
				}
			}
			
			return lista;
		}
	}
	
	public static FGConfiguracao get(String chave) throws SQLException {
		try (Connection connection = FGConnectionFactory.getDefaultDataSource().getConnection()) {
			try (PreparedStatement ps = connection.prepareStatement("SELECT chave, valor FROM configuracoes WHERE chave = ?")) {
				ps.setString(1, chave);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						FGConfiguracao config = new FGConfiguracao();
						config.carregarDoResultSet(rs);
						return config;
					}
				}
			}
			
			return null;
		}
	}	
	
	public static void set(String chave, String valor) throws SQLException {
		try (Connection connection = FGConnectionFactory.getDefaultDataSource().getConnection()) {
			
			if (valor == null) {
				try (PreparedStatement psDelete = connection.prepareStatement("DELETE FROM configuracoes WHERE chave = ?")) {
					psDelete.setString(1, chave);
					psDelete.executeUpdate();
				}
			} else {
				try (PreparedStatement psUpdate = connection.prepareStatement("UPDATE configuracoes SET valor = ? WHERE chave = ?")) {
					psUpdate.setString(1, valor);
					psUpdate.setString(2, chave);
					if (psUpdate.executeUpdate() == 0) {
						try (PreparedStatement psInsert = connection.prepareStatement("INSERT INTO configuracoes (chave, valor) VALUES (?, ?)")) {
							psInsert.setString(1, chave);
							psInsert.setString(2, valor);
							psInsert.executeUpdate();
						}
					}
				}
			}
		}
	}
	
	private void carregarDoResultSet(ResultSet rs) throws SQLException {
		chave = rs.getString("chave");
		valor = rs.getString("valor");
	}
	
	public String getChave() {
		return chave;
	}
	
	public String getValor() {
		return valor;
	}
}
