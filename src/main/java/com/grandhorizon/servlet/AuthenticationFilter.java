package com.grandhorizon.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {
        "/profile", "/profile/update", "/profile/delete",
        "/booking", "/book-room.jsp", "/admin-dashboard.jsp", "/admin/*"
})
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null
                || (session.getAttribute("userId") == null && session.getAttribute("username") == null)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (request.getServletPath().equals("/book-room.jsp")
                && request.getAttribute("room") == null) {
            response.sendRedirect(request.getContextPath() + "/rooms");
            return;
        }
        if (request.getServletPath().startsWith("/profile")
                && session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/rooms");
            return;
        }
        if ((request.getServletPath().equals("/admin-dashboard.jsp")
                || request.getServletPath().startsWith("/admin/"))
                && !"Admin".equals(session.getAttribute("role"))
                && !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
