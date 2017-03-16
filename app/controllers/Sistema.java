package controllers;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import br.com.caelum.stella.boleto.Banco;
import br.com.caelum.stella.boleto.Boleto;
import br.com.caelum.stella.boleto.Datas;
import br.com.caelum.stella.boleto.Emissor;
import br.com.caelum.stella.boleto.Sacado;
import br.com.caelum.stella.boleto.bancos.Bradesco;
import br.com.caelum.stella.boleto.transformer.GeradorDeBoleto;
import models.Status;
import models.Vendedor;
import models.Cliente;
import models.Conta;
import models.negocio.GerenciadorSessao;
import play.mvc.Controller;
import play.mvc.With;
import java.util.Calendar;

@With(ControllerSeguranca.class)
public class Sistema extends Controller{
	
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
        
        //GERENCIAR BOLETOS
        public static void gerenciaboleto(){
                List<Conta> contas = Collections.emptyList();
                contas = Conta.find("aceite = ?",Status.INATIVO).fetch();
		int ativos = (int) Conta.count("aceite = ?",Status.ATIVO);
                int inativos = (int) Conta.count("aceite = ?",Status.INATIVO);
                int gerados = (int) Conta.count();
		render(ativos,inativos,gerados,contas);
	}
        
        //MODIFICAR ACEITE DO BOLETO
        public static void mudarAceite(Long id){
                Conta c = Conta.findById(id);
                if(c.aceite == Status.INATIVO){
                    c.aceite = Status.ATIVO;
                    c.save();
                }else{
                    c.aceite = Status.INATIVO;
                    c.save();
                }
		gerenciaboleto();
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
		profile(cliente.id,null);
	}
	
	//AÇÃO INDEX - CHAMA A PÁGINA PRINCIPAL COM TODAS INFORMAÇÕES NECESSÁRIAS
	public static void index(){
		List<Cliente> clientes = Collections.emptyList();
                List<Conta> contas = Collections.emptyList();
		clientes = Cliente.find("status = ? AND vendedor_fk = ?",Status.ATIVO, Vendedor.findById(Long.parseLong(session.get("vendedor_id")))).fetch();
		int registros = (int) Cliente.count("status = ?",Status.ATIVO);
		int compras = (int) Conta.count("status = ?", Status.COMPRA);
                int gerados = (int) Conta.count();
		render(clientes,registros,compras,gerados,contas);
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
		session.put("debito_antigo", conta.debito);
                
                cliente.findById(Long.parseLong(session.get("cliente_id")));
		conta.find("cliente_id = ?", Long.parseLong(session.get("cliente_id")));
		
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat data = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		conta.data = data.format(now);
		
		Double credito_anterior = Double.parseDouble(session.get("credito_antigo").replace(",","."));//10.0
		Double debito = Double.parseDouble(session.get("debito_antigo").replace(",",".")); //10.0
		Double credito_final = credito_anterior - debito; //10.0 + 10.0 = 20.0
                String c = String.valueOf(credito_final).format("%.2f",credito_final);
                Double credito = Double.parseDouble(c.replace(",","."));
                if(debito > credito){
                    String mensagem = "Cliente não tem saldo suficiente";
                    flash.success(mensagem);
                    profile(cliente.id,null);
                }else{
                    conta.credito =  c.replace(".",","); //20.0
                    conta.status = Status.COMPRA;
                    conta.aceite = Status.ATIVO;
                    conta.cliente = cliente;
                    session.put("credito_antigo", conta.credito);
                    conta.save();

                    String mensagem = "Valor debitado do cliente";
                    flash.success(mensagem);
                    session.remove("cliente_id");
                    
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
                conta.aceite = Status.ATIVO;
		conta.cliente = cliente;
		conta.save();
		
		Double credito_anterior = Double.parseDouble(session.get("credito_antigo").replace(",","."));//10.0
		Double creditar = Double.parseDouble(conta.creditado.replace(",",".")); //10.0
		Double credito_total = credito_anterior + creditar; //10.0 + 10.0
		String c = String.valueOf(credito_total).format("%.2f",credito_total);
		conta.credito =  c.replace(".",","); //20.0
		session.put("credito_antigo", conta.credito);
                conta.save();
		
		String mensagem = "Crédito adicionado ao cliente";
		flash.success(mensagem);
		session.remove("cliente_id");
		
		profile(cliente.id,null);
	}
	
	//AÇÃO PROFILE - CHAMA UM CLIENTE A PARTIR DO SEU ID E MOSTRA TODAS INFORMAÇÕES DO MESMO
	public static void profile(Long id,Conta c){
		Cliente cliente = Cliente.findById(id);
		session.put("cliente_id", cliente.id);
		List<Conta> conta = Collections.emptyList();
		conta = Conta.find("cliente_id = ? AND aceite = ? ORDER BY data DESC", id, c.aceite.ATIVO).fetch(5);
                render(conta,cliente);
	}
        
        //IMPRIMIR BOLETO - TESTE
        public static void imprimirBoleto(Long id,Vendedor vendedor, Conta conta){
        	vendedor = Vendedor.findById(Long.parseLong(session.get("vendedor_id")));
            Cliente cliente = Cliente.findById(Long.parseLong(session.get("cliente_id")));
            conta.find("cliente_id = ? AND aceite = ?", Long.parseLong(session.get("cliente_id")), conta.aceite.ATIVO);
                        
            Date now = new Date(System.currentTimeMillis());
            SimpleDateFormat data = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
            conta.data = data.format(now);
            conta.status = Status.CREDITO;
            conta.aceite = Status.INATIVO;
            conta.cliente = cliente;
            
            Double credito_anterior = Double.parseDouble(session.get("credito_antigo").replace(",","."));//10.0
            Double creditar = Double.parseDouble(conta.creditado.replace(",","."));
            Double credito_total = credito_anterior + creditar; //10.0 + 10.0
            String creditou = String.valueOf(credito_total).format("%.2f",credito_total);
            conta.credito =  creditou.replace(".",","); //20.0
            conta.save();
            
            BigDecimal credito = new BigDecimal(creditar);
            Calendar c = Calendar.getInstance();
            Datas datas = Datas.novasDatas().comDocumento(c.get(Calendar.DAY_OF_MONTH),c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR))
                .comProcessamento(c.get(Calendar.DAY_OF_MONTH),c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR)).comVencimento(c.get(Calendar.DAY_OF_MONTH),c.get(Calendar.MONTH)+2,c.get(Calendar.YEAR));

            Emissor emissor = Emissor.novoEmissor()
                .comCedente(vendedor.nome)
                .comAgencia("2345")
                .comDigitoAgencia("6")
                .comContaCorrente("12345")
                .comNumeroConvenio("1234567")
                .comDigitoContaCorrente("1")
                .comCarteira("22")
                .comNossoNumero("9050987");

            Sacado sacado = Sacado.novoSacado()
                .comNome(cliente.nome)
                .comCpf(cliente.cpf)
                .comEndereco(cliente.endereco)
                .comBairro("Centro")
                .comCep("01234-111")
                .comCidade(cliente.cidade)
                .comUf("RN");

            Banco banco = new Bradesco();

            Boleto boleto = Boleto.novoBoleto()
                .comBanco(banco)
                .comDatas(datas)
                .comDescricoes("descricao 1", "descricao 2", "descricao 3",
                            "descricao 4", "descricao 5")
                .comEmissor(emissor)
                .comSacado(sacado)
                .comValorBoleto(credito)
                .comNumeroDoDocumento("1234")
                .comInstrucoes("Por favor não receber após o vencimento", "Após vencimento pagar diretamente no banco")
                .comLocaisDePagamento("Banco Bradesco", "Banco Bradesco")
                .comNumeroDoDocumento("4343");

            GeradorDeBoleto gerador = new GeradorDeBoleto(boleto);

            // Para gerar um boleto em PDF
            gerador.geraPNG("boleto.png");
            
            System.out.println("Boleto do Sr. "+cliente.nome+" gerado com sucesso!");
            
            String mensagem = "Boleto gerado com sucesso\n<a type='button' class='btn btn-primary' href='gerenciaboleto'\n" +
"           <i class=\"fa fa-plus\"></i>\n" +
"           Ativar crédito\n" +
"           <i class=\"fa fa-dollar\"></i>\n" +
"           </a>";
            flash.success(mensagem);
            profile(cliente.id,null);
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
