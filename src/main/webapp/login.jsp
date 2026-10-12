<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in | The Grand Horizon</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body class="account-page">
<nav class="navbar site-nav"><div class="container">
    <a class="navbar-brand brand-mark" href="${pageContext.request.contextPath}/rooms">THE GRAND <span>HORIZON</span></a>
    <a class="nav-link" href="${pageContext.request.contextPath}/rooms">Back to the hotel</a>
</div></nav>
<main class="container account-wrap">
    <section class="account-card">
        <p class="eyebrow">WELCOME BACK</p><h1>Your stay<br>starts here.</h1>
        <p class="muted-copy">Sign in to manage your stay.</p>
        <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="mb-3"><label class="form-label" for="usernameOrEmail">Username or email</label>
                <input class="form-control" id="usernameOrEmail" name="usernameOrEmail" type="text" maxlength="254" required autocomplete="username"></div>
            <div class="mb-2"><label class="form-label" for="password">Password</label>
                <input class="form-control" id="password" name="password" type="password" required autocomplete="current-password"></div>
            <button class="btn btn-gold w-100 mt-4" type="submit">Sign in</button>
        </form>
        <p class="account-switch">New to The Grand Horizon? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
    </section>
</main>
</body>
</html>
