<%@ include file="head.jspf" %>
<main id="main" class="auth-main">
  <div class="page-intro"><span class="eyebrow">Join PawMatch</span><h1>Create an account</h1><p>A place for pet owners and the people who care for them.</p></div>
  <% if ("1".equals(request.getParameter("created"))) { %><p class="notice success" role="status">Your account is ready.</p><% } %>
  <% if (request.getAttribute("message") != null) { %><p class="notice error" role="alert"><%= Web.escape(request.getAttribute("message")) %></p><% } %>
  <% if (request.getAttribute("errors") != null) { %><p class="notice error" role="alert">Check the fields below.</p><% } %>
  <form class="surface" method="post" action="<%= request.getContextPath() %>/register">
    <input type="hidden" name="csrf" value="<%= Web.csrf(request) %>">
    <fieldset class="role-group">
      <legend>I am a…</legend>
      <div class="role-options">
        <label class="role-choice"><input type="radio" name="role" value="owner" <%= "owner".equals(request.getParameter("role")) ? "checked" : "" %><%= Web.invalid(request, "role") %> required><span><strong>Pet owner</strong><small>I need care for my pet.</small></span></label>
        <label class="role-choice"><input type="radio" name="role" value="caregiver" <%= "caregiver".equals(request.getParameter("role")) ? "checked" : "" %> required><span><strong>Caregiver</strong><small>I care for other people's pets.</small></span></label>
      </div>
      <%= Web.error(request, "role") %>
    </fieldset>
    <div class="field"><label for="name">Name</label><input id="name" name="name" value="<%= Web.value(request, "name") %>"<%= Web.invalid(request, "name") %> autocomplete="name" maxlength="100" required><%= Web.error(request, "name") %></div>
    <div class="field"><label for="email">Email</label><input id="email" name="email" value="<%= Web.value(request, "email") %>"<%= Web.invalid(request, "email") %> type="email" autocomplete="email" maxlength="100" required><%= Web.error(request, "email") %></div>
    <div class="field"><label for="phone">Phone</label><input id="phone" name="phone" value="<%= Web.value(request, "phone") %>"<%= Web.invalid(request, "phone") %> type="tel" autocomplete="tel" maxlength="20" required><%= Web.error(request, "phone") %></div>
    <div class="field"><label for="password">Password</label><input id="password" name="password"<%= Web.invalid(request, "password") %> type="password" autocomplete="new-password" minlength="8" maxlength="128" required><small id="password-help">Use at least 8 characters.</small><%= Web.error(request, "password") %></div>
    <button class="button primary" type="submit">Create account</button>
  </form>
</main>
<%@ include file="foot.jspf" %>
