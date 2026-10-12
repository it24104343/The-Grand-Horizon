<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin dashboard | The Grand Horizon</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body class="account-page">
<nav class="navbar site-nav"><div class="container">
    <a class="navbar-brand brand-mark" href="${pageContext.request.contextPath}/rooms">THE GRAND <span>HORIZON</span></a>
    <form method="post" action="${pageContext.request.contextPath}/logout">
        <button class="nav-link border-0 bg-transparent" type="submit">Sign out</button>
    </form>
</div></nav>
<main class="container account-wrap">
    <section class="account-card">
        <p class="eyebrow">ADMINISTRATION</p>
        <h1>Welcome, <c:out value="${sessionScope.username}"/>.</h1>
        <p class="muted-copy">You are signed in with the Admin role.</p>
        <a class="btn btn-gold w-100 mt-3" href="${pageContext.request.contextPath}/admin/rooms">Create, update, or delete rooms</a>
        <a class="btn btn-gold w-100 mt-3" href="${pageContext.request.contextPath}/rooms">View rooms</a>
    </section>
</main>
</body>
</html>
