<%@ include file="head.jspf" %>

<main id="main" class="auth-main">
    <div class="page-intro">
        <span class="eyebrow">Account recovery</span>
        <h1>Reset Password</h1>
        <p>Enter your new password below.</p>
    </div>

    <% if (request.getAttribute("message") != null) { %>
        <p class="notice error" role="alert">
            <%= Web.escape(request.getAttribute("message")) %>
        </p>
    <% } %>

    <% if (Boolean.TRUE.equals(request.getAttribute("validToken"))) { %>
        <form class="surface" method="post"
              action="<%= request.getContextPath() %>/reset-password">

            <input type="hidden" name="csrf"
                   value="<%= Web.csrf(request) %>">

            <input type="hidden" name="token"
                   value="<%= Web.value(request, "token") %>">

            <div class="field">
                <label for="password">New Password</label>
                <input id="password" name="password" type="password"
                       autocomplete="new-password"
                       minlength="8" maxlength="128" required>
            </div>

            <div class="field">
                <label for="confirmPassword">Confirm New Password</label>
                <input id="confirmPassword" name="confirmPassword"
                       type="password" autocomplete="new-password"
                       minlength="8" maxlength="128" required>
            </div>

            <button class="button primary" type="submit">
                Reset Password
            </button>
        </form>
    <% } %>

    <p class="form-footer">
        <a href="<%= request.getContextPath() %>/login">Back to Login</a>
    </p>
</main>

<%@ include file="foot.jspf" %>
