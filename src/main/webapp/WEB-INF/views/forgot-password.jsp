<%@ include file="head.jspf" %>

<main id="main" class="auth-main">
    <div class="page-intro">
        <span class="eyebrow">Account recovery</span>
        <h1>Forgot Password</h1>
        <p>Enter your email address to request a password reset.</p>
    </div>

    <% if (request.getAttribute("message") != null) { %>
        <p class="notice" role="status">
            <%= Web.escape(request.getAttribute("message")) %>
        </p>
    <% } %>

    <form class="surface" method="post"
          action="<%= request.getContextPath() %>/forgot-password">

        <input type="hidden" name="csrf"
               value="<%= Web.csrf(request) %>">

        <div class="field">
            <label for="email">Email Address</label>
            <input id="email"
                   name="email"
                   type="email"
                   value="<%= Web.value(request, "email") %>"
                   autocomplete="email"
                   maxlength="100"
                   required>
        </div>

        <button class="button primary" type="submit">
            Request Password Reset
        </button>
    </form>

    <p class="form-footer">
        Remember your password?
        <a href="<%= request.getContextPath() %>/login">
            Back to Login
        </a>
    </p>
</main>

<%@ include file="foot.jspf" %>
