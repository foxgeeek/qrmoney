package controllers;

import models.Vendedor;
import play.Logger;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;

import com.google.gson.JsonObject;

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

    public static void index() {
        Vendedor u = connected();
        JsonObject me = null;
        if (u != null && u.access_token != null) {
            me = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(u.access_token)).get().getJson().getAsJsonObject();
        }
        render(me);
    }

    public static void auth() {
        if (OAuth2.isCodeResponse()) {
            Vendedor u = connected();
            OAuth2.Response response = FACEBOOK.retrieveAccessToken(authURL());
            u.access_token = response.accessToken;
            u.save();
            Sistema.index(u);
        }
        FACEBOOK.retrieveVerificationCode(authURL());
    }

    @Before
    static void setuser() {
        Vendedor user = null;
        if (session.contains("uid")) {
            Logger.info("existing user: " + session.get("uid"));
            user = Vendedor.get(Long.parseLong(session.get("uid")));
        }
        if (user == null) {
            user = Vendedor.createNew();
            session.put("uid", user.uid);
        }
        renderArgs.put("user", user);
    }

    static String authURL() {
        return play.mvc.Router.getFullUrl("FB.auth");
    }

    static Vendedor connected() {
        return (Vendedor)renderArgs.get("user");
    }

}
