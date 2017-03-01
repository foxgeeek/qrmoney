package controllers;

import play.mvc.Before;
import play.mvc.Controller;

public class ControllerSeguranca extends Controller{

	@Before(only={"Sistema.index","Sistema.profile","Sistema.registration_cliente","Sistema.invoice","Sistema.invoice_print","Sistema.remover"})
	static void verificarVendedor(){
		if(!session.contains("vendedor"))
			Sistema.login();
	}
}
