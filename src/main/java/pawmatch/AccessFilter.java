package pawmatch;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AccessFilter implements Filter {
    @Override
    public void doFilter(ServletRequest input, ServletResponse output, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) input;
        HttpServletResponse response = (HttpServletResponse) output;
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        String path = request.getServletPath();
        String requiredRole = null;
        for (String role : new String[] {"owner", "caregiver", "admin"}) {
            if (path.startsWith("/" + role + "/")) requiredRole = role;
        }
        if (requiredRole != null || path.equals("/logout")) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("accountId") == null) {
                response.sendRedirect(request.getContextPath() + "/login?required=1");
                return;
            }
            if (requiredRole != null && !requiredRole.equals(session.getAttribute("role"))) {
                response.sendError(403);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
