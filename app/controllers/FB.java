package controllers;

import models.Vendedor;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.Controller;

import com.google.gson.JsonObject;
import models.negocio.GerenciadorSessao;

public class FB extends Controller {

    // The following keys correspond to a test application
    // registered on Facebook, and associated with the loisant.org domain.
    // You need to bind loisant.org to your machine with /etc/hosts to
    // test the application locally.

    public static OAuth2 FACEBOOK = new OAuth2(
            "https://graph.facebook.com/oauth/authorize",
            "https://graph.facebook.com/oauth/access_token",
            "403822643289659",
            "ba6492631719419d7322cbe8aa636c2e"
    );
    
    public static void auth() {
        if (OAuth2.isCodeResponse()) {
            Vendedor vendedor = null;
            
            OAuth2.Response resposta;
            resposta = FACEBOOK.retrieveAccessToken(authURL());
            
            String accessToken = resposta.accessToken;
            
            JsonObject me = WS.url("https://graph.facebook.com/me?fields=id,name,picture,email&access_token=%s", WS.encode(accessToken)).get().getJson().getAsJsonObject();
            
            String email = me.get("email").getAsString();
            
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
        FACEBOOK.retrieveVerificationCode(authURL(),"scope","email");
    }
    
    static String authURL() {
        return play.mvc.Router.getFullUrl("FB.auth");
    }
}