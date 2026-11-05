package com.nexusmarket.application.adapters.in.web.support;

import com.nexusmarket.domain.exceptions.UnauthenticatedException;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;

/**
 * En la interfaz web el actor se identifica una vez al ingresar y su id queda
 * en la sesion HTTP; cumple el mismo papel que la cabecera X-User-Id en la API REST.
 */
public final class WebSession {

    private static final String ACTOR = "actorId";

    private WebSession() {
    }

    public static EntityId actor(HttpSession session) {
        Object id = session.getAttribute(ACTOR);
        if (id == null) {
            throw new UnauthenticatedException("Log in to continue");
        }
        return EntityId.of(id.toString());
    }

    public static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(ACTOR) != null;
    }

    public static void login(HttpSession session, EntityId actorId) {
        session.setAttribute(ACTOR, actorId.toString());
    }

    public static void logout(HttpSession session) {
        session.invalidate();
    }
}
