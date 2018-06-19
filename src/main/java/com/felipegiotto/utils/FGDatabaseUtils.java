package com.felipegiotto.utils;

import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;

import com.felipegiotto.utils.datasources.FGConnectionFactory;
import com.felipegiotto.utils.exception.NotFoundException;
/**
 * Métodos auxiliares referentes a bancos de dados
 * 
 * @author felipegiotto@gmail.com
 */
public class FGDatabaseUtils {

	private static final Logger LOGGER = LogManager.getLogger(FGDatabaseUtils.class);
	
	/**
	 * Fecha todos os objetos recebidos por parametro, chamando o metodo "close()".
	 * Qualquer exceção será logada e suprimida.
	 * 
	 * Esse método pode ser utilizado com objetos do tipo "Connection", "ResultSet",
	 * "Statement", "PreparedStatement" e qualquer outro que possua o método "close()".
	 *  
	 * @param objects
	 */
	public static void closeAllSilently(Object... objects) {
		for (Object object: objects) {
			if (object != null) {
				try {
					Method closeMethod = object.getClass().getMethod("close");
					closeMethod.invoke(object);
				} catch (InvocationTargetException ex) {
					LOGGER.warn("Error invoking 'close' on " + object + ": " + ex.getTargetException().getLocalizedMessage(), ex);
				} catch (Exception ex) {
					LOGGER.error("Error trying to invoke 'close' on " + object + ": " + ex.getLocalizedMessage(),  ex);
				}
			}
		}
	}

	/**
	 * Lê um campo "double" de um ResultSet, retornando NULL quando necessário.
	 * 
	 * @param rs
	 * @param fieldName
	 * @return
	 * @throws SQLException
	 */
	public static Double getDoubleOrNull(ResultSet rs, String fieldName) throws SQLException {
		Double value = rs.getDouble(fieldName);
		if (rs.wasNull()) {
			return null;
		} else {
			return value;
		}
	}

	
	/**
	 * Seta um parâmetro Double em um PreparedStatement, utilizando "setNull" quando necessário
	 * 
	 * @param ps
	 * @param parameterIndex
	 * @param value
	 * @throws SQLException
	 */
	public static void setDoubleOrNull(PreparedStatement ps, int parameterIndex, Double value) throws SQLException {
		if (value == null) {
			ps.setNull(parameterIndex, Types.DOUBLE);
		} else {
			ps.setDouble(parameterIndex, value);
		}
	}
	
	
	/**
	 * Lê um campo "long" de um ResultSet, retornando NULL quando necessário.
	 * 
	 * @param rs
	 * @param fieldName
	 * @return
	 * @throws SQLException
	 */
	public static Long getLongOrNull(ResultSet rs, String fieldName) throws SQLException {
		Long value = rs.getLong(fieldName);
		if (rs.wasNull()) {
			return null;
		} else {
			return value;
		}
	}
	
	
	/**
	 * Seta um parâmetro Long em um PreparedStatement, utilizando "setNull" quando necessário
	 * 
	 * @param ps
	 * @param parameterIndex
	 * @param value
	 * @throws SQLException
	 */
	public static void setLongOrNull(PreparedStatement ps, int parameterIndex, Long value) throws SQLException {
		if (value == null) {
			ps.setNull(parameterIndex, Types.BIGINT);
		} else {
			ps.setLong(parameterIndex, value);
		}
	}
	
	
	/**
	 * Lê um campo "int" de um ResultSet, retornando NULL quando necessário.
	 * 
	 * @param rs
	 * @param fieldName
	 * @return
	 * @throws SQLException
	 */
	public static Integer getIntOrNull(ResultSet rs, String fieldName) throws SQLException {
		Integer value = rs.getInt(fieldName);
		if (rs.wasNull()) {
			return null;
		} else {
			return value;
		}
	}
	
	
	/**
	 * Seta um parâmetro Integer em um PreparedStatement, utilizando "setNull" quando necessário
	 * 
	 * @param ps
	 * @param parameterIndex
	 * @param value
	 * @throws SQLException
	 */
	public static void setIntOrNull(PreparedStatement ps, int parameterIndex, Integer value) throws SQLException {
		if (value == null) {
			ps.setNull(parameterIndex, Types.INTEGER);
		} else {
			ps.setInt(parameterIndex, value);
		}
	}
	
	
	/**
	 * Seta um parâmetro Boolean em um PreparedStatement, utilizando "setNull" quando necessário
	 * 
	 * @param ps
	 * @param parameterIndex
	 * @param value
	 * @throws SQLException
	 */
	public static void setBooleanOrNull(PreparedStatement ps, int parameterIndex, Boolean value) throws SQLException {
		if (value == null) {
			ps.setNull(parameterIndex, Types.BOOLEAN);
		} else {
			ps.setBoolean(parameterIndex, value);
		}
	}
	
	/**
	 * Executa uma consulta SQL e retorna o primeiro campo do 
	 * primeiro registro do ResultSet, no formato Integer.
	 * 
	 * @param conn
	 * @param sql
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public static Integer executeQueryInt(Connection conn, String sql) throws NotFoundException, SQLException {
		try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
			if (rs.next()) {
				int value = rs.getInt(1);
				if (rs.wasNull()) {
					return null;
				} else {
					return value;
				}
			} else {
				throw new NotFoundException("Record not found");
			}
		}
	}
	
	/**
	 * Executa um PreparedStatement e retorna o primeiro campo do 
	 * primeiro registro do ResultSet, no formato Integer.
	 * 
	 * @param conn
	 * @param sql
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public static Integer executeQueryInt(PreparedStatement ps) throws NotFoundException, SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				int value = rs.getInt(1);
				if (rs.wasNull()) {
					return null;
				} else {
					return value;
				}
			} else {
				throw new NotFoundException("Record not found");
			}
		}
	}
	
	/**
	 * Executa uma consulta SQL e retorna o primeiro campo do 
	 * primeiro registro do ResultSet, no formato Long.
	 * 
	 * @param conn
	 * @param sql
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public static Long executeQueryLong(Connection conn, String sql) throws NotFoundException, SQLException {
		try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
			if (rs.next()) {
				long value = rs.getLong(1);
				if (rs.wasNull()) {
					return null;
				} else {
					return value;
				}
			} else {
				throw new NotFoundException("Record not found");
			}
		}
	}
	
	/**
	 * Executa um PreparedStatement e retorna o primeiro campo do 
	 * primeiro registro do ResultSet, no formato Long.
	 * 
	 * @param conn
	 * @param sql
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public static Long executeQueryLong(PreparedStatement ps) throws NotFoundException, SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				long value = rs.getLong(1);
				if (rs.wasNull()) {
					return null;
				} else {
					return value;
				}
			} else {
				throw new NotFoundException("Record not found");
			}
		}
	}
	
	/**
	 * Executa uma consulta SQL e retorna o primeiro campo do 
	 * primeiro registro do ResultSet, no formato Integer.
	 * 
	 * Pega uma conexão do "default data source" e fecha depois
	 * da consulta.
	 * 
	 * @param sql
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public static Integer executeQueryInt(String sql) throws NotFoundException, SQLException {
		try (Connection conn = FGConnectionFactory.getDefaultDataSource().getConnection()) {
			return executeQueryInt(conn, sql);
		}
	}
	
	/**
	 * Executa uma consulta SQL e retorna o primeiro campo do 
	 * primeiro registro do ResultSet, no formato Long.
	 * 
	 * Pega uma conexão do "default data source" e fecha depois
	 * da consulta.
	 * 
	 * @param sql
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public static Long executeQueryLong(String sql) throws NotFoundException, SQLException {
		try (Connection conn = FGConnectionFactory.getDefaultDataSource().getConnection()) {
			return executeQueryLong(conn, sql);
		}
	}
	
	/**
	 * Executa uma operação DML, pegando uma conexão do 
	 * "default data source". Ao final, fecha a conexão.
	 * 
	 * @param sql
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public static Integer executeUpdate(String sql) throws SQLException {
		try (Connection conn = FGConnectionFactory.getDefaultDataSource().getConnection()) {
			return conn.createStatement().executeUpdate(sql);
		}
	}
	
	public static Integer executeUpdate(Connection conn, String sql) throws SQLException {
		return conn.createStatement().executeUpdate(sql);
	}
	
	/**
	 * Migra o banco de dados padrão, aplicando scripts de atualização conforme documentação do Flyway.
	 * 
	 * Resumo:
	 * 1. Criar pasta "src/main/resources/db/migration"
	 * 2. Criar scripts com número da versão e descrição, ex: "V1__Criar tabela X.sql".
	 * 
	 * @throws SQLException
	 */
	public static void migrateDatabase() throws SQLException {
		
		// Create the Flyway instance
        Flyway flyway = new Flyway();

        // Point it to the database
        flyway.setDataSource(FGConnectionFactory.getDefaultDataSource());

        // Start the migration
        flyway.migrate();

	}
}
