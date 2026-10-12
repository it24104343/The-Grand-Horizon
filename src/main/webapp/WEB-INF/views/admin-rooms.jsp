<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Manage rooms | The Grand Horizon</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body class="account-page">
<nav class="navbar site-nav"><div class="container">
    <a class="navbar-brand brand-mark" href="${pageContext.request.contextPath}/rooms">THE GRAND <span>HORIZON</span></a>
    <a class="nav-link" href="${pageContext.request.contextPath}/admin-dashboard.jsp">Admin dashboard</a>
</div></nav>
<main class="container py-5">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div><p class="eyebrow">ADMINISTRATION</p><h1>Manage rooms</h1></div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/rooms">View guest site</a>
    </div>
    <c:if test="${param.updated eq 'true'}">
        <div class="alert alert-success" role="status">Room changes saved.</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert"><c:out value="${error}"/></div>
    </c:if>

    <section class="card p-4 mb-5">
        <h2 class="h4">Create a room</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/rooms" class="row g-3">
            <input type="hidden" name="action" value="create">
            <div class="col-sm-6 col-lg-3">
                <label class="form-label" for="new-room-number">Room number</label>
                <input class="form-control" id="new-room-number" name="roomNumber" maxlength="20" required>
            </div>
            <div class="col-sm-6 col-lg-3">
                <label class="form-label" for="new-room-type">Type</label>
                <select class="form-select" id="new-room-type" name="roomType" required>
                    <option value="STANDARD">Standard</option>
                    <option value="LUXURY">Luxury</option>
                </select>
            </div>
            <div class="col-sm-6 col-lg-3">
                <label class="form-label" for="new-room-capacity">Capacity</label>
                <input class="form-control" id="new-room-capacity" name="capacity" type="number" min="1" max="50" required>
            </div>
            <div class="col-sm-6 col-lg-3">
                <label class="form-label" for="new-room-price">Price per night (Rs.)</label>
                <input class="form-control" id="new-room-price" name="pricePerNight" type="number" min="0.01" step="0.01" required>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="new-room-description">Description</label>
                <input class="form-control" id="new-room-description" name="description" maxlength="500" required>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="new-room-amenities">Amenities</label>
                <input class="form-control" id="new-room-amenities" name="amenities" maxlength="500" required>
            </div>
            <div class="col-12 form-check ms-2">
                <input class="form-check-input" id="new-room-available" name="available" type="checkbox" checked>
                <label class="form-check-label" for="new-room-available">Available for booking</label>
            </div>
            <div class="col-12"><button class="btn btn-gold" type="submit">Create room</button></div>
        </form>
    </section>

    <section>
        <h2 class="h4 mb-3">Existing rooms</h2>
        <c:forEach var="room" items="${rooms}">
            <article class="card p-4 mb-3">
                <form method="post" action="${pageContext.request.contextPath}/admin/rooms" class="row g-3 align-items-end">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" value="<c:out value='${room.id}'/>">
                    <div class="col-sm-6 col-lg-2">
                        <label class="form-label" for="room-number-${room.id}">Room number</label>
                        <input class="form-control" id="room-number-${room.id}" name="roomNumber" maxlength="20" value="<c:out value='${room.roomNumber}'/>" required>
                    </div>
                    <div class="col-sm-6 col-lg-2">
                        <label class="form-label" for="room-type-${room.id}">Type</label>
                        <select class="form-select" id="room-type-${room.id}" name="roomType">
                            <option value="STANDARD" ${room.roomType eq 'STANDARD' ? 'selected' : ''}>Standard</option>
                            <option value="LUXURY" ${room.roomType eq 'LUXURY' ? 'selected' : ''}>Luxury</option>
                        </select>
                    </div>
                    <div class="col-sm-6 col-lg-2">
                        <label class="form-label" for="room-capacity-${room.id}">Capacity</label>
                        <input class="form-control" id="room-capacity-${room.id}" name="capacity" type="number" min="1" max="50" value="<c:out value='${room.capacity}'/>" required>
                    </div>
                    <div class="col-sm-6 col-lg-2">
                        <label class="form-label" for="room-price-${room.id}">Price per night (Rs.)</label>
                        <input class="form-control" id="room-price-${room.id}" name="pricePerNight" type="number" min="0.01" step="0.01" value="<c:out value='${room.pricePerNight}'/>" required>
                    </div>
                    <div class="col-md-6 col-lg-2">
                        <label class="form-label" for="room-description-${room.id}">Description</label>
                        <input class="form-control" id="room-description-${room.id}" name="description" maxlength="500" value="<c:out value='${room.description}'/>" required>
                    </div>
                    <div class="col-md-6 col-lg-2">
                        <label class="form-label" for="room-amenities-${room.id}">Amenities</label>
                        <input class="form-control" id="room-amenities-${room.id}" name="amenities" maxlength="500" value="<c:out value='${room.amenities}'/>" required>
                    </div>
                    <div class="col-md-6 form-check ms-2">
                        <input class="form-check-input" id="room-available-${room.id}" name="available" type="checkbox" ${room.available ? 'checked' : ''}>
                        <label class="form-check-label" for="room-available-${room.id}">Available for booking</label>
                    </div>
                    <div class="col-12">
                        <button class="btn btn-gold" type="submit">Save changes</button>
                    </div>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/admin/rooms"
                      class="mt-2" onsubmit="return confirm('Delete this room?');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="<c:out value='${room.id}'/>">
                    <button class="btn btn-outline-danger" type="submit">Delete</button>
                </form>
            </article>
        </c:forEach>
        <c:if test="${empty rooms}">
            <p class="text-muted">There are no rooms to manage yet.</p>
        </c:if>
    </section>
</main>
</body>
</html>
