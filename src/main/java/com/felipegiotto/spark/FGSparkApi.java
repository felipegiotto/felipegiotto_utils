package com.felipegiotto.spark;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import spark.Request;

public class FGSparkApi {

	protected FGSparkApplication application;
	
	public FGSparkApi(FGSparkApplication app) {
		this.application = app;
	}
	
	/**
	 * Converte um objeto para JSON, utilizando o objeto "gson" da aplicação
	 * 
	 * @param object
	 * @return
	 */
	public String toJson(Object object) {
		return this.application.getGson().toJson(object);
	}
	
	/**
	 * Lê um objeto do BODY de uma requisição.
	 * 
	 * @param request : requisição de onde o objeto será lido
	 * @param classOfT : classe do objeto que será instanciado.
	 * @return
	 */
	public <T> T fromJsonBody(Request request, Class<T> classOfT) {
		return this.application.getGson().fromJson(request.body(), classOfT);
	}
	
	/**
	 * Carrega um parâmetro do tipo "long" da URL de uma requisição (ex: "/agendamentos/:id").
	 * 
	 * O parâmetro é obrigatório (já que faz parte da URL). Pode causar um erro no "parseLong" se não existir.
	 * 
	 * @param paramName
	 * @param request
	 * @return
	 */
	public long urlParamLong(String paramName, Request request) {
		return Long.parseLong(request.params(paramName));
	}
	
	/**
	 * Carrega um "query param" do tipo "int"
	 * 
	 * @param paramName
	 * @param request
	 * @return o valor do parâmetro (se for informado), NULL (se não for informado) ou exception se for informado um valor não numérico.
	 */
	public Integer queryParamInt(String paramName, Request request) {
		String param = request.queryParams(paramName);
		return param != null ? Integer.parseInt(param) : null;
	}
	
	/**
	 * Carrega um "query param" do tipo "boolean", mas com valores de entrada "1" ou "0".
	 * 
	 * @param paramName
	 * @param request
	 * @return "true" se parâmetro = "1", NULL se parâmetro não for informado, "false" caso contrário.
	 */
	public Boolean queryParamIntBoolean(String paramName, Request request) {
		String param = request.queryParams(paramName);
		return param != null ? "1".equals(param) : null;
	}
	
	/**
	 * Carrega um "query param" do tipo "boolean", mas com valores de entrada "1" ou "0", permitindo informar valor padrão caso não exista.
	 * 
	 * @param paramName
	 * @param request
	 * @param padrao
	 * @return "true" se parâmetro = "1", parâmetro "padrao" se parâmetro não for informado, "false" caso contrário.
	 */
	public boolean queryParamIntBoolean(String paramName, Request request, boolean padrao) {
		String param = request.queryParams(paramName);
		return param != null ? "1".equals(param) : padrao;
	}
	
	public Gson getGson() {
		return application.getGson();
	}
}
