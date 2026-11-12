<%@ page import="java.util.Map" %>
<%@ include file="head.jspf" %>

<%
    Map<String, Object> pet =
            (Map<String, Object>) request.getAttribute("pet");
%>

<main id="main" class="auth-main">
    <div class="page-intro">
        <span class="eyebrow">Pet owner account</span>
        <h1>Edit Pet</h1>
        <p>Update your pet's information below.</p>
    </div>

    <% if (request.getAttribute("message") != null) { %>
        <p class="notice error" role="alert">
            <%= Web.escape(request.getAttribute("message")) %>
        </p>
    <% } %>

    <form class="surface" method="post" enctype="multipart/form-data"
          action="<%= request.getContextPath() %>/owner/pets/edit">

        <input type="hidden" name="csrf" value="<%= Web.csrf(request) %>">
        <input type="hidden" name="id" value="<%= pet.get("id") %>">

        <div class="field">
            <label for="name">Pet Name</label>
            <input id="name" name="name" type="text"
                   value="<%= Web.escape(pet.get("name")) %>"
                   maxlength="50" required>
        </div>

        <div class="field">
            <label for="species">Species</label>
            <input id="species" name="species" type="text"
                   value="<%= Web.escape(pet.get("species")) %>"
                   maxlength="30" required>
        </div>

        <div class="field">
            <label for="breed_description">Breed or Description</label>
            <input id="breed_description" name="breed_description"
                   type="text"
                   value="<%= Web.escape(pet.get("breedDescription")) %>"
                   maxlength="100" required>
        </div>

        <div class="field">
            <label for="age">Age (years)</label>
            <input id="age" name="age" type="number"
                   value="<%= Web.escape(pet.get("age")) %>"
                   min="0" max="100" required>
        </div>

        <div class="field">
            <label for="size">Size</label>
            <select id="size" name="size" required>
                <option value="">Select size</option>
                <% for (String option : new String[]{"Small", "Medium", "Large"}) { %>
                    <option value="<%= option %>"
                        <%= option.equals(pet.get("size")) ? "selected" : "" %>>
                        <%= option %>
                    </option>
                <% } %>
            </select>
        </div>

        <div class="field">
            <label for="contact">Contact Name</label>
            <input id="contact" name="contact" type="text"
                   value="<%= Web.escape(pet.get("contact")) %>"
                   maxlength="100">
        </div>

        <div class="field">
            <label for="conditions">Medical Conditions or Notes</label>
            <textarea id="conditions" name="conditions"
                      maxlength="200" rows="3"><%= Web.escape(pet.get("conditions")) %></textarea>
        </div>

        <div class="field">
            <label for="medication_instructions">Medication Instructions</label>
            <textarea id="medication_instructions" name="medication_instructions"
                      maxlength="500" rows="3"><%= Web.escape(pet.get("medicationInstructions")) %></textarea>
        </div>

        <div class="field">
            <label for="feeding_instructions">Feeding Instructions</label>
            <textarea id="feeding_instructions" name="feeding_instructions"
                      maxlength="500" rows="3"><%= Web.escape(pet.get("feedingInstructions")) %></textarea>
        </div>

        <div class="field">
            <label for="behavior_notes">Behavior Notes</label>
            <textarea id="behavior_notes" name="behavior_notes"
                      maxlength="500" rows="3"><%= Web.escape(pet.get("behaviorNotes")) %></textarea>
        </div>

        <div class="field">
            <label for="vet_contact">Veterinary Contact</label>
            <input id="vet_contact" name="vet_contact" type="text"
                   value="<%= Web.escape(pet.get("vetContact")) %>"
                   maxlength="200">
        </div>

        <div class="field">
            <label for="photo">New Pet Photo (Optional)</label>
            <input id="photo" name="photo" type="file"
                   accept="image/jpeg,image/png">
        </div>

        <button class="button primary" type="submit">Save Changes</button>
    </form>

    <p class="form-footer">
        <a href="<%= request.getContextPath() %>/owner/pets">
            Back to My Pets
        </a>
    </p>
</main>

<%@ include file="foot.jspf" %>
