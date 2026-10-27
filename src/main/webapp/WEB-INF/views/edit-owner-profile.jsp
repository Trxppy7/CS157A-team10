<%@ include file="head.jspf" %>

<main id="main" class="auth-main">
    <div class="page-intro">
        <span class="eyebrow">Owner account</span>
        <h1>Edit Profile</h1>
        <p>Update your account information below.</p>
    </div>

    <% if (request.getAttribute("message") != null) { %>
        <p class="notice error" role="alert">
            <%= Web.escape(request.getAttribute("message")) %>
        </p>
    <% } %>

    <form class="surface" method="post"
          action="<%= request.getContextPath() %>/owner/edit-profile">

        <input type="hidden" name="csrf" value="<%= Web.csrf(request) %>">

        <div class="field">
            <label for="name">Name</label>
            <input id="name" name="name" type="text"
                   value="<%= Web.value(request, "name") %>"
                   maxlength="100" required>
        </div>

        <div class="field">
            <label for="email">Email</label>
            <input id="email" name="email" type="email"
                   value="<%= Web.value(request, "email") %>"
                   maxlength="100" required>
        </div>

        <div class="field">
            <label for="phone">Phone Number</label>
            <input id="phone" name="phone" type="tel"
                   value="<%= Web.value(request, "phone") %>"
                   maxlength="20" required>
        </div>

        <button class="button primary" type="submit">Save Changes</button>
    </form>

    <p class="form-footer">
        <a href="<%= request.getContextPath() %>/owner/home">
            Back to My Account
        </a>
    </p>
</main>

<%@ include file="foot.jspf" %>
