<?php
function generateTrackingNumber() {
    return 'CF-' . date('Ymd') . '-' . strtoupper(substr(uniqid(), -6));
}

function redirect($url) {
    header("Location: $url");
    exit();
}

function displayMessage($type, $message) {
    $class = $type === 'success' ? 'bg-green-100 text-green-700 border-green-200' : 'bg-red-100 text-red-700 border-red-200';
    return "<div class='$class border rounded-lg p-4 mb-4'>$message</div>";
}
?>