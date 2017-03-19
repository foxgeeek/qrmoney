package controllers;

import models.Vendedor;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.Controller;
import com.google.gson.JsonObject;
import models.negocio.GerenciadorSessao;

public class FB extends Controller {

    public static OAuth2 FACEBOOK = new OAuth2(
            "https://graph.facebook.com/oauth/authorize",
            "https://graph.facebook.com/oauth/access_token",
            "403822643289659",
            "ba6492631719419d7322cbe8aa636c2e"
    );
    //ESTA AÇÃO FAZ TODA A VERIFICAÇÃO VIA FACEBOOK PARA LOGAR NO SISTEMA
    public static void auth() {
        if (OAuth2.isCodeResponse()) {
            Vendedor vendedor = null;
            
            OAuth2.Response resposta;
            resposta = FACEBOOK.retrieveAccessToken(authURL());
            
            String accessToken = resposta.accessToken;
            
            JsonObject me = WS.url("https://graph.facebook.com/me?fields=id,name,picture.height(500),email&access_token=%s", WS.encode(accessToken)).get().getJson().getAsJsonObject();
            
            String email = me.get("email").getAsString();
            
            /*VERIFICA SE JÁ EXISTE VENDEDOR CADASTRADO COM ESTE E-MAIL
            CASO EXISTA ELE É REDIRECIONADO DIRETAMENTE AO SISTEMA.INDEX, CASO NÃO
            CAPTURA AS INFORMAÇÕES PASSADAS NO JSON OBJECT E PASSA COMO DADOS PARA O VENDEDOR.
            */
            vendedor = Vendedor.find("lower(email)", email.toLowerCase()).first();
            if(vendedor == null){
                vendedor = new Vendedor();
                vendedor.email = me.get("email").getAsString();
                vendedor.nome = me.get("name").getAsString();
                vendedor.foto = me.get("picture").getAsJsonObject().get("data").getAsJsonObject().get("url").toString().replaceAll("\"", "");
                vendedor.save();
            }
            GerenciadorSessao.sessaoLogin(session, vendedor);
            Sistema.registro(vendedor.id);
        }
        //ESTA FUNÇÃO FAZ A REQUISIÇÃO DO ESCOPO DE E-MAIL PARA O USUÁRIO
        FACEBOOK.retrieveVerificationCode(authURL(),"scope","email");
    }
    
    static String authURL() {
        return play.mvc.Router.getFullUrl("FB.auth");
    }
}