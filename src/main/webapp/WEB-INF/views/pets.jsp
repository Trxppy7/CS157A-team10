<%@ page import="java.util.List,java.util.Map" %>
<%@ include file="head.jspf" %>

<main id="main" class="account-main">
    <div class="page-intro">
        <span class="eyebrow">Pet owner account</span>
        <h1>My Pets</h1>
        <p>View and manage your pets.</p>
    </div>

    <%
        List<Map<String, Object>> pets =
                (List<Map<String, Object>>) request.getAttribute("pets");
    %>

    <p>
        <a class="button primary" href="<%= request.getContextPath() %>/owner/pets/add">
            Add Pet
        </a>
    </p>

    <% if (pets.isEmpty()) { %>
        <p>You haven't added any pets yet.</p>
    <% } else { %>
        <% for (Map<String, Object> pet : pets) { %>
            <div class="surface">
                <h2><%= Web.escape(pet.get("name")) %></h2>
                <% if (pet.get("photoFilename") != null) { %>
                    <img src="<%= request.getContextPath() %>/owner/pets/photo?id=<%= pet.get("id") %>"
                         alt="Photo of <%= Web.escape(pet.get("name")) %>"
                         style="max-width: 250px; height: auto;">
                <% } %>
                <p><strong>Status:</strong>
                    <%= Boolean.TRUE.equals(pet.get("active")) ? "Active" : "Inactive" %>
                </p>
                <p><strong>Species:</strong> <%= Web.escape(pet.get("species")) %></p>
                <p><strong>Breed or Description:</strong> <%= Web.escape(pet.get("breedDescription")) %></p>
                <p><strong>Age:</strong> <%= pet.get("age") == null ? "Not specified" : pet.get("age") %></p>
                <p><strong>Size:</strong> <%= Web.escape(pet.get("size")) %></p>
                <p><strong>Contact:</strong> <%= Web.escape(pet.get("contact")) %></p>
                <p><strong>Conditions:</strong> <%= Web.escape(pet.get("conditions")) %></p>
                <p><strong>Medication Instructions:</strong> <%= Web.escape(pet.get("medicationInstructions")) %></p>
                <p><strong>Feeding Instructions:</strong> <%= Web.escape(pet.get("feedingInstructions")) %></p>
                <p><strong>Behavior Notes:</strong> <%= Web.escape(pet.get("behaviorNotes")) %></p>
                <p><strong>Veterinary Contact:</strong> <%= Web.escape(pet.get("vetContact")) %></p>
                <a class="button primary"
                   href="<%= request.getContextPath() %>/owner/pets/edit?id=<%= pet.get("id") %>">
                    Edit Pet
                </a>
                <% if (Boolean.TRUE.equals(pet.get("active"))) { %>
                <form method="post"
                      action="<%= request.getContextPath() %>/owner/pets/deactivate">
                    <input type="hidden" name="csrf" value="<%= Web.csrf(request) %>">
                    <input type="hidden" name="id" value="<%= pet.get("id") %>">
                    <button class="button" type="submit">Deactivate Pet</button>
                </form>
                <% } %>
            </div>
        <% } %>
    <% } %>

    <p class="form-footer">
        <a href="<%= request.getContextPath() %>/owner/home">Back to My Account</a>
    </p>
</main>

<%@ include file="foot.jspf" %>
