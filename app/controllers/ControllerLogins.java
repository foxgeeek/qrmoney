package controllers;

import java.util.List;

import models.Cliente;
import models.Vendedor;
import play.cache.Cache;
import play.mvc.Controller;

public class ControllerLogins extends Controller {
	
	//LOGIN
	public static void login() {
		Sistema.login();
	}
	
	//LOGOFF
	public static void logoff() {
		session.clear();
		Sistema.login();
	}
	
	//LOGANDO COM VENDEDOR
	public static void logar(Vendedor vendedor){
		//BUSCANDO VENDEDOR POR USUÁRIO E SENHA
		Vendedor v = Vendedor.find("byUsuarioAndSenha AND byAccess_token", vendedor.usuario, vendedor.senha, vendedor.access_token).first();
		
		if(v == null){
			String mensagem = "Por favor, verifique se inseriu usuário/senha corretos!";
			flash.success(mensagem);
			Sistema.login();
		}else{
			//INSERINDO DADOS NA SESSÃO
			session.put("vendedor_id", v.id);
			session.put("vendedor", "logado");
			session.put("vendedor_nome", v.nome);
			session.put("credito_antigo", "0,00");
			session.put("debito_antigo", "0,00");
			Sistema.index(null);
		}
	}
}