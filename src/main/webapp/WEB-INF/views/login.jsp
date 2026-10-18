<%@ include file="head.jspf" %>
<main id="main" class="auth-main">
  <div class="page-intro"><span class="eyebrow">Welcome back</span><h1>Log in</h1><p>Your PawMatch account, all in one place.</p></div>
  <% if ("1".equals(request.getParameter("created"))) { %><p class="notice success" role="status">Your account is ready. Log in to continue.</p><% } %>
  <% if ("1".equals(request.getParameter("required"))) { %><p class="notice" role="status">Log in to see that page.</p><% } %>
  <% if (request.getAttribute("message") != null) { %><p class="notice error" role="alert"><%= Web.escape(request.getAttribute("message")) %></p><% } %>
  <form class="surface" method="post" action="<%= request.getContextPath() %>/login">
    <input type="hidden" name="csrf" value="<%= Web.csrf(request) %>">
    <div class="field"><label for="email">Email</label><input id="email" name="email" type="email" value="<%= Web.value(request, "email") %>" autocomplete="email" maxlength="100" required></div>
    <div class="field"><label for="password">Password</label><input id="password" name="password" type="password" autocomplete="current-password" maxlength="128" required></div>
    <button class="button primary" type="submit">Log in</button>
  </form>
  <p class="form-footer">New to PawMatch? <a href="<%= request.getContextPath() %>/register">Create an account</a></p>
</main>
<%@ include file="foot.jspf" %>
