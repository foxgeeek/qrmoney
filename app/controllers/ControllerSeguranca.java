package controllers;

import play.mvc.Before;
import play.mvc.Controller;

public class ControllerSeguranca extends Controller{

	//AUTORIZAÇÃO PARA ACESSO DAS PÁGINAS - ANTES DE QUALQUER REDIRECT VERIFICA SE TEM VENDEDOR LOGADO
        @Before(only={"Sistema.index","Sistema.config","Sistema.imprimirBoleto","Sistema.adicionarCredito","Sistema.removerCredito","Sistema.cadastraCliente","Sistema.lojas","Sistema.mudarAceite","Sistema.gerenciaboleto","Sistema.registro","Sistema.profile","Sistema.registration_cliente","Sistema.invoice","Sistema.invoice_print","Sistema.remover"})
	static void verificarVendedor(){
            if(!session.contains("vendedor"))
                Sistema.login();
	}
}
