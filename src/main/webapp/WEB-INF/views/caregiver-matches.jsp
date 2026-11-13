<%@ page import="java.util.List, java.util.Map" %>
<%@ include file="head.jspf" %>
<main id="main" class="account-main">
  <div class="page-intro">
    <span class="eyebrow">Owner account</span>
    <h1>Caregiver matches</h1>
    <p><%= Web.escape(request.getAttribute("service")) %> for <%= Web.escape(request.getAttribute("pet")) %> in <%= Web.escape(request.getAttribute("location")) %>.</p>
  </div>
  <% if (request.getParameter("unavailable") != null) { %>
    <p class="notice error" role="alert">This request or caregiver is no longer available. Check the current matches below.</p>
  <% } %>
  <%
    List<Map<String, Object>> matches = (List<Map<String, Object>>) request.getAttribute("matches");
    if (!"open".equals(request.getAttribute("status"))) {
  %>
    <p class="notice" role="status">This request is already on hold or booked. <a href="<%= request.getContextPath() %>/owner/bookings">View your bookings</a>.</p>
  <% } else if (matches.isEmpty()) { %>
    <div class="surface empty-state"><p>No caregivers match every requested visit, service, city, and animal type. Your request is still open.</p></div>
  <% } else { for (Map<String, Object> match : matches) { %>
    <article class="card">
      <div class="card-head"><h2><%= Web.escape(match.get("name")) %></h2><span class="badge badge-active">Available</span></div>
      <div class="match-why"><span class="chip"><%= Web.escape(request.getAttribute("species")) %> experience</span><span class="chip"><%= Web.escape(match.get("area")) %></span><span class="chip"><%= Web.escape(request.getAttribute("service")) %></span></div>
      <dl class="card-facts">
        <div><dt>Price per visit</dt><dd>$<%= Web.escape(match.get("price")) %></dd></div>
        <div><dt>Estimated total</dt><dd>$<%= Web.escape(match.get("total")) %></dd></div>
        <div><dt>Available visits</dt><dd><ul>
          <% for (String slot : (List<String>) match.get("slots")) { %><li><%= Web.escape(slot) %></li><% } %>
        </ul></dd></div>
      </dl>
      <form class="card-actions" method="post" action="<%= request.getContextPath() %>/owner/bookings">
        <input type="hidden" name="csrf" value="<%= Web.csrf(request) %>">
        <input type="hidden" name="requestId" value="<%= request.getAttribute("requestId") %>">
        <input type="hidden" name="caregiverId" value="<%= match.get("id") %>">
        <button class="button primary" type="submit">Request booking with <%= Web.escape(match.get("name")) %></button>
      </form>
    </article>
  <% } } %>
  <p class="form-footer"><a href="<%= request.getContextPath() %>/owner/requests">Back to care requests</a></p>
</main>
<%@ include file="foot.jspf" %>
