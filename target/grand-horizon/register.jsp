<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Create an account | The Grand Horizon</title>
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
        <p class="eyebrow">BECOME OUR GUEST</p><h1>Make yourself<br>at home.</h1>
        <p class="muted-copy">Create an account to make your next stay yours.</p>
        <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/register">
            <div class="row g-3">
                <div class="col-sm-6"><label class="form-label" for="firstName">First name</label>
                    <input class="form-control" id="firstName" name="firstName" maxlength="80" required autocomplete="given-name" value="<c:out value='${param.firstName}'/>"></div>
                <div class="col-sm-6"><label class="form-label" for="lastName">Last name</label>
                    <input class="form-control" id="lastName" name="lastName" maxlength="80" required autocomplete="family-name" value="<c:out value='${param.lastName}'/>"></div>
                <div class="col-12"><label class="form-label" for="email">Email address</label>
                    <input class="form-control" id="email" name="email" type="email" maxlength="254" required autocomplete="email" value="<c:out value='${param.email}'/>"></div>
                <div class="col-12"><label class="form-label" for="phone">Phone number <span class="optional">(optional)</span></label>
                    <input class="form-control" id="phone" name="phone" type="tel" maxlength="30" autocomplete="tel" value="<c:out value='${param.phone}'/>"></div>
                <div class="col-sm-6"><label class="form-label" for="password">Password</label>
                    <input class="form-control" id="password" name="password" type="password" minlength="8" required autocomplete="new-password"></div>
                <div class="col-sm-6"><label class="form-label" for="confirmPassword">Confirm password</label>
                    <input class="form-control" id="confirmPassword" name="confirmPassword" type="password" minlength="8" required autocomplete="new-password"></div>
            </div>
            <button class="btn btn-gold w-100 mt-4" type="submit">Create your account</button>
        </form>
        <p class="account-switch">Already a guest? <a href="${pageContext.request.contextPath}/login">Sign in</a></p>
    </section>
</main>
</body>
</html>
