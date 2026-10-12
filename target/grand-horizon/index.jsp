<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Discover considered comfort and thoughtful hospitality at The Grand Horizon.">
    <title>The Grand Horizon | A stay above the ordinary</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Playfair+Display:wght@500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg site-nav">
    <div class="container">
        <a class="navbar-brand brand-mark" href="${pageContext.request.contextPath}/rooms">THE GRAND <span>HORIZON</span></a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <div class="navbar-nav ms-auto align-items-lg-center gap-lg-4">
                <a class="nav-link" href="#rooms">The rooms</a>
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <a class="nav-link" href="${pageContext.request.contextPath}/profile">My profile</a>
                    </c:when>
                    <c:when test="${not empty sessionScope.username}">
                        <span class="nav-link">Welcome, <c:out value="${sessionScope.username}"/></span>
                    </c:when>
                    <c:otherwise>
                        <a class="nav-link" href="${pageContext.request.contextPath}/login">Sign in</a>
                        <a class="btn btn-nav" href="${pageContext.request.contextPath}/register">Join us</a>
                    </c:otherwise>
                </c:choose>
                <c:if test="${sessionScope.role eq 'Admin'}">
                    <a class="nav-link" href="${pageContext.request.contextPath}/admin/rooms">Manage rooms</a>
                </c:if>
            </div>
        </div>
    </div>
</nav>
<header class="hero">
    <div class="hero-shade"></div>
    <div class="container hero-content">
        <p class="eyebrow">A QUIETER KIND OF LUXURY</p>
        <h1>Arrive somewhere<br><em>exceptional.</em></h1>
        <p class="hero-copy">Thoughtful rooms. Unhurried mornings. A view that stays with you.</p>
        <a class="btn btn-gold" href="#rooms">Explore our rooms <span aria-hidden="true">↗</span></a>
    </div>
    <div class="hero-caption">A PLACE TO PAUSE &nbsp;·&nbsp; A PLACE TO BELONG</div>
</header>
<main>
    <section class="intro-strip">
        <div class="container d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
            <p class="eyebrow mb-0">THE GRAND HORIZON — EST. 1998</p>
            <p class="intro-note mb-0">Hospitality shaped around the way you want to feel.</p>
        </div>
    </section>
    <section class="rooms-section" id="rooms">
        <div class="container">
            <div class="section-heading">
                <div><p class="eyebrow">STAY A LITTLE LONGER</p><h2>Rooms with room<br>to breathe.</h2></div>
                <p class="muted-copy section-aside">Each space is designed with intention—natural textures, considered details, and comfort that feels like your own.</p>
            </div>
            <c:if test="${param.accountDeleted eq 'true'}">
                <div class="alert alert-success" role="status">Your account has been deleted.</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-warning" role="alert"><c:out value="${error}"/></div>
            </c:if>
            <div class="row g-4">
                <c:forEach var="room" items="${rooms}">
                    <div class="col-md-6">
                        <article class="room-card">
                            <div class="room-image ${room.roomType eq 'LUXURY' ? 'room-image-luxury' : 'room-image-standard'}">
                                <span class="room-tag"><c:out value="${room.roomType eq 'LUXURY' ? 'THE SUITE COLLECTION' : 'THE HORIZON COLLECTION'}"/></span>
                                <span class="room-number">ROOM <c:out value="${room.roomNumber}"/></span>
                            </div>
                            <div class="room-details">
                                <div class="d-flex justify-content-between align-items-start gap-3">
                                    <div><h3><c:out value="${room.roomType eq 'LUXURY' ? 'Horizon Suite' : 'Signature Room'}"/></h3>
                                        <p class="muted-copy mb-2"><c:out value="${room.description}"/></p></div>
                                    <p class="room-price"><strong>Rs. <c:out value="${room.pricePerNight}"/></strong><span> / night</span></p>
                                </div>
                                <p class="room-meta"><c:out value="${room.capacity}"/> guests <span>·</span> <c:out value="${room.amenities}"/></p>
                                <c:choose>
                                    <c:when test="${not empty sessionScope.userId or not empty sessionScope.username}">
                                        <a class="text-link" href="${pageContext.request.contextPath}/booking?roomId=${room.id}">Book this room <span aria-hidden="true">→</span></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="text-link" href="${pageContext.request.contextPath}/login">Sign in to book <span aria-hidden="true">→</span></a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </article>
                    </div>
                </c:forEach>
                <c:if test="${empty rooms}">
                    <div class="col-12"><div class="empty-rooms">Room availability is being refreshed. Please check back shortly.</div></div>
                </c:if>
            </div>
        </div>
    </section>
</main>
<footer class="site-footer">
    <div class="container d-flex flex-column flex-md-row justify-content-between gap-2">
        <a class="brand-mark footer-brand" href="${pageContext.request.contextPath}/rooms">THE GRAND <span>HORIZON</span></a>
        <p class="mb-0">A considered stay, wherever the day takes you.</p>
        <small>© 2026 The Grand Horizon</small>
    </div>
</footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
