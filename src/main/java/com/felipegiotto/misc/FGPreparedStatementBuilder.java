package com.felipegiotto.misc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que auxilia na criação de PreparedStatements informando, de uma só vez, os pedaços de SQL e seus parâmetros.
 * 
 * Ex:
 <code>
 	FGPreparedStatementBuilder builder = new FGPreparedStatementBuilder();
 	builder.append("SELECT * FROM pessoas ");
 	builder.append("WHERE nome = ? ", nome);
 	builder.append("  AND idade = ? ", idade);
 	try (PreparedStatement ps = builder.build(connection)) {
 		try (ResultSet rs = ps.executeQuery()) {
 			// ...
 		}
 	}
 </code>
 * @author felipegiotto@gmail.com
 */
public class FGPreparedStatementBuilder {

	private StringBuilder sql = new StringBuilder();
	private List<Object> params = new ArrayList<>();
	
	public void append(String sql, Object... params) {
		this.sql.append(sql);
		for (Object param: params) {
			this.params.add(param);
		}
	}
	
	public PreparedStatement build(Connection connection) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql.toString());
		try {
			
			int index = 1;
			for (Object param : params) {
				if (param instanceof String) {
					ps.setString(index++, (String) param);
					
				} else if (param instanceof Boolean) {
					ps.setBoolean(index++, (Boolean) param);
					
				} else if (param instanceof Float) {
					ps.setFloat(index++, (Float) param);
					
				} else if (param instanceof Integer) {
					ps.setInt(index++, (Integer) param);
					
				} else if (param instanceof Long) {
					ps.setLong(index++, (Long) param);
					
				} else {
					throw new SQLException("Tipo de dado ainda não implementado em FGPreparedStatementBuilder: " + (param != null ? param.getClass() : "null"));
				}
			}
			return ps;
			
		} catch (SQLException ex) {
			ps.close();
			throw ex;
		}
	}
}
