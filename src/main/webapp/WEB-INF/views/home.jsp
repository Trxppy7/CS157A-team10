<%@ include file="head.jspf" %>
<main id="main" class="account-main">
  <div class="page-intro"><span class="eyebrow"><%= Web.escape(session.getAttribute("role")) %> account</span><h1>Hi, <%= Web.escape(request.getAttribute("name")) %></h1></div>
  <dl class="details">
    <div><dt>Name</dt><dd><%= Web.escape(request.getAttribute("name")) %></dd></div>
    <div><dt>Email</dt><dd><%= Web.escape(request.getAttribute("email")) %></dd></div>
    <div><dt>Phone</dt><dd><%= Web.escape(request.getAttribute("phone")) %></dd></div>
  </dl>
</main>
<%@ include file="foot.jspf" %>
