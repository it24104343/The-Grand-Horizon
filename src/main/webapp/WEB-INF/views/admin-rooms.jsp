<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
        <div class="d-flex gap-2">
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/rooms">View guest site</a>
            <a class="btn btn-gold" href="#create-room"><span aria-hidden="true">＋</span> Create new room</a>
        </div>
    </div>
    <c:if test="${param.updated eq 'true'}">
        <div class="alert alert-success" role="status">Room changes saved.</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert"><c:out value="${error}"/></div>
    </c:if>

    <section class="card p-4 p-lg-5 mb-5" id="create-room">
        <h2 class="h4 mb-1">Create a room</h2>
        <p class="muted-copy mb-4">Set its room details, base nightly price, image and meal options.</p>
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/rooms" class="row g-3">
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
                <label class="form-label" for="new-room-price">Base price per night (Rs.)</label>
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
            <div class="col-md-6">
                <label class="form-label" for="new-room-image">Room image <span class="optional">(optional, PNG or JPEG up to 5 MB)</span></label>
                <input class="form-control" id="new-room-image" name="roomImage" type="file" accept="image/png,image/jpeg">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="new-room-meals">Meals available for this room</label>
                <select class="form-select" id="new-room-meals" name="mealOptions" multiple size="3">
                    <option value="BREAKFAST">Breakfast</option>
                    <option value="LUNCH">Lunch</option>
                    <option value="DINNER">Dinner</option>
                </select>
                <small class="form-text text-muted">Use Ctrl (Windows) or Command (Mac) to select more than one. Leave blank for room-only.</small>
            </div>
            <div class="col-12 form-check ms-2">
                <input class="form-check-input" id="new-room-available" name="available" type="checkbox" checked>
                <label class="form-check-label" for="new-room-available">Available for booking</label>
            </div>
            <div class="col-12"><button class="btn btn-gold" type="submit">Create room</button></div>
        </form>
    </section>

    <section>
        <div class="d-flex justify-content-between align-items-baseline mb-3">
            <h2 class="h4">Existing rooms</h2>
            <span class="text-muted small"><c:out value="${fn:length(rooms)}"/> rooms</span>
        </div>
        <div class="row g-4">
            <c:forEach var="room" items="${rooms}">
                <div class="col-xl-6">
                    <article class="card h-100 p-4 p-lg-4">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div>
                                <span class="room-tag admin-room-tag"><c:out value="${room.roomType}"/></span>
                                <h3 class="h5 mt-3 mb-1">Room <c:out value="${room.roomNumber}"/></h3>
                            </div>
                            <c:if test="${not empty room.imageFileName}">
                                <img class="admin-room-thumbnail" src="${pageContext.request.contextPath}/room-image?file=${room.imageFileName}" alt="Room <c:out value='${room.roomNumber}'/>">
                            </c:if>
                        </div>
                        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/rooms" class="row g-3 align-items-end">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="id" value="<c:out value='${room.id}'/>">
                            <div class="col-sm-6">
                                <label class="form-label" for="room-number-${room.id}">Room number</label>
                                <input class="form-control" id="room-number-${room.id}" name="roomNumber" maxlength="20" value="<c:out value='${room.roomNumber}'/>" required>
                            </div>
                            <div class="col-sm-6">
                                <label class="form-label" for="room-type-${room.id}">Type</label>
                                <select class="form-select" id="room-type-${room.id}" name="roomType" required>
                                    <option value="STANDARD" ${room.roomType eq 'STANDARD' ? 'selected' : ''}>Standard</option>
                                    <option value="LUXURY" ${room.roomType eq 'LUXURY' ? 'selected' : ''}>Luxury</option>
                                </select>
                            </div>
                            <div class="col-sm-6">
                                <label class="form-label" for="room-capacity-${room.id}">Capacity</label>
                                <input class="form-control" id="room-capacity-${room.id}" name="capacity" type="number" min="1" max="50" value="<c:out value='${room.capacity}'/>" required>
                            </div>
                            <div class="col-sm-6">
                                <label class="form-label" for="room-price-${room.id}">Base price per night (Rs.)</label>
                                <input class="form-control" id="room-price-${room.id}" name="pricePerNight" type="number" min="0.01" step="0.01" value="<c:out value='${room.pricePerNight}'/>" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="room-description-${room.id}">Description</label>
                                <input class="form-control" id="room-description-${room.id}" name="description" maxlength="500" value="<c:out value='${room.description}'/>" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="room-amenities-${room.id}">Amenities</label>
                                <input class="form-control" id="room-amenities-${room.id}" name="amenities" maxlength="500" value="<c:out value='${room.amenities}'/>" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="room-image-${room.id}">Replace image <span class="optional">(optional)</span></label>
                                <input class="form-control" id="room-image-${room.id}" name="roomImage" type="file" accept="image/png,image/jpeg">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="room-meals-${room.id}">Meals available</label>
                                <select class="form-select" id="room-meals-${room.id}" name="mealOptions" multiple size="3">
                                    <option value="BREAKFAST" ${room.breakfastAvailable ? 'selected' : ''}>Breakfast</option>
                                    <option value="LUNCH" ${room.lunchAvailable ? 'selected' : ''}>Lunch</option>
                                    <option value="DINNER" ${room.dinnerAvailable ? 'selected' : ''}>Dinner</option>
                                </select>
                            </div>
                            <div class="col-12 form-check ms-2">
                                <input class="form-check-input" id="room-available-${room.id}" name="available" type="checkbox" ${room.available ? 'checked' : ''}>
                                <label class="form-check-label" for="room-available-${room.id}">Available for booking</label>
                            </div>
                            <div class="col-12 d-flex flex-wrap gap-2">
                                <button class="btn btn-gold" type="submit">Save changes</button>
                                <button class="btn btn-outline-danger" type="submit" form="delete-room-${room.id}">Delete room</button>
                            </div>
                        </form>
                        <form id="delete-room-${room.id}" method="post" action="${pageContext.request.contextPath}/admin/rooms" onsubmit="return confirm('Delete this room?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<c:out value='${room.id}'/>">
                        </form>
                    </article>
                </div>
            </c:forEach>
            <c:if test="${empty rooms}">
                <div class="col-12"><p class="text-muted">There are no rooms to manage yet.</p></div>
            </c:if>
        </div>
    </section>
</main>
</body>
</html>
