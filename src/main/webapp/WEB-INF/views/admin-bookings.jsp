<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bookings | The Grand Horizon</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body class="account-page">
<%-- Admin booking management page. It lists active and cancelled reservations so the administrator can monitor hotel usage. --%>
<nav class="navbar site-nav"><div class="container">
    <a class="navbar-brand brand-mark" href="${pageContext.request.contextPath}/rooms">THE GRAND <span>HORIZON</span></a>
    <a class="nav-link" href="${pageContext.request.contextPath}/rooms">Back to rooms</a>
</div></nav>
<main class="container py-5">
    <header class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
        <div><p class="eyebrow mb-2">ADMINISTRATION</p><h1 class="page-title">Bookings</h1></div>
        <span class="booking-status status-active"><c:out value="${bookingCount}"/> current</span>
    </header>
    <section class="booking-admin-section" id="current-bookings">
        <h2 class="h4 mb-3">Current bookings</h2>
        <c:choose>
            <c:when test="${not empty bookings}">
                <div class="table-responsive bg-white">
                    <table class="table table-hover align-middle mb-0">
                        <thead><tr>
                            <th>Guest Name</th><th>Room Number</th><th>Date</th>
                            <th>Time</th><th>Duration</th><th>Total Paid</th>
                        </tr></thead>
                        <tbody>
                        <c:forEach var="booking" items="${bookings}">
                            <tr>
                                <td><strong><c:out value="${booking.guestName}"/></strong><br>
                                    <small class="text-muted"><c:out value="${booking.guestEmail}"/></small></td>
                                <td><c:out value="${booking.roomNumber}"/></td>
                                <td><c:out value="${booking.bookingDate}"/></td>
                                <td><c:out value="${booking.startTimeAmPm}"/></td>
                                <td><c:out value="${booking.durationHours}"/> hour(s)</td>
                                <td>Rs. <c:out value="${booking.totalAmount}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise><p class="empty-rooms">There are no current bookings.</p></c:otherwise>
        </c:choose>
    </section>
    <section class="booking-admin-section mt-5" id="cancelled-bookings">
        <h2 class="h4 mb-3">Cancelled bookings</h2>
        <c:choose>
            <c:when test="${not empty cancelledBookings}">
                <div class="table-responsive bg-white">
                    <table class="table table-hover align-middle mb-0">
                        <thead><tr>
                            <th>Guest Name</th><th>Room Number</th><th>Date</th><th>Time</th><th>Duration</th><th>Status</th>
                        </tr></thead>
                        <tbody>
                        <c:forEach var="booking" items="${cancelledBookings}">
                            <tr>
                                <td><strong><c:out value="${booking.guestName}"/></strong><br>
                                    <small class="text-muted"><c:out value="${booking.guestEmail}"/></small></td>
                                <td><c:out value="${booking.roomNumber}"/></td>
                                <td><c:out value="${booking.bookingDate}"/></td>
                                <td><c:out value="${booking.startTimeAmPm}"/></td>
                                <td><c:out value="${booking.durationHours}"/> hour(s)</td>
                                <td>Cancelled</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise><p class="empty-rooms">No bookings have been cancelled.</p></c:otherwise>
        </c:choose>
    </section>
</main>
</body>
</html>
