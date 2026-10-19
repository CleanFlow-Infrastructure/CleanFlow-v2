<?php
require_once '../Database.php';
require_once '../includes/google_config.php';
if (session_status() === PHP_SESSION_NONE) { session_start(); }
$database = new Database();
$pdo = $database->getConnection();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';
    
    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();
    
    if ($user && password_verify($password, $user['password'])) {
        session_regenerate_id(true);
        $_SESSION['user_id'] = $user['id'];
        $_SESSION['user_name'] = $user['full_name'];
        $_SESSION['user_email'] = $user['email'];
        $_SESSION['user_role'] = $user['role'];
        header("Location: ../index.php");
        exit();
    } else {
        $error = "Invalid email or password";
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - CleanFlow</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://accounts.google.com/gsi/client" async defer></script>
</head>
<body class="bg-gradient-to-br from-blue-50 to-slate-100 min-h-screen flex items-center justify-center p-6">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-xl p-8">
        <div class="text-center mb-6">
            <img src="../images/logo.png" alt="CleanFlow" class="h-16 mx-auto mb-3" onerror="this.src='https://via.placeholder.com/64?text=CF'">
            <h2 class="text-2xl font-bold text-slate-800">Welcome Back</h2>
        </div>
        
        <?php if (isset($error)): ?>
            <div class="bg-red-100 text-red-700 border border-red-200 rounded-lg p-4 mb-4"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        <div id="googleError" class="hidden bg-red-100 text-red-700 border border-red-200 rounded-lg p-4 mb-4"></div>
        
        <form method="POST" class="space-y-5">
            <div>
                <label class="block text-sm font-medium text-slate-700 mb-2">Email Address</label>
                <input type="email" name="email" required class="w-full px-4 py-3 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600">
            </div>
            <div>
                <label class="block text-sm font-medium text-slate-700 mb-2">Password</label>
                <input type="password" name="password" required class="w-full px-4 py-3 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600">
            </div>
            <div class="flex items-center justify-between">
                <label class="flex items-center"><input type="checkbox" class="mr-2"> Remember me</label>
                <a href="#" class="text-sm text-blue-600 hover:underline">Forgot Password?</a>
            </div>
            <button type="submit" class="w-full py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold">Sign In</button>
        </form>

        <div class="flex items-center gap-3 my-6">
            <div class="flex-1 h-px bg-slate-200"></div>
            <span class="text-sm text-slate-500">or</span>
            <div class="flex-1 h-px bg-slate-200"></div>
        </div>

        <div id="g_id_onload" data-client_id="<?php echo htmlspecialchars(GOOGLE_CLIENT_ID); ?>" data-callback="handleGoogle" data-auto_prompt="false"></div>
        <div class="flex justify-center">
            <div class="g_id_signin" data-type="standard" data-size="large" data-theme="outline" data-text="signin_with" data-shape="rectangular" data-width="340"></div>
        </div>

        <p class="text-center mt-6 text-slate-600">Don't have an account? <a href="register.php" class="text-blue-600 font-semibold">Create Account</a></p>
    </div>

    <script>
        function handleGoogle(response) {
            var box = document.getElementById('googleError');
            box.classList.add('hidden');
            fetch('google_login.php', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ credential: response.credential })
            })
            .then(function (r) { return r.json(); })
            .then(function (d) {
                if (d.ok) { window.location.href = '../index.php'; }
                else { box.textContent = d.error || 'Google sign-in failed. Please try again.'; box.classList.remove('hidden'); }
            })
            .catch(function () {
                box.textContent = 'Could not reach the server. Please try again.';
                box.classList.remove('hidden');
            });
        }
    </script>
</body>
</html>