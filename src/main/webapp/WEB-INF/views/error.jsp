<% request.setAttribute("title", response.getStatus() == 403 ? "Page unavailable" : response.getStatus() == 404 ? "Page not found" : "Something went wrong"); %>
<%@ include file="head.jspf" %>
<main id="main" class="auth-main">
  <div class="page-intro"><span class="eyebrow">PawMatch</span><h1><%= Web.escape(request.getAttribute("title")) %></h1></div>
  <p><%= response.getStatus() == 403 ? "This page isn't available for your account." : response.getStatus() == 404 ? "We can't find that page." : "Please try again in a moment." %></p>
  <% if (session.getAttribute("accountId") != null) { %>
  <a class="button primary" href="<%= request.getContextPath() %>/<%= Web.escape(session.getAttribute("role")) %>/home">Go to my account</a>
  <% } else { %><a class="button primary" href="<%= request.getContextPath() %>/">Back to home</a><% } %>
</main>
<%@ include file="foot.jspf" %>
