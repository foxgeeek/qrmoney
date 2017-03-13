package controllers;

import java.util.List;

import models.Cliente;
import models.Vendedor;
import models.negocio.GerenciadorSessao;
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
		Vendedor v = Vendedor.find("email", vendedor.email).first();
		
		if(v == null){
			String mensagem = "Por favor, verifique se inseriu usuário/senha corretos!";
			flash.success(mensagem);
			Sistema.login();
		}else{
			//INSERINDO DADOS NA SESSÃO
                        GerenciadorSessao.sessaoLogin(session, v);
			Sistema.index();
		}
	}
}