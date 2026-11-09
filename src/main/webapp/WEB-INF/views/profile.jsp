<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Your profile | The Grand Horizon</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body class="account-page">
<nav class="navbar site-nav"><div class="container">
    <a class="navbar-brand brand-mark" href="${pageContext.request.contextPath}/rooms">THE GRAND <span>HORIZON</span></a>
    <a class="nav-link" href="${pageContext.request.contextPath}/rooms">Back to rooms</a>
</div></nav>
<main class="container account-wrap">
    <div class="profile-layout">
    <section class="account-card profile-card">
        <p class="eyebrow">GUEST ACCOUNT</p>
        <h1>Your profile</h1>
        <p class="muted-copy">Keep your contact details current for a seamless stay.</p>
        <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
        <c:if test="${not empty sessionScope.profileNotice}">
            <div class="alert alert-success" role="status"><c:out value="${sessionScope.profileNotice}"/></div>
            <c:remove var="profileNotice" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.bookingNotice}">
            <div class="alert alert-info" role="status"><c:out value="${sessionScope.bookingNotice}"/></div>
            <c:remove var="bookingNotice" scope="session"/>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/profile/update">
            <div class="row g-3">
                <div class="col-md-6"><label class="form-label" for="firstName">First name</label>
                    <input class="form-control" id="firstName" name="firstName" maxlength="80" required value="<c:out value='${user.firstName}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="lastName">Last name</label>
                    <input class="form-control" id="lastName" name="lastName" maxlength="80" required value="<c:out value='${user.lastName}'/>"></div>
                <div class="col-12"><label class="form-label" for="email">Email address</label>
                    <input class="form-control" id="email" name="email" type="email" maxlength="254" required value="<c:out value='${user.email}'/>"></div>
                <div class="col-12"><label class="form-label" for="phone">Phone number</label>
                    <input class="form-control" id="phone" name="phone" type="tel" maxlength="30" value="<c:out value='${user.phone}'/>"></div>
                <div class="col-12"><label class="form-label" for="newPassword">New password <span class="optional">(optional)</span></label>
                    <input class="form-control" id="newPassword" name="newPassword" type="password" minlength="8" autocomplete="new-password" placeholder="Leave blank to keep your current password"></div>
            </div>
            <button class="btn btn-gold w-100 mt-4" type="submit">Save changes</button>
        </form>
        <hr class="soft-divider">
        <details class="delete-account">
            <summary>Delete account</summary>
            <p class="small muted-copy mt-2">This permanently removes your account. Confirm with your current password.</p>
            <form method="post" action="${pageContext.request.contextPath}/profile/delete" class="d-flex gap-2 flex-column flex-sm-row">
                <input class="form-control" name="password" type="password" required autocomplete="current-password" placeholder="Current password" aria-label="Current password">
                <button class="btn btn-outline-danger flex-shrink-0" type="submit">Delete account</button>
            </form>
        </details>
        <form method="post" action="${pageContext.request.contextPath}/logout" class="mt-4 text-center">
            <button class="btn btn-link muted-copy" type="submit">Sign out</button>
        </form>
    </section>
    <c:if test="${sessionScope.role ne 'Admin' and sessionScope.role ne 'ADMIN'}">
        <section class="account-card profile-card">
            <p class="eyebrow">YOUR STAYS</p>
            <h2 class="profile-section-title">My Bookings</h2>
            <c:choose>
                <c:when test="${not empty bookings}">
                    <div class="booking-list">
                        <c:forEach var="booking" items="${bookings}">
                            <article class="booking-history-card">
                                <div class="d-flex flex-wrap justify-content-between gap-2">
                                    <h3 class="h5 mb-1">Room <c:out value="${booking.roomNumber}"/></h3>
                                    <c:choose>
                                        <c:when test="${booking.cancelled}"><span class="booking-status status-cancelled">Cancelled</span></c:when>
                                        <c:when test="${booking.completed}"><span class="booking-status status-completed">Completed</span></c:when>
                                        <c:otherwise><span class="booking-status status-active">Active</span></c:otherwise>
                                    </c:choose>
                                </div>
                                <p class="muted-copy mb-2">
                                    <c:out value="${booking.bookingDate}"/> at <c:out value="${booking.startTime}"/> ·
                                    <c:out value="${booking.durationHours}"/> hour(s) ·
                                    Rs. <c:out value="${booking.totalAmount}"/>
                                </p>
                                <c:if test="${booking.status eq 'ACTIVE' and not booking.completed}">
                                    <p class="booking-countdown">
                                        <c:choose>
                                            <c:when test="${booking.daysRemaining gt 0}"><c:out value="${booking.daysRemaining}"/> day(s) remaining</c:when>
                                            <c:otherwise>Ends today</c:otherwise>
                                        </c:choose>
                                    </p>
                                </c:if>
                                <c:if test="${booking.cancellable}">
                                    <form method="post" action="${pageContext.request.contextPath}/booking/cancel"
                                          onsubmit="return confirm('Cancel this upcoming booking?');">
                                        <input type="hidden" name="bookingId" value="<c:out value='${booking.id}'/>">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                        <button class="btn btn-outline-danger btn-sm" type="submit">Cancel booking</button>
                                    </form>
                                </c:if>
                            </article>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise><p class="muted-copy mb-0">You do not have any bookings yet.</p></c:otherwise>
            </c:choose>
        </section>
    </c:if>
    </div>
</main>
</body>
</html>
