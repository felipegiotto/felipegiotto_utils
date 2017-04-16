package com.felipegiotto.utils.datasources;

import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;

/**
 * Servlet que pode ser estendido, em uma aplicação Web, para configurar automaticamente 
 * os DataSources da aplicação e migrar o banco de dados.
 * 
 * @author felipegiotto@gmail.com
 */
public class FGDatabaseInitServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LogManager.getLogger(FGDatabaseInitServlet.class);
	
	private boolean deveMigrarBanco;
	private String[] jdbcNames;
	
	public FGDatabaseInitServlet(boolean deveMigrarBanco, String... jdbcNames) throws NamingException, SQLException {
		this.deveMigrarBanco = deveMigrarBanco;
		this.jdbcNames = jdbcNames;
	}
	
	@Override
	public void init() throws ServletException {
		if (jdbcNames.length > 0) {
			try {
				FGConnectionFactory.loadFromWebContext(jdbcNames);
			} catch (NamingException e) {
				throw new ServletException(e);
			}
		}
		
		if (deveMigrarBanco) {
			try {
				migrarBancoDeDados();
			} catch (SQLException e) {
				throw new ServletException(e);
			}
		}
	}
	
	/**
	 * Fonte: https://flywaydb.org/getstarted/firststeps/api
	 * 
	 * @throws SQLException 
	 */
	public void migrarBancoDeDados() throws SQLException {
		
		LOGGER.debug("Iniciando migração do banco de dados...");
		
		// Create the Flyway instance
        Flyway flyway = new Flyway();

        // Point it to the database
        flyway.setDataSource(FGConnectionFactory.getDefaultDataSource());

        // Start the migration
        flyway.migrate();
        
		LOGGER.debug("Migração finalizada!");
	}

	@Override
	public void destroy() {
		FGConnectionFactory.close();
	}
}
