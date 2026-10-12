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
<main class="container account-wrap">
    <section class="account-card">
        <p class="eyebrow">YOUR STAY IS CONFIRMED</p>
        <h1>We look forward<br>to welcoming you.</h1>
        <p>Thank you, <c:out value="${username}"/>. Room <c:out value="${roomName}"/> is booked.</p>
        <p>Check-in: <strong><c:out value="${checkIn}"/></strong></p>
        <p>Check-out: <strong><c:out value="${checkOut}"/></strong></p>
        <p class="room-price">Total amount: <strong>Rs. <c:out value="${totalAmount}"/></strong></p>
        <a class="btn btn-gold w-100 mt-3" href="${pageContext.request.contextPath}/rooms">Back to rooms</a>
    </section>
</main>
</body>
</html>
