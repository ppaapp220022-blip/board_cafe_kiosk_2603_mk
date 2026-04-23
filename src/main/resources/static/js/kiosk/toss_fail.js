function retryPayment() {
    window.location.href = `/kiosk/checkout?tableNumber=${tableNumber}`;
}

function goMenu() {
    window.location.href = `/kiosk/games?tableNumber=${tableNumber}`;
}