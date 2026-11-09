<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Room details | The Grand Horizon</title>
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
        <p class="eyebrow">ROOM DETAILS &amp; AVAILABILITY</p>
        <h1>Room <c:out value="${room.roomNumber}"/>.</h1>
        <p class="muted-copy"><c:out value="${room.description}"/></p>
        <p class="room-price mb-4">Hourly rate: <strong>Rs. <c:out value="${room.hourlyRate}"/></strong><span> / hour</span></p>
        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert"><c:out value="${error}"/></div>
        </c:if>
        <form id="booking-form" method="post" action="${pageContext.request.contextPath}/booking">
            <input type="hidden" name="roomId" value="<c:out value='${room.id}'/>">
            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
            <div class="mb-3">
                <label class="form-label" for="bookingDate">Date</label>
                <input class="form-control" id="bookingDate" name="date" type="date"
                       min="<c:out value='${minimumDate}'/>" value="<c:out value='${param.date}'/>" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="startTime">Start time</label>
                <input class="form-control" id="startTime" name="startTime" type="time"
                       value="<c:out value='${param.startTime}'/>" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="durationHours">Duration (hours)</label>
                <input class="form-control" id="durationHours" name="durationHours" type="number"
                       min="1" max="72" step="1" value="<c:out value='${param.durationHours}'/>" required>
            </div>
            <c:if test="${not empty room.mealOptions}">
                <fieldset class="meal-options-fieldset mb-3">
                    <legend class="form-label">Optional meals for your stay</legend>
                    <div class="meal-options">
                        <c:if test="${room.breakfastAvailable}">
                            <label class="meal-option" for="booking-breakfast">
                                <input id="booking-breakfast" name="mealOptions" type="checkbox" value="BREAKFAST" data-price="500" ${selectedBreakfast ? 'checked' : ''}>
                                <span>Breakfast</span><strong>Rs. 500</strong>
                            </label>
                        </c:if>
                        <c:if test="${room.lunchAvailable}">
                            <label class="meal-option" for="booking-lunch">
                                <input id="booking-lunch" name="mealOptions" type="checkbox" value="LUNCH" data-price="800" ${selectedLunch ? 'checked' : ''}>
                                <span>Lunch</span><strong>Rs. 800</strong>
                            </label>
                        </c:if>
                        <c:if test="${room.dinnerAvailable}">
                            <label class="meal-option" for="booking-dinner">
                                <input id="booking-dinner" name="mealOptions" type="checkbox" value="DINNER" data-price="1000" ${selectedDinner ? 'checked' : ''}>
                                <span>Dinner</span><strong>Rs. 1,000</strong>
                            </label>
                        </c:if>
                    </div>
                    <small class="form-text text-muted">Meal charges are added once to your stay total.</small>
                </fieldset>
            </c:if>
            <div id="availability-message" class="booking-availability mb-3 <c:out value='${availabilityClass}'/>"
                 role="status" aria-live="polite">
                <c:choose>
                    <c:when test="${not empty availabilityMessage}">
                        <c:out value="${availabilityMessage}"/>
                    </c:when>
                    <c:otherwise>Select a date, start time, and duration, then check availability.</c:otherwise>
                </c:choose>
            </div>
            <div id="booking-total" class="booking-total d-flex justify-content-between align-items-baseline d-none" hidden>
                <span>Full Total Price</span>
                <strong>Rs. <span id="estimated-total"></span></strong>
            </div>
            <p id="price-note" class="price-note mt-2 d-none" hidden>
                Hourly room rate × selected hours, plus selected meal charges. The total is verified when you confirm.
            </p>
            <button id="check-availability" class="btn btn-outline-secondary w-100 mt-3" type="button">
                Check Availability
            </button>
            <c:choose>
                <c:when test="${not empty sessionScope.userId and sessionScope.role ne 'Admin' and sessionScope.role ne 'ADMIN'}">
                    <button id="confirm-booking" class="btn btn-gold w-100 mt-3 d-none" type="submit" disabled hidden>
                        Confirm booking
                    </button>
                </c:when>
                <c:otherwise>
                    <div class="d-grid gap-2 mt-3">
                        <a id="sign-in-to-book" class="btn btn-gold d-none" aria-disabled="true" hidden
                           href="${pageContext.request.contextPath}/login">Sign in to book</a>
                        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/register">Join us / Create an account</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </form>
    </section>
</main>
<script>
(() => {
    const form = document.getElementById('booking-form');
    const bookingDate = document.getElementById('bookingDate');
    const startTime = document.getElementById('startTime');
    const durationHours = document.getElementById('durationHours');
    const availability = document.getElementById('availability-message');
    const checkButton = document.getElementById('check-availability');
    const bookingTotal = document.getElementById('booking-total');
    const priceNote = document.getElementById('price-note');
    const total = document.getElementById('estimated-total');
    const submit = document.getElementById('confirm-booking');
    const signIn = document.getElementById('sign-in-to-book');
    const contextPath = '${pageContext.request.contextPath}';
    const roomId = form.elements.roomId.value;
    let checkSequence = 0;

    function setVisible(element, visible) {
        if (!element) return;
        element.classList.toggle('d-none', !visible);
        element.hidden = !visible;
    }

    function selectedStart() {
        return bookingDate.value && startTime.value
            ? new Date(bookingDate.value + 'T' + startTime.value)
            : null;
    }

    function resetAvailability() {
        checkSequence++;
        checkButton.disabled = false;
        checkButton.textContent = 'Check Availability';
        availability.textContent = 'Select a date, start time, and duration, then check availability.';
        availability.className = 'booking-availability mb-3';
        total.textContent = '';
        setVisible(bookingTotal, false);
        setVisible(priceNote, false);
        setVisible(submit, false);
        if (submit) submit.disabled = true;
        setVisible(signIn, false);
    }

    function updateSignInLink() {
        if (!signIn) return;
        const target = new URLSearchParams({
            roomId,
            date: bookingDate.value,
            startTime: startTime.value,
            durationHours: durationHours.value
        });
        selectedMealInputs().forEach((input) => target.append('mealOptions', input.value));
        signIn.href = contextPath + '/login?redirect='
            + encodeURIComponent('/booking?' + target.toString());
    }

    function selectedMealInputs() {
        return Array.from(form.querySelectorAll('input[name="mealOptions"]:checked'));
    }

    async function checkAvailability() {
        const sequence = ++checkSequence;
        updateSignInLink();
        setVisible(submit, false);
        if (submit) submit.disabled = true;
        setVisible(signIn, false);
        setVisible(bookingTotal, false);
        setVisible(priceNote, false);

        const start = selectedStart();
        const hours = Number(durationHours.value);
        if (!start || !Number.isInteger(hours) || hours < 1 || hours > 72) {
            availability.textContent = 'Enter a date, start time, and duration from 1 to 72 hours.';
            availability.className = 'booking-availability booking-unavailable mb-3';
            return;
        }
        if (Number.isNaN(start.getTime()) || start <= new Date()) {
            availability.textContent = 'Choose a future date and start time.';
            availability.className = 'booking-availability booking-unavailable mb-3';
            return;
        }

        checkButton.disabled = true;
        checkButton.textContent = 'Checking...';
        availability.textContent = 'Checking availability...';
        availability.className = 'booking-availability mb-3';
        const query = new URLSearchParams({
            roomId,
            date: bookingDate.value,
            startTime: startTime.value,
            durationHours: String(hours)
        });
        selectedMealInputs().forEach((input) => query.append('mealOptions', input.value));
        try {
            const response = await fetch(contextPath + '/booking/availability?' + query.toString(),
                { headers: { Accept: 'application/json' }, cache: 'no-store' });
            const result = await response.json();
            if (sequence !== checkSequence) return;
            checkButton.disabled = false;
            checkButton.textContent = 'Check Availability';
            availability.textContent = result.message;
            availability.className = 'booking-availability mb-3 '
                + (result.available ? 'booking-available' : 'booking-unavailable');
            if (result.available) {
                total.textContent = Number(result.total).toFixed(2);
                setVisible(bookingTotal, true);
                setVisible(priceNote, true);
                setVisible(submit, true);
                if (submit) submit.disabled = false;
                if (signIn) {
                    signIn.href = contextPath + '/login?redirect='
                        + encodeURIComponent('/booking?' + query.toString());
                    signIn.setAttribute('aria-disabled', 'false');
                    setVisible(signIn, true);
                }
            }
        } catch (error) {
            if (sequence !== checkSequence) return;
            checkButton.disabled = false;
            checkButton.textContent = 'Check Availability';
            availability.textContent = 'Availability could not be checked. Please try again.';
            availability.className = 'booking-availability booking-unavailable mb-3';
        }
    }

    form.addEventListener('input', resetAvailability);
    form.addEventListener('change', resetAvailability);
    checkButton.addEventListener('click', checkAvailability);
    form.addEventListener('submit', (event) => {
        if (!submit || submit.disabled || submit.hidden) event.preventDefault();
    });
})();
</script>
</body>
</html>
