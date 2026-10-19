<?php
// pages/google_login.php - verifies a Google ID token and signs the user in
require_once '../Database.php';
require_once '../includes/google_config.php';
if (session_status() === PHP_SESSION_NONE) { session_start(); }
header('Content-Type: application/json');

function fail($msg, $code = 400) {
    http_response_code($code);
    echo json_encode(['ok' => false, 'error' => $msg]);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') { fail('Invalid request', 405); }
if (GOOGLE_CLIENT_ID === 'PASTE_YOUR_GOOGLE_CLIENT_ID_HERE') { fail('Google sign-in is not configured yet', 500); }

$input = json_decode(file_get_contents('php://input'), true);
$idToken = $input['credential'] ?? '';
if ($idToken === '') { fail('Missing Google credential'); }

// Ask Google to validate the token (signature and expiry are checked by Google)
$ch = curl_init('https://oauth2.googleapis.com/tokeninfo?id_token=' . urlencode($idToken));
curl_setopt_array($ch, [CURLOPT_RETURNTRANSFER => true, CURLOPT_TIMEOUT => 10]);
$resp = curl_exec($ch);
$status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
if ($resp === false) { error_log('Google tokeninfo cURL error: ' . curl_error($ch)); }
curl_close($ch);
if ($resp === false || $status !== 200) { fail('Could not verify your Google sign-in', 401); }

$p = json_decode($resp, true);
$verified = ($p['email_verified'] ?? '') === 'true' || ($p['email_verified'] ?? false) === true;
if (($p['aud'] ?? '') !== GOOGLE_CLIENT_ID) { fail('Token was not issued for this application', 401); }
if (!in_array($p['iss'] ?? '', ['accounts.google.com', 'https://accounts.google.com'], true)) { fail('Invalid token issuer', 401); }
if (!$verified || empty($p['email'])) { fail('Your Google email is not verified', 401); }

$email = $p['email'];
$name = $p['name'] ?? $email;

try {
    $database = new Database();
    $pdo = $database->getConnection();

    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();

    if (!$user) {
        // First Google sign-in: create the account with an unusable random password
        $randomHash = password_hash(bin2hex(random_bytes(16)), PASSWORD_DEFAULT);
        $ins = $pdo->prepare("INSERT INTO users (full_name, email, phone, password, role) VALUES (?, ?, '', ?, 'community_member')");
        $ins->execute([$name, $email, $randomHash]);
        $stmt->execute([$email]);
        $user = $stmt->fetch();
    }
} catch (PDOException $e) {
    error_log('Google login DB error: ' . $e->getMessage());
    fail('Could not create or load your account', 500);
}

session_regenerate_id(true);
$_SESSION['user_id'] = $user['id'];
$_SESSION['user_name'] = $user['full_name'];
$_SESSION['user_email'] = $user['email'];
$_SESSION['user_role'] = $user['role'];

echo json_encode(['ok' => true]);