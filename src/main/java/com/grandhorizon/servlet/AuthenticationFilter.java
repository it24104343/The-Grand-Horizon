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
        "/booking", "/booking/cancel", "/book-room.jsp", "/admin-dashboard.jsp", "/admin/*"
})
// AuthenticationFilter blocks unauthorized access to protected user and admin pages.
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        String path = request.getServletPath();
        boolean adminPath = path.equals("/admin-dashboard.jsp") || path.startsWith("/admin/");
        boolean userPath = path.startsWith("/profile") || path.equals("/booking/cancel")
                || path.equals("/book-room.jsp");
        boolean bookingSubmission = path.equals("/booking") && "POST".equalsIgnoreCase(request.getMethod());
        if ((adminPath || userPath || bookingSubmission)
                && (session == null
                || (session.getAttribute("userId") == null && session.getAttribute("username") == null))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (request.getServletPath().equals("/book-room.jsp")
                && request.getAttribute("room") == null) {
            response.sendRedirect(request.getContextPath() + "/rooms");
            return;
        }
        if ((bookingSubmission || path.equals("/booking/cancel"))
                && session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "A registered guest account is required to make a booking.");
            return;
        }
        if (path.startsWith("/profile")
                && session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/rooms");
            return;
        }
        if (adminPath
                && !"Admin".equals(session.getAttribute("role"))
                && !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (path.equals("/admin-dashboard.jsp")) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        chain.doFilter(request, response);
    }
}
