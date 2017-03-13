package controllers;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import models.Status;
import models.Vendedor;
import models.Cliente;
import models.Conta;
import models.negocio.GerenciadorSessao;
import play.mvc.Controller;
import play.mvc.With;

@With(ControllerSeguranca.class)
public class Sistema extends Controller {
	
	//AÇÃO LOGIN - DIRECIONA O USUÁRIO PARA TELA DE LOGIN
	public static void login(){
		render();
	}
	
	//AÇÃO REGISTRATION - PARA REGISTRAR/CADASTRAR VENDEDORES
	public static void registration(Long id){
		if (id == null) {
			render();
		}
		Vendedor vendedor = Vendedor.findById(id);
		render(vendedor);
	}
	
	//AÇÃO REGISTRATION_CLIENTE - PARA REGISTRAR/CADASTRAR CLIENTES
	public static void registration_cliente(Long id){
		if (id == null) {
			render();
		}
		Cliente cliente = Cliente.findById(id);
		render(cliente);
	}
	
	//AÇÃO INVOICE - CHAMA PÁGINA PARA GERAÇÃO DO QRCODE DO USUÁRIO NA CARTEIRA VIRTUAL
	public static void invoice(Long id){
		Cliente cliente = Cliente.findById(id);
		render(cliente);
	}
	
	//AÇÃO INVOICE_PRINT - CHAMA PÁGINA PARA IMPRESSÃO DA CARTEIRA VIRTUAL
	public static void invoice_print(Long id){
		Cliente cliente = Cliente.findById(id);
		render(cliente);
	}
	
	//AÇÃO CADASTRAVENDEDOR - RECEBE DADOS DE UM FORMULÁRIO PARA CADASTRO DO VENDEDOR
	public static void cadastraVendedor(Vendedor vendedor){
            vendedor.status = Status.ATIVO;
            vendedor.termo = Status.ACEITO;
            vendedor.save();
            String mensagem = "Vendedor cadastrado com sucesso!";
            flash.success(mensagem);
            GerenciadorSessao.sessaoLogin(session, vendedor);
            Sistema.index();
	}
	
	//AÇÃO CADASTRACLIENTE - RECEBE DADOS DE UM FORMULÁRIO PARA CADASTRO DO CLIENTE
	public static void cadastraCliente(Long id, Cliente cliente){
		Vendedor v = Vendedor.findById(id);
		cliente.status = Status.ATIVO;
		cliente.termo = Status.ACEITO;
		cliente.vendedor = v;
		cliente.save();
		
		String mensagem = "Cliente cadastrado com sucesso!";
		flash.success(mensagem);
		index();
	}
	
	//AÇÃO INDEX - CHAMA A PÁGINA PRINCIPAL COM TODAS INFORMAÇÕES NECESSÁRIAS
	public static void index(){
		List<Cliente> clientes = Collections.emptyList();
                List<Conta> contas = Collections.emptyList();
		clientes = Cliente.find("status = ? AND vendedor_fk = ?",Status.ATIVO, Vendedor.findById(Long.parseLong(session.get("vendedor_id")))).fetch();
		int registros = (int) Cliente.count("status = ?",Status.ATIVO);
		int compras = (int) Conta.count("status = ?", Status.COMPRA);
		render(clientes,registros,compras,contas);
	}
	
	//AÇÃO REMOVER - REMOVE UM CLIENTE A PARTIR DO SEU ID
	public static void remover(Long id){
		Cliente cliente = Cliente.findById(id);
		cliente.status = Status.INATIVO;
		cliente.save();
		
		String mensagem = "Cliente removido com sucesso";
		flash.success(mensagem);
		index();
	}
	
	//AÇÃO REMOVERCREDITO - DEBITA A CONTA DE UM DETERMINADO CLIENTE
	public static void removerCredito(Long id, Conta conta, Cliente cliente){
		session.put("cliente_id", cliente.id);
		cliente.findById(Long.parseLong(session.get("cliente_id")));
		conta.find("cliente_id = ?", Long.parseLong(session.get("cliente_id")));
		
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat data = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		conta.data = data.format(now);
		
		session.put("debito_antigo", conta.debito);
		
		Double credito_anterior = Double.parseDouble(session.get("credito_antigo").replace(",","."));//10.0
		Double debitar = Double.parseDouble(session.get("debito_antigo").replace(",",".")); //10.0
		Double credito_total = credito_anterior - debitar; //10.0 + 10.0 = 20.0
                String c = String.valueOf(credito_total).format("%.2f",credito_total);
                Double credito = Double.parseDouble(c.replace(",","."));
                if(debitar > credito){
                    String mensagem = "Cliente não tem saldo suficiente";
                    flash.success(mensagem);
                    profile(cliente.id,null);
                }else{
                    conta.credito =  c.replace(".",","); //20.0
                    conta.status = Status.COMPRA;
                    conta.cliente = cliente;
                    conta.save();

                    String mensagem = "Valor debitado do cliente";
                    flash.success(mensagem);
                    session.remove("cliente_id");
                    session.put("credito_antigo", conta.credito);
                    profile(cliente.id,null);
                }
	}
	
	//AÇÃO ADICIONARCREDITO - ADICIONA CRÉDITO A CONTA DE UM DETERMINADO CLIENTE
	public static void adicionarCredito(Long id, Cliente cliente, Conta conta){
		session.put("cliente_id", cliente.id);
		cliente.findById(Long.parseLong(session.get("cliente_id")));
		conta.find("cliente_id = ?", Long.parseLong(session.get("cliente_id")));
		
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat data = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		conta.data = data.format(now);
		
		
		conta.status = Status.CREDITO;
		conta.cliente = cliente;
		conta.save();
		
		Double credito_anterior = Double.parseDouble(session.get("credito_antigo").replace(",","."));//10.0
		Double creditar = Double.parseDouble(conta.creditado.replace(",",".")); //10.0
		Double credito_total = credito_anterior + creditar; //10.0 + 10.0
		String c = String.valueOf(credito_total).format("%.2f",credito_total);
		conta.credito =  c.replace(".",","); //20.0
		conta.save();
		
		String mensagem = "Crédito adicionado ao cliente";
		flash.success(mensagem);
		session.remove("cliente_id");
		session.put("credito_antigo", conta.credito);
		profile(cliente.id,null);
	}
	
	//AÇÃO PROFILE - CHAMA UM CLIENTE A PARTIR DO SEU ID E MOSTRA TODAS INFORMAÇÕES DO MESMO
	public static void profile(Long id,Conta c){
		Cliente cliente = Cliente.findById(id);
		session.put("cliente_id", cliente.id);
		List<Conta> conta = Collections.emptyList();
		conta = Conta.find("cliente_id = ? ORDER BY data DESC", id).fetch(5);
		render(conta,cliente);
	}
	
	//AÇÃO SAIR - CHAMA UMA AÇÃO DO CONTROLADOR DE LOGINS PARA FAZER LOGOFF DO USUÁRIO
	public static void sair(){
		ControllerLogins.logoff();
	}
	
	//AÇÃO DOWNLOADFOTO - FAZ DOWNLOAD DA IMAGEM DE UM CLIENTE E MOSTRA EM UMA PÁGINA
	public static void downloadFoto(Long id){
		Cliente cliente = Cliente.findById(id);
		renderBinary(cliente.foto.getFile());
        }
}
