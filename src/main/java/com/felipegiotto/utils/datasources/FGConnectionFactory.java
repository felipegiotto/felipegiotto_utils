package com.felipegiotto.utils.datasources;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.felipegiotto.utils.FGDatabaseUtils;

/**
 * Classe que gerencia o pool de conexões da aplicação, que pode ser configurado:
 * 1. Através de JNDI, declarando uma tag <Resource .../> no arquivo context.xml
 *    Ver método "loadFromWebContext"
 * 2. Manualmente, para facilitar testes unitários e execução direta de classes 
 *    pela IDE
 *    
 * @author felipegiotto@gmail.com
 */
public class FGConnectionFactory {

	private static final Logger LOGGER = LogManager.getLogger(FGConnectionFactory.class);
	
	private static Context ctx;
	private static Map<String, DataSource> dsMap = new HashMap<>();
	private static String defaultJdbcName;
	
	public static void loadFromWebContext(String... jdbcNames) throws NamingException {
		
		LOGGER.debug("Iniciando Datasources...");
		ctx = new InitialContext();
		for (String name: jdbcNames) {
			LOGGER.debug("* " + name);
			try {
				DataSource ds = (DataSource) ctx.lookup("java:/comp/env/" + name);
				gravarDataSourceNoCache(name, ds);
			} catch (NamingException ex) {
				LOGGER.error("Não foi encontrada configuração do DataSource '" + name + "'. Edite o arquivo 'context.xml' e adicione uma linha neste formato: \n<Resource name=\"jdbc/template_webapp\" \n   auth=\"Container\" \n   type=\"javax.sql.DataSource\" \n   driverClassName=\"org.sqlite.JDBC\" \n   url=\"jdbc:sqlite:/path/to/database.sqlite3\" />.");
				LOGGER.error("Se estiver utilizando o Eclipse, o arquivo context.xml deverá estar na pasta 'workspace/Servers/[nome do servidor] at localhost-config'");
				LOGGER.error("Exceção original: " + ex.getLocalizedMessage(), ex);
				throw ex;
			}
		}
	}

	/**
	 * Fonte: https://examples.javacodegeeks.com/core-java/apache/commons/dbcp/basicdatasource/create-a-simple-basicdatasource-object/
	 * 
	 * @param jdbcName
	 * @throws NamingException
	 */
	public static void addManualDataSource(String jdbcName, String driverClassName, String url) throws NamingException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(driverClassName);
		ds.setUrl(url);
		
		gravarDataSourceNoCache(jdbcName, ds);
	}
	
	private static void gravarDataSourceNoCache(String name, DataSource ds) {
		dsMap.put(name, ds);

		if (defaultJdbcName == null) {
			defaultJdbcName = name;
		}
	}
	
	public static DataSource getDataSourceByJndiName(String jdbcName) throws SQLException {
		if (!dsMap.containsKey(jdbcName)) {
			throw new SQLException("Não foi configurado DataSource com nome " + jdbcName);
		}
		return dsMap.get(jdbcName);
	}
	
	public static DataSource getDefaultDataSource() throws SQLException {
		
		if (defaultJdbcName == null) {
			throw new SQLException("Nenhum DataSource foi configurado. Informe algum DataSource com os métodos 'FGConnectionFactory.addManualDataSource' ou 'FGConnectionFactory.loadFromWebContext'");
		}
		return getDataSourceByJndiName(defaultJdbcName);
	}
	
	public static void close() {
		
		LOGGER.debug("Finalizando Datasources...");
		dsMap.clear();
		dsMap = null;
		
		if (ctx != null) {
			FGDatabaseUtils.closeAllSilently(ctx);
			ctx = null;
		}
	}
}
