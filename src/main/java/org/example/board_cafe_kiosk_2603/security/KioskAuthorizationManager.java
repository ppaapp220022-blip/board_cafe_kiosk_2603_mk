package org.example.board_cafe_kiosk_2603.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

public class KioskAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication,
                                       RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        Object tableId = request.getSession().getAttribute("tableId");
        Object tableNumber = request.getSession().getAttribute("tableNumber");
        return new AuthorizationDecision(tableId != null || tableNumber != null);
    }
}
