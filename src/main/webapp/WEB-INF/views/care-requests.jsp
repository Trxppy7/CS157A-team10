<%@ page import="java.util.List, java.util.Map" %>
<%@ include file="head.jspf" %>
<main id="main" class="account-main">
  <div class="page-intro">
    <span class="eyebrow">Owner account</span>
    <h1>Care requests</h1>
    <p>Requests you've created for your pets.</p>
  </div>

  <% if (request.getParameter("created") != null) { %>
    <p class="notice success" role="status">Your care request was submitted.</p>
  <% } %>

  <p class="toolbar">
    <a class="button primary" href="<%= request.getContextPath() %>/owner/requests/new">New request</a>
  </p>

  <%
    List<Map<String, Object>> requests = (List<Map<String, Object>>) request.getAttribute("requests");
    if (requests == null || requests.isEmpty()) {
  %>
    <div class="surface empty-state">
      <p>You have not created any care requests yet.</p>
    </div>
  <%
    } else {
      for (Map<String, Object> req : requests) {
        String status = (String) req.get("status");
        String badge = "booked".equals(status) ? "badge-booked" : "badge-open";
  %>
    <article class="card">
      <div class="card-head">
        <h2><%= Web.escape(req.get("service")) %> for <%= Web.escape(req.get("pet")) %></h2>
        <span class="badge <%= badge %>"><%= Web.escape(status) %></span>
      </div>
      <dl class="card-facts">
        <div><dt>Location</dt><dd><%= Web.escape(req.get("location")) %></dd></div>
        <div><dt>Visits</dt><dd><%= req.get("visits") %></dd></div>
        <div><dt>Requested</dt><dd><%= req.get("created") == null ? "&mdash;" : Web.escape(req.get("created")) %></dd></div>
      </dl>
      <div class="card-actions">
        <% if ("open".equals(status)) { %>
          <a class="button primary small" href="<%= request.getContextPath() %>/owner/matches?requestId=<%= req.get("id") %>">View caregiver matches</a>
        <% } else { %>
          <a href="<%= request.getContextPath() %>/owner/bookings">View bookings</a>
        <% } %>
      </div>
    </article>
  <%
      }
    }
  %>

  <p class="form-footer"><a href="<%= request.getContextPath() %>/owner/home">Back to My Account</a></p>
</main>
<%@ include file="foot.jspf" %>
