<%@ include file="head.jspf" %>
<main id="main" class="auth-main">
  <div class="page-intro"><span class="eyebrow">Join PawMatch</span><h1>Create an account</h1><p>A place for pet owners and the people who care for them.</p></div>
  <form class="surface" method="post" action="<%= request.getContextPath() %>/register">
    <fieldset class="role-group">
      <legend>I am a…</legend>
      <div class="role-options">
        <label class="role-choice"><input type="radio" name="role" value="owner" required><span><strong>Pet owner</strong><small>I need care for my pet.</small></span></label>
        <label class="role-choice"><input type="radio" name="role" value="caregiver" required><span><strong>Caregiver</strong><small>I care for other people's pets.</small></span></label>
      </div>
    </fieldset>
    <div class="field"><label for="name">Name</label><input id="name" name="name" autocomplete="name" maxlength="100" required></div>
    <div class="field"><label for="email">Email</label><input id="email" name="email" type="email" autocomplete="email" maxlength="100" required></div>
    <div class="field"><label for="phone">Phone</label><input id="phone" name="phone" type="tel" autocomplete="tel" maxlength="20" required></div>
    <div class="field"><label for="password">Password</label><input id="password" name="password" type="password" autocomplete="new-password" minlength="8" maxlength="128" aria-describedby="password-help" required><small id="password-help">Use at least 8 characters.</small></div>
    <button class="button primary" type="submit">Create account</button>
  </form>
</main>
<%@ include file="foot.jspf" %>
