package com.felipegiotto.spark;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.options;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.Gson;

import spark.ExceptionHandler;
import spark.ExceptionHandlerImpl;
import spark.ExceptionMapper;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.servlet.SparkApplication;

public abstract class FGSparkApplication implements SparkApplication {

	/**
	 * Método que deve ser sobrescrito pela aplicação. Aqui as rotas devem ser definidas.
	 */
	public abstract void afterInit();
	
	/**
	 * Método que deve ser sobrescrito pela aplicação e deve criar um objeto Gson, responsável
	 * pela serialização e deserialização dos dados entre frontend e backend.
	 * 
	 * @return
	 */
	public abstract Gson initGson();
	private Gson gson;
	
	@Override
	public void init() {
		
		// Callback disparado pelo Spark caso haja algum erro ao INICIAR APLICAÇÃO
		tratarErrosAoIniciarAplicacao();

		// Prepara objeto Gson, que tratará da serialização e deserialização de dados do frontend
		this.gson = initGson();

		// Callback disparado pelo Spark caso haja algum erro ao EXECUTAR UMA REQUISIÇÃO
		tratarErrosEmRequisicoes();

		this.afterInit();
	}

	/**
	 * Se houver algum erro ao INICIAR a aplicação, Spark chamará um callback
	 */
	public void tratarErrosAoIniciarAplicacao() {
		initExceptionHandler((e) -> {
			this.eventoErroAoIniciarAplicacao(e);
		});
	}

	/**
	 * Por padrão, se ocorrer algum erro ao INICIAR a aplicação, somente registra no log.
	 * Este método pode ser sobrescrito para aplicar tratamento adicional.
	 * 
	 * @param exception
	 */
	public void eventoErroAoIniciarAplicacao(Exception exception) {
		System.err.println("Erro ao iniciar aplicação");
		exception.printStackTrace();
	}
	
	/**
	 * Configura cabeçalhos CORS para permitir chamadas de qualquer local
	 */
	public void configurarFiltrosParaCabecalhoCORS() {
		
		// OBS: "after" não funcionou para o CORS
		// Fonte: https://gist.github.com/saeidzebardast/e375b7d17be3e0f4dddf
		before((Filter) (request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
		});
		
		options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}

			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}

			return "";
		});
	}
	
	/**
	 * Define um callback para quando ocorrer algum erro na chamada de algum serviço.
	 */
	public void tratarErrosEmRequisicoes() {
		
		// Solução de contorno para tratamento de exceptions (ver javadoc do método "sevletException")
		ExceptionHandler<? super Exception> handler = (exception, request, response) -> {
			eventoErroAoTratarRequisicao(exception, request, response);
		};
		exception(Exception.class, handler);
		sevletException(Exception.class, handler);
	}

	/**
	 * Por padrão, se houver algum erro em alguma requisição, grava no log e retorna um JSON
	 * com a mensagem. Este comportamento pode ser sobrescrito.
	 * 
	 * @param exception
	 * @param request
	 * @param response
	 */
	public void eventoErroAoTratarRequisicao(Exception exception, Request request, Response response) {
		System.err.println("Exception em " + request.uri());
		exception.printStackTrace();
		
		String mensagem = null;
		Throwable exceptionRecursive = exception;
		while (mensagem == null && exceptionRecursive != null) {
			mensagem = exception.getLocalizedMessage();
			exceptionRecursive = exceptionRecursive.getCause();
		}
		
		if (mensagem != null) {
			response.status(500);
			
			if (isHeaderAcceptJson(request)) {
				response.header("Content-type", "application/json; charset=UTF-8");
				Map<String, String> json = new HashMap<>();
				json.put("mensagem", mensagem);
				response.body(this.gson.toJson(json));
			} else {
				StringBuilder html = new StringBuilder();
				html.append("<h1>Erro</h1>\n");
				html.append("<h2>" + StringEscapeUtils.escapeHtml4(mensagem) + "</h2>\n");
				response.body(html.toString());
			}
		}
	}
	
	private boolean isHeaderAcceptJson(Request request) {
		String header = request.headers("Accept");
		return header != null && header.toLowerCase().contains("/json");
	}

	/**
	 * Solução de contorno para que o tratamento de exceptions funcione corretamente
	 * dentro do Tomcat. É preciso duplicar a rotina de tratamento em "exception" e
	 * "servletException"
	 * 
	 * Fonte: https://github.com/perwendel/spark/issues/1062
	 * 
	 * @param <T>
	 * @param exceptionClass
	 * @param handler
	 */
	public synchronized <T extends Exception> void sevletException(Class<T> exceptionClass,
			ExceptionHandler<? super T> handler) {
		// wrap
		ExceptionHandlerImpl<T> wrapper = new ExceptionHandlerImpl<T>(exceptionClass) {
			@Override
			public void handle(T exception, Request request, Response response) {
				handler.handle(exception, request, response);
			}
		};

		ExceptionMapper.getServletInstance().map(exceptionClass, wrapper);
	}
	
	public Gson getGson() {
		return gson;
	}
}
