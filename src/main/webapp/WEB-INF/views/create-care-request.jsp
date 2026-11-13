<%@ page import="java.util.List, java.util.Map" %>
<%@ include file="head.jspf" %>
<%
  List<Map<String, Object>> pets = (List<Map<String, Object>>) request.getAttribute("pets");
  List<Map<String, Object>> services = (List<Map<String, Object>>) request.getAttribute("services");
  String petSel = request.getParameter("petId");
  String svcSel = request.getParameter("serviceId");
  String[] vDates = request.getParameterValues("visitDate");
  String[] vTimes = request.getParameterValues("visitTime");
  int visitRows = (vDates != null && vDates.length > 0) ? vDates.length : 2;
%>
<main id="main" class="auth-main">
  <div class="page-intro">
    <span class="eyebrow">Owner account</span>
    <h1>Request care</h1>
    <p>Tell us which pet needs care and when, and we'll find caregivers who match.</p>
  </div>

  <% if (request.getAttribute("message") != null) { %>
    <p class="notice error" role="alert"><%= Web.escape(request.getAttribute("message")) %></p>
  <% } %>
  <% if (request.getAttribute("errors") != null) { %>
    <p class="notice error" role="alert">Check the fields below.</p>
  <% } %>

  <% if (pets == null || pets.isEmpty()) { %>
    <div class="surface empty-state">
      <p>You have no active pets to request care for.</p>
    </div>
  <% } else { %>
  <form class="surface" method="post" action="<%= request.getContextPath() %>/owner/requests/new">
    <input type="hidden" name="csrf" value="<%= Web.csrf(request) %>">

    <div class="field">
      <label for="petId">Pet</label>
      <select id="petId" name="petId"<%= Web.invalid(request, "petId") %> required>
        <option value="">Choose a pet…</option>
        <% for (Map<String, Object> pet : pets) {
             String id = String.valueOf(pet.get("id"));
             String label = (String) pet.get("name");
             if (pet.get("species") != null) label += " (" + pet.get("species") + ")"; %>
          <option value="<%= id %>"<%= id.equals(petSel) ? " selected" : "" %>><%= Web.escape(label) %></option>
        <% } %>
      </select>
      <%= Web.error(request, "petId") %>
    </div>

    <div class="field">
      <label for="serviceId">Service</label>
      <select id="serviceId" name="serviceId"<%= Web.invalid(request, "serviceId") %> required>
        <option value="">Choose a service…</option>
        <% for (Map<String, Object> svc : services) {
             String id = String.valueOf(svc.get("id")); %>
          <option value="<%= id %>"<%= id.equals(svcSel) ? " selected" : "" %>><%= Web.escape(svc.get("category")) %></option>
        <% } %>
      </select>
      <%= Web.error(request, "serviceId") %>
    </div>

    <div class="field">
      <label for="location">City and state</label>
      <input id="location" placeholder="San Jose, CA" name="location" value="<%= Web.value(request, "location") %>"<%= Web.invalid(request, "location") %> maxlength="200" required>
      <%= Web.error(request, "location") %>
    </div>

    <fieldset>
      <legend>Visits</legend>
      <div class="visit-rows" id="visit-rows">
        <% for (int i = 0; i < visitRows; i++) {
             String d = (vDates != null && i < vDates.length) ? vDates[i] : "";
             String t = (vTimes != null && i < vTimes.length) ? vTimes[i] : ""; %>
          <div class="visit-row">
            <input type="date" name="visitDate" value="<%= Web.escape(d) %>" aria-label="Visit date">
            <input type="time" name="visitTime" value="<%= Web.escape(t) %>" aria-label="Visit time">
          </div>
        <% } %>
      </div>
      <button type="button" class="button ghost small" id="add-visit">Add another visit</button>
      <%= Web.error(request, "visits") %>
    </fieldset>

    <div class="field">
      <label for="instructions">Instructions</label>
      <textarea id="instructions" name="instructions" rows="3"<%= Web.invalid(request, "instructions") %> maxlength="500" placeholder="Feeding, medication, access, anything a caregiver should know…"><%= Web.value(request, "instructions") %></textarea>
      <%= Web.error(request, "instructions") %>
    </div>

    <button class="button primary" type="submit">Submit request</button>
  </form>

  <script>
    (function () {
      var add = document.getElementById('add-visit');
      var rows = document.getElementById('visit-rows');
      if (!add || !rows) return;
      add.addEventListener('click', function () {
        var first = rows.querySelector('.visit-row');
        var row = first.cloneNode(true);
        row.querySelectorAll('input').forEach(function (input) { input.value = ''; });
        rows.appendChild(row);
      });
    })();
  </script>
  <% } %>

  <p class="form-footer"><a href="<%= request.getContextPath() %>/owner/requests">Back to care requests</a></p>
</main>
<%@ include file="foot.jspf" %>
