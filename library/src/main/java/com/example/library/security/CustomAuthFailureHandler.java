
// OBS: Ej i bruk eftersom vi kör JWT-login via AuthController

// OBS: Denna handler används endast om .formLogin() är aktivt (klassisk HTML-form login).
// I vår JWT-baserade REST-app används istället AuthController för login,
// så denna handler är inte kopplad till något flöde just nu.


//package com.example.library.security;
//
//import com.example.library.security.LoginAttemptService;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class CustomAuthFailureHandler implements AuthenticationFailureHandler {
//
//    private final LoginAttemptService loginAttemptService;
//
//    public CustomAuthFailureHandler(LoginAttemptService loginAttemptService) {
//        this.loginAttemptService = loginAttemptService;
//    }
//
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        AuthenticationException exception)
//            throws IOException, ServletException {
//
//        String email = request.getParameter("username");
//        if (email == null) {
//            // om du använder "email" i request-body via JSON (AuthController) - hantera där istället
//            email = request.getParameter("email");
//        }
//        if (email != null) {
//            loginAttemptService.loginFailed(email.toLowerCase());
//        }
//
//        System.out.println("❌ Misslyckad inloggning: " + email + " - " + exception.getMessage());
//        response.sendRedirect("/login?error");
//    }
//}
