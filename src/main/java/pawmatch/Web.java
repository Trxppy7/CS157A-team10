package pawmatch;

import jakarta.servlet.http.HttpServletRequest;

public class Web {
    public static String escape(Object value) {
        if (value == null) return "";
        return value.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    public static String value(HttpServletRequest request, String name) {
        return escape(request.getParameter(name));
    }
}
