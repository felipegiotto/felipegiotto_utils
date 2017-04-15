package com.felipegiotto.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
}
