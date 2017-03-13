/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models.negocio;

import models.Vendedor;
import play.mvc.Scope.Session;

/**
 *
 * @author leonatercio
 */
public class GerenciadorSessao {
    
    public static void sessaoLogin(Session session, Vendedor v){
        session.put("vendedor_id", v.id);
        session.put("vendedor", "logado");
        session.put("vendedor_email", v.email);
        session.put("vendedor_nome", v.nome);
        session.put("vendedor_foto", v.foto);
        session.put("credito_antigo", "0,00");
        session.put("debito_antigo", "0,00");
    }
    
}