package controllers;

import models.Vendedor;
import models.negocio.GerenciadorSessao;
import play.mvc.Controller;

public class ControllerLogins extends Controller {
	
	//PÁGINA DE LOGIN
	public static void login() {
		Sistema.login();
	}
	
	//FAZ LOGOUT DA CONTA
	public static void logoff() {
		session.clear();
		Sistema.login();
	}
	
	//LOGANDO COM VENDEDOR
	public static void logar(Vendedor vendedor){
		
                //BUSCANDO VENDEDOR POR USUÁRIO E SENHA
		Vendedor v = Vendedor.find("byUsuarioAndSenha", vendedor.usuario, vendedor.senha).first();
		
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