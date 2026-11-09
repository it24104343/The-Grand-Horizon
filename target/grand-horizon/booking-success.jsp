<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Booking confirmed | The Grand Horizon</title>
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
        <p class="eyebrow">YOUR STAY IS CONFIRMED</p>
        <h1>We look forward<br>to welcoming you.</h1>
        <c:if test="${not empty emailNotice}">
            <div class="alert alert-warning" role="status"><c:out value="${emailNotice}"/></div>
        </c:if>
        <p>Thank you, <c:out value="${booking.guestName}"/>. Room <c:out value="${booking.roomNumber}"/> is booked.</p>
        <p>Date: <strong><c:out value="${booking.bookingDate}"/></strong></p>
        <p>Start time: <strong><c:out value="${booking.startTime}"/></strong></p>
        <p>Duration: <strong><c:out value="${booking.durationHours}"/> hour(s)</strong></p>
        <c:if test="${not empty booking.mealOptions}">
            <p>Selected meals: <strong><c:out value="${booking.mealsDisplay}"/></strong></p>
        </c:if>
        <p class="room-price">Total amount: <strong>Rs. <c:out value="${booking.totalAmount}"/></strong></p>
        <a class="btn btn-outline-secondary w-100 mt-2" href="${pageContext.request.contextPath}/profile">My profile and bookings</a>
        <a class="btn btn-gold w-100 mt-3" href="${pageContext.request.contextPath}/rooms">Back to rooms</a>
    </section>
</main>
</body>
</html>
