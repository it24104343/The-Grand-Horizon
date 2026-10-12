<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Book a room | The Grand Horizon</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body class="account-page">
<nav class="navbar site-nav"><div class="container">
    <a class="navbar-brand brand-mark" href="${pageContext.request.contextPath}/rooms">THE GRAND <span>HORIZON</span></a>
    <a class="nav-link" href="${pageContext.request.contextPath}/rooms">Back to rooms</a>
</div></nav>
<main class="container account-wrap">
    <section class="account-card">
        <p class="eyebrow">MAKE IT YOUR STAY</p>
        <h1>Reserve room <c:out value="${room.roomNumber}"/>.</h1>
        <p class="muted-copy">Nightly rate: <strong>Rs. <c:out value="${room.pricePerNight}"/></strong></p>
        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert"><c:out value="${error}"/></div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/booking">
            <input type="hidden" name="roomId" value="<c:out value='${room.id}'/>">
            <input type="hidden" name="roomPrice" value="<c:out value='${room.pricePerNight}'/>">
            <div class="mb-3">
                <label class="form-label" for="checkIn">Check-in date</label>
                <input class="form-control" id="checkIn" name="checkIn" type="date"
                       min="<c:out value='${today}'/>" value="<c:out value='${param.checkIn}'/>" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="checkOut">Check-out date</label>
                <input class="form-control" id="checkOut" name="checkOut" type="date"
                       min="<c:out value='${today}'/>" value="<c:out value='${param.checkOut}'/>" required>
            </div>
            <button class="btn btn-gold w-100 mt-3" type="submit">Confirm booking</button>
        </form>
    </section>
</main>
</body>
</html>
