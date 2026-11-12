<%@ page import="java.util.List, java.util.Map" %>
<%@ include file="head.jspf" %>
<main id="main" class="account-main">
  <div class="page-intro"><span class="eyebrow">Owner account</span><h1>My bookings</h1><p>Your care bookings and visit times.</p></div>
  <% if (request.getParameter("created") != null) { %>
    <p class="notice success" role="status">Booking request submitted. It is pending caregiver acceptance.</p>
  <% } %>
  <%
    List<Map<String, Object>> bookings = (List<Map<String, Object>>) request.getAttribute("bookings");
    if (bookings.isEmpty()) {
  %>
    <div class="surface empty-state"><p>You have no bookings yet. Find a caregiver from your care requests.</p></div>
  <% } else { for (Map<String, Object> booking : bookings) { %>
    <article class="card">
      <div class="card-head"><h2><%= Web.escape(booking.get("service")) %> for <%= Web.escape(booking.get("pet")) %></h2><span class="badge"><%= Web.escape(booking.get("status")) %></span></div>
      <dl class="card-facts">
        <div><dt>Caregiver</dt><dd><%= Web.escape(booking.get("caregiver")) %></dd></div>
        <div><dt>Location</dt><dd><%= Web.escape(booking.get("location")) %></dd></div>
        <div><dt>Estimated total</dt><dd>$<%= Web.escape(booking.get("total")) %></dd></div>
        <div><dt>Visits</dt><dd><ul>
          <% for (String visit : (List<String>) booking.get("visits")) { %><li><%= Web.escape(visit) %></li><% } %>
        </ul></dd></div>
      </dl>
    </article>
  <% } } %>
  <p class="form-footer"><a href="<%= request.getContextPath() %>/owner/requests">Back to care requests</a></p>
</main>
<%@ include file="foot.jspf" %>
