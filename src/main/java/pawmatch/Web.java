package pawmatch;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Web {
    public static String escape(Object value) {
        if (value == null) return "";
        return value.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    public static String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    public static String email(HttpServletRequest request) {
        return text(request, "email").toLowerCase(Locale.ROOT);
    }

    public static String value(HttpServletRequest request, String name) {
        return escape(request.getParameter(name));
    }

    public static boolean validEmail(String value) {
        return value.length() <= 100 && value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    }

    public static boolean validPhone(String value) {
        int digits = value.replaceAll("[^0-9]", "").length();
        return value.length() <= 20 && value.matches("[+0-9(). -]+") && digits >= 10 && digits <= 15;
    }

    public static String csrf(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("csrf") == null) session.setAttribute("csrf", UUID.randomUUID().toString());
        return (String) session.getAttribute("csrf");
    }

    public static boolean validCsrf(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && request.getParameter("csrf") != null
                && request.getParameter("csrf").equals(session.getAttribute("csrf"));
    }

    @SuppressWarnings("unchecked")
    public static String error(HttpServletRequest request, String field) {
        Map<String, String> errors = (Map<String, String>) request.getAttribute("errors");
        if (errors == null || !errors.containsKey(field)) return "";
        return "<small class=\"error-text\" id=\"" + field + "-error\">" + escape(errors.get(field)) + "</small>";
    }

    public static String invalid(HttpServletRequest request, String field) {
        return error(request, field).isEmpty() ? "" : " aria-invalid=\"true\" aria-describedby=\"" + field + "-error\"";
    }
}
